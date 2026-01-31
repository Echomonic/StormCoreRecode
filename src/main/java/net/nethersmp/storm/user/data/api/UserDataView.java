package net.nethersmp.storm.user.data.api;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class UserDataView {
    private final UserData data;


    public <T> T get(UserDataKey<T> key, T def) {
        return data.getOrDefault(key, def);
    }

    public <T> Optional<T> get(UserDataKey<T> key) {
        return data.get(key);
    }

    public <T> void set(UserDataKey<T> key, T value) {
        data.set(key, value);
    }

}
