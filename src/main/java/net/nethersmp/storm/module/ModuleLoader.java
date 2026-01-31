package net.nethersmp.storm.module;

import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.ModuleAccess;
import net.nethersmp.storm.module.api.ModuleDefinition;
import net.nethersmp.storm.module.api.Result;

import java.util.*;

public class ModuleLoader implements ModuleAccess {

    public enum State {DISCOVERED, READY, LOADED, FAILED, BLOCKED}

    public record StepResult(
            boolean hasMoreWork,
            int loadedThisStep,
            Map<String, State> states,
            Map<String, String> reasons,
            Map<String, String> warnings
    ) {
    }

    // ========= Storage =========
    private final Map<String, ModuleDefinition<?>> defs = new HashMap<>();
    private final Map<String, net.nethersmp.storm.module.api.Module<?>> instances = new HashMap<>();

    private final Map<String, State> state = new LinkedHashMap<>();
    private final Map<String, String> reason = new LinkedHashMap<>();
    private final Map<String, String> warnings = new LinkedHashMap<>();

    // dep -> modules that require it
    private final Map<String, List<String>> dependents = new HashMap<>();
    private final Map<String, Integer> unmet = new HashMap<>();

    // modules ready to load
    private final PriorityQueue<String> ready = new PriorityQueue<>(this::compareReady);

    // successful load order for proper unload order
    private final List<String> loadOrder = new ArrayList<>();

    private boolean initialized;

    // =========================
    // Public API
    // =========================

    public void register(ModuleDefinition<?> def) {
        ensureNotInitialized();
        putDef(def);
    }

    /**
     * Build graph + seed ready queue. Call once before step().
     */
    public void init() {
        if (initialized) return;
        initialized = true;

        buildGraph();
        seedReadyQueue();
    }

    /**
     * Load up to maxToLoad modules this call.
     * Call repeatedly (e.g. per tick) to avoid long stalls.
     */
    public StepResult step(int maxToLoad) {
        ensureInitialized();

        if (maxToLoad <= 0) return snapshot(!ready.isEmpty(), 0);

        int loaded = processReady(maxToLoad);

        boolean moreWork = !ready.isEmpty();
        if (!moreWork) finalizeUnresolved();

        return snapshot(moreWork, loaded);
    }

    /**
     * Unload all loaded modules in correct order (dependents first).
     */
    public void unloadAll() {
        for (int i = loadOrder.size() - 1; i >= 0; i--) {
            String id = loadOrder.get(i);
            net.nethersmp.storm.module.api.Module<?> m = instances.remove(id);
            if (m == null) continue;

            try {
                m.unload();
            } catch (Exception ex) {
                // Don't crash shutdown; record for logs
                reason.putIfAbsent(id, "Unload failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
            } finally {
                // Mark as discovered so a reload can call init+step again if you rebuild the loader state.
                state.put(id, State.DISCOVERED);
            }
        }

        loadOrder.clear();
        instances.clear();
        warnings.clear();
    }

    public Map<String, State> states() {
        return Map.copyOf(state);
    }

    public Map<String, String> reasons() {
        return Map.copyOf(reason);
    }

    public Map<String, String> warnings() {
        return Map.copyOf(warnings);
    }


    @Override
    public <T> T require(String id, Class<T> type) {
        Object o = instances.get(id);
        if (o == null) throw new IllegalStateException("Required module not loaded: " + id);
        if (!type.isInstance(o)) throw new ClassCastException("Module " + id + " is not a " + type.getName());
        return type.cast(o);
    }

    @Override
    public Optional<Object> get(String id) {
        return Optional.ofNullable(instances.get(id));
    }


    private void ensureNotInitialized() {
        if (initialized) throw new IllegalStateException("Cannot register after init()");
    }

    private void ensureInitialized() {
        if (!initialized) throw new IllegalStateException("Call init() first");
    }

    private void putDef(ModuleDefinition<?> def) {
        Objects.requireNonNull(def, "def");
        if (defs.putIfAbsent(def.id(), def) != null) {
            throw new IllegalArgumentException("Duplicate module id: " + def.id());
        }
        state.put(def.id(), State.DISCOVERED);
    }

    private int compareReady(String a, String b) {
        ModuleDefinition<?> da = defs.get(a);
        ModuleDefinition<?> db = defs.get(b);

        int p = Integer.compare(db.priority(), da.priority()); // higher first
        return (p != 0) ? p : a.compareTo(b);                 // deterministic
    }

    private void buildGraph() {
        for (ModuleDefinition<?> def : defs.values()) {
            if (isTerminal(def.id())) {
                System.out.println(def.id() + " is terminal!");
                continue;
            }

            int count = 0;
            for (String dep : def.dependencies()) {
                if (!defs.containsKey(dep)) {
                    block(def.id(), "Missing required dependency: " + dep);
                    continue;
                }
                dependents.computeIfAbsent(dep, __ -> new ArrayList<>()).add(def.id());
                count++;
            }
            unmet.put(def.id(), count);
        }
    }

    private void seedReadyQueue() {
        for (String id : defs.keySet()) {
            if (isTerminal(id)) continue;
            if (unmet.getOrDefault(id, 0) == 0) markReady(id);
        }
    }

    private int processReady(int maxToLoad) {
        int loaded = 0;

        for (int i = 0; i < maxToLoad; i++) {
            String id = ready.poll();
            if (id == null) break;

            if (isTerminal(id)) continue;
            if (attemptLoad(id)) loaded++;
        }

        return loaded;
    }

    private boolean attemptLoad(String id) {
        ModuleDefinition<?> def = defs.get(id);

        try {
            net.nethersmp.storm.module.api.Module<?> module = construct(def);
            Result<?> res = module.load();
            handleLoadResult(id, res);

            instances.put(id, module);
            state.put(id, State.LOADED);
            loadOrder.add(id);

            onLoaded(id);
            return true;

        } catch (Exception ex) {
            fail(id, ex);
            cascadeBlockFrom(id);
            return false;
        }
    }

    private net.nethersmp.storm.module.api.Module<?> construct(ModuleDefinition<?> def) {
        Module<?> module = def.factory().apply(this);
        if (module == null) throw new IllegalStateException("Factory returned null for " + def.id());

        if (!Objects.equals(module.id(), def.id())) {
            throw new IllegalStateException("Factory id mismatch: def=" + def.id() + " module=" + module.id());
        }
        return module;
    }

    /**
     * Semantics:
     * - Success => OK
     * - Warn    => OK but record warning
     * - Fail    => throw => module FAILED and dependents BLOCKED
     */
    private void handleLoadResult(String id, Result<?> res) {
        if (res == null) throw new IllegalStateException(id + ".load() returned null");

        if (Result.failed(res)) {
            Result.Fail f = (Result.Fail) res;
            throw new ModuleLoadFailedException(f.code(), f.message());
        }

        if (Result.warned(res)) {
            Result.Warn w = (Result.Warn) res;
            warnings.put(id, w.code() + ": " + w.message());
            // still treated as LOADED
            return;
        }

        if (Result.succeeded(res)) {
            return;
        }

        // Defensive: sealed should make this impossible unless something weird happens.
        throw new IllegalStateException("Unknown Result type: " + res.getClass().getName());
    }

    private void onLoaded(String id) {
        for (String dependent : dependents.getOrDefault(id, List.of())) {
            if (isTerminal(dependent)) continue;

            int left = unmet.merge(dependent, -1, Integer::sum);
            if (left == 0) markReady(dependent);
        }
    }

    private void markReady(String id) {
        if (isTerminal(id)) return;
        state.put(id, State.READY);
        ready.add(id);
    }

    private void fail(String id, Exception ex) {
        state.put(id, State.FAILED);

        if (ex instanceof ModuleLoadFailedException mlfe) {
            reason.put(id, "Load failed [" + mlfe.code + "]: " + mlfe.getMessage());
        } else {
            reason.put(id, "Load failed: " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    private void block(String id, String why) {
        if (state.get(id) == State.FAILED) return;
        state.put(id, State.BLOCKED);
        reason.putIfAbsent(id, why);
    }

    private void cascadeBlockFrom(String failedDepId) {
        ArrayDeque<String> q = new ArrayDeque<>();
        q.add(failedDepId);

        while (!q.isEmpty()) {
            String dep = q.removeFirst();

            for (String dependent : dependents.getOrDefault(dep, List.of())) {
                if (state.get(dependent) == State.LOADED) continue;
                if (state.get(dependent) == State.BLOCKED) continue;
                if (state.get(dependent) == State.FAILED) continue;

                block(dependent, "Required dependency failed: " + failedDepId);
                q.add(dependent);
            }
        }
    }

    private void finalizeUnresolved() {
        for (String id : defs.keySet()) {
            State s = state.get(id);
            if (s == State.DISCOVERED || s == State.READY) {
                block(id, "Cycle or unresolved dependencies (unmet=" + unmet.getOrDefault(id, 0) + ")");
            }
        }
    }

    private boolean isTerminal(String id) {
        State s = state.get(id);

        System.out.println(id + ": " + s);

        return s == State.LOADED || s == State.FAILED || s == State.BLOCKED;
    }

    private StepResult snapshot(boolean moreWork, int loadedThisStep) {
        return new StepResult(
                moreWork,
                loadedThisStep,
                Map.copyOf(state),
                Map.copyOf(reason),
                Map.copyOf(warnings)
        );
    }

    private static final class ModuleLoadFailedException extends RuntimeException {
        final String code;

        ModuleLoadFailedException(String code, String message) {
            super(message);
            this.code = code;
        }
    }

}
