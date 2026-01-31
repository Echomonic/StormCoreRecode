package net.nethersmp.storm.user.data.api;

import net.nethersmp.storm.user.UserDataModifier;

import java.util.UUID;

public record UserDataKey<T>(String id, Class<T> type, T defaultValue) {

    public static <T> UserDataKey<T> of(String id, Class<T> type, T defaultValue) {
        return new UserDataKey<>(id, type, defaultValue);
    }

    public T get(UUID uuid) {
        return UserDataModifier.data(uuid).get(this).orElse(defaultValue);
    }

    public T getOrDefault(UUID uuid, T defaultValue) {
        return UserDataModifier.data(uuid).getOrDefault(this, defaultValue);
    }

    public void set(UUID playerId, T type) {
        UserDataModifier.data(playerId).set(this, type);
        UserDataModifier.markDirty(playerId);
    }

    public void remove(UUID playerId) {
        UserDataModifier.data(playerId).remove(this);
        UserDataModifier.markDirty(playerId);
    }

    @Override
    public String toString() {
        return id;
    }

}
