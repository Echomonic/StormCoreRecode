package net.nethersmp.storm.punishment.api.storage;

import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.punishment.UserPunishment;

import java.util.Optional;
import java.util.UUID;

public interface PunishmentDataStore {

    void initialize();

    Optional<UserPunishment> load(UUID player);


    Result<String> write(UserPunishment punishment);
}
