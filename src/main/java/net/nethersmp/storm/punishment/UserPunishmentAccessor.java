package net.nethersmp.storm.punishment;

import lombok.Setter;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.punishment.api.storage.PunishmentDataStore;

import java.util.Optional;
import java.util.UUID;

public class UserPunishmentAccessor {

    @Setter
    private static PunishmentDataStore punishmentStorage;

    public static Optional<UserPunishment> get(UUID player) {
        if (punishmentStorage == null)
            return Optional.empty();

        return punishmentStorage.load(player);
    }

    public static Result<String> write(UserPunishment punishment) {
        return punishmentStorage.write(punishment);
    }
}
