package net.nethersmp.storm.module.api;

import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public record ModuleDefinition<T>(
        String id,
        Set<String> dependencies,
        int priority,
        Function<ModuleAccess, Module<T>> factory
) {
    public ModuleDefinition {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(dependencies, "dependencies");
        Objects.requireNonNull(factory, "factory");
    }
}
