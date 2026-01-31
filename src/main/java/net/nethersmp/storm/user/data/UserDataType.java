package net.nethersmp.storm.user.data;

import lombok.experimental.UtilityClass;
import net.nethersmp.storm.user.data.api.UserDataKey;

@UtilityClass
public class UserDataType {

    public final UserDataKey<String> UUID = UserDataKey.of("basic.uuid", String.class, "");
    public final UserDataKey<String> USERNAME = UserDataKey.of("basic.username", String.class, "");
    public final UserDataKey<String> RANK = UserDataKey.of("basic.rank", String.class, "member");

}
