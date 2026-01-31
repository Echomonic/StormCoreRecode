package net.nethersmp.storm.user.data.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UserData {

    private final Map<String, Object> userData = new HashMap<>();

    public <T> Optional<T> get(UserDataKey<T> key) {
        Object value = userData.get(key.id());
        return value == null ? Optional.empty() : Optional.of(key.type().cast(value));
    }

    public <T> T getOrDefault(UserDataKey<T> key, T defaultValue) {
        return get(key).orElse(defaultValue);
    }

    public <T> void set(UserDataKey<T> key, T value) {
        setFlat(key.id(), key.type().cast(value));
    }

    public void setFlat(String key, Object value) {
        userData.put(key, value);
    }

    public void remove(UserDataKey<?> key) {
        userData.remove(key.id());
    }

    public Map<String, Object> getFlat() {
        return userData;
    }

}
