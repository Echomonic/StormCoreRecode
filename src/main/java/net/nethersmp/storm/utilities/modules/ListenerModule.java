package net.nethersmp.storm.utilities.modules;

import lombok.RequiredArgsConstructor;
import net.nethersmp.storm.StormPlugin;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ListenerModule implements Module<Void> {

    private final StormPlugin plugin;

    private final Map<String, List<Listener>> owned = new HashMap<>();

    @Override
    public int priority() {
        return 1000;
    }

    @Override
    public String id() {
        return "listeners";
    }

    @Override
    public Result<Void> load() {
        return plugin == null ? Result.fail("NULL POINTER", "Plugin is null.") : Result.success();
    }

    public <E extends Event> void listen(String owner, Class<E> eventClass, Consumer<E> consumer) {
        listen(owner, eventClass, EventPriority.NORMAL, consumer);
    }

    public <E extends Event> void listen(String owner, Class<E> eventClass, EventPriority priority, Consumer<E> consumer) {
        listen(owner, eventClass, priority, false, consumer);
    }

    public <E extends Event> void listen(String owner, Class<E> eventType, EventPriority priority, boolean ignoreCancelled, Consumer<E> handler) {

        Listener listener = new Listener() {
        };

        EventExecutor executor = (l, event) -> {
            if (!eventType.isAssignableFrom(event.getClass())) {
                System.out.println("Event type doesn't match.");
                return;
            }
            handler.accept(eventType.cast(event));
        };

        plugin.getServer().getPluginManager().registerEvent(
                eventType,
                listener,
                priority,
                executor,
                plugin,
                ignoreCancelled
        );
        owned.computeIfAbsent(owner.toLowerCase(), k -> new ArrayList<>()).add(listener);
    }

    public boolean call(Event event) {
        return event.callEvent();
    }

    public void unloadOwner(String owner) {
        List<Listener> listeners = owned.get(owner.toLowerCase());
        if (listeners == null || listeners.isEmpty()) return;

        for (Listener listener : listeners) {
            HandlerList.unregisterAll(listener);
        }
        owned.remove(owner);
    }

    @Override
    public void unload() {
        owned.clear();
        HandlerList.unregisterAll(plugin);
    }
}
