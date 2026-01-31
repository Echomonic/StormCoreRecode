package net.nethersmp.storm.module.api;

import java.util.Set;

public interface Module<T> {
    default String id() {
        return getClass().getSimpleName().toLowerCase();
    }

    default Set<String> dependencies() {
        return Set.of();
    }

    default int priority() {
        return 0;
    }

    Result<T> load();

    void unload();


}
