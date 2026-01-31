package net.nethersmp.storm.user.storage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import net.nethersmp.storm.user.data.api.UserData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
public class UserDataStore {

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final TypeReference<Map<String, Object>> MAP =
            new TypeReference<>() {
            };


    public UserData load(UUID playerId) throws IOException {
        Map<String, Object> root = readRoot();
        Map<String, Object> players = map(root.get("players"));
        Map<String, Object> nested =
                players == null ? null : map(players.get(playerId.toString()));

        UserData data = new UserData();

        if (nested != null) {
            for (var ns : nested.entrySet()) {
                String namespace = ns.getKey();
                Map<String, Object> sub = map(ns.getValue());
                if (sub == null) continue;

                for (var e : sub.entrySet()) {
                    data.setFlat(namespace + "." + e.getKey(), e.getValue());
                }
            }
        }
        return data;
    }

    public void save(UUID playerId, UserData data) throws IOException {
        Map<String, Object> root = readRoot();
        Map<String, Object> players = mapOrCreate(root, "players");

        Map<String, Object> nested = new LinkedHashMap<>();

        for (var e : data.getFlat().entrySet()) {
            String[] parts = e.getKey().split("\\.", 2);
            if (parts.length != 2) continue;

            @SuppressWarnings("unchecked")
            Map<String, Object> sub =
                    (Map<String, Object>) nested.computeIfAbsent(
                            parts[0], a -> new LinkedHashMap<>());

            sub.put(parts[1], e.getValue());
        }

        players.put(playerId.toString(), nested);
        atomicWrite(root);
    }

    /* ---------- helpers ---------- */

    private Map<String, Object> readRoot() throws IOException {
        if (!Files.exists(file)) {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("players", new LinkedHashMap<>());
            return root;
        }
        try (InputStream in = Files.newInputStream(file)) {
            return mapper.readValue(in, MAP);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> map(Object o) {
        if (o == null) return null;
        if (o instanceof Map<?, ?> m) return (Map<String, Object>) m;
        throw new IllegalStateException("Expected Map but got " + o.getClass());
    }

    private Map<String, Object> mapOrCreate(Map<String, Object> root, String key) {
        Map<String, Object> m = map(root.get(key));
        if (m != null) return m;
        Map<String, Object> created = new LinkedHashMap<>();
        root.put(key, created);
        return created;
    }

    private void atomicWrite(Map<String, Object> root) throws IOException {
        Path parent = file.getParent();
        if (parent != null) Files.createDirectories(parent);

        Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
        try (OutputStream out = Files.newOutputStream(tmp)) {
            mapper.writerWithDefaultPrettyPrinter().writeValue(out, root);
        }
        Files.move(tmp, file,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE);
    }

}
