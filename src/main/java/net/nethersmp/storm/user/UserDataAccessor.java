package net.nethersmp.storm.user;

import net.nethersmp.storm.user.data.api.UserData;
import net.nethersmp.storm.user.data.api.UserDataKey;
import net.nethersmp.storm.user.storage.UserDataStore;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserDataAccessor {

    private final UserDataStore store;
    private final Map<UUID, UserData> loaded = new ConcurrentHashMap<>();
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();

    public UserDataAccessor(UserDataStore store) {
        this.store = store;
    }

    /* lifecycle */

    public void load(UUID playerId) {
        try {
            loaded.put(playerId, store.load(playerId));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load data for " + playerId, e);
        }
    }

    public void defaults(UUID playerId, Collection<UserDataKey<?>> keys) {
        UserData state = data(playerId);
        boolean changed = false;

        for (UserDataKey<?> key : keys) {
            if (state.get(key).isPresent()) continue;

            state.setFlat(key.id(), key.defaultValue());
            changed = true;
        }

        if (changed) {
            markDirty(playerId);
        }
    }

    public void unload(UUID playerId) {
        flush(playerId);
        loaded.remove(playerId);
        dirty.remove(playerId);
    }

    /* internal access */

    UserData data(UUID playerId) {
        UserData data = loaded.get(playerId);
        if (data == null)
            throw new IllegalStateException("Player data not loaded: " + playerId);
        return data;
    }

    void markDirty(UUID playerId) {
        dirty.add(playerId);
    }

    /* persistence */

    public void flush(UUID playerId) {
        if (!dirty.contains(playerId)) return;
        try {
            store.save(playerId, loaded.get(playerId));
            dirty.remove(playerId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save data for " + playerId, e);
        }
    }

    public void flushAll() {
        for (UUID id : new ArrayList<>(dirty)) {
            flush(id);
        }
    }

}
