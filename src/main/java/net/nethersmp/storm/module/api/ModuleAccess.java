package net.nethersmp.storm.module.api;

import java.util.Optional;

public interface ModuleAccess {

    <T> T require(String id, Class<T> type);

    Optional<Object> get(String id);

}
