package net.nethersmp.storm.user.data;

import lombok.experimental.UtilityClass;
import net.nethersmp.storm.user.data.api.UserDataKey;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class UserPunishmentDataType {

    public final UserDataKey<String> CURRENT_PUNISHMENT = UserDataKey.of("punishment.current", String.class, "");
    public final UserDataKey<List<String>> OLD_PUNISHMENTS =
            UserDataKey.of("punishment.old",
                    (Class<List<String>>) (Class<?>) List.class,
                    new ArrayList<String>());

}
