package net.nethersmp.storm.user;

import net.nethersmp.storm.user.data.api.UserData;

import java.util.UUID;

public class UserDataModifier {

    private static UserDataAccessor accessor;

    private UserDataModifier() {
    }

    public static void init(UserDataAccessor acc) {
        accessor = acc;
    }

    public static UserData data(UUID playerId) {
        if (accessor == null)
            throw new IllegalStateException("UserData not initialized");
        return accessor.data(playerId);
    }

    public static void markDirty(UUID playerId) {
        accessor.markDirty(playerId);
    }

}
