package net.nethersmp.storm.user.data;

import lombok.experimental.UtilityClass;
import net.nethersmp.storm.user.data.api.UserDataKey;

import java.util.ArrayList;

@UtilityClass
public class UserCrateKeyDataType {

    public final UserDataKey<Long> COMMON = template("common");
    public final UserDataKey<Long> UNCOMMON = template("uncommon");
    public final UserDataKey<Long> RARE = template("rare");
    public final UserDataKey<Long> VIP = template("vip");
    public final UserDataKey<Long> LEGEND = template("legend");
    public final UserDataKey<Long> ULTIMATE = template("ultimate");


    private final ArrayList<String> IDS = new ArrayList<>();

    static {
        IDS.add("common");
        IDS.add("uncommon");
        IDS.add("rare");
        IDS.add("vip");
        IDS.add("legend");
        IDS.add("ultimate");
    }

    /**
     * Used for custom crates that aren't part of the standard keys.
     *
     * @param keyName the name of crate
     * @return the data type used for handling data
     */
    public UserDataKey<Long> template(String keyName) {
        return UserDataKey.of("key." + keyName, Long.class, 0L);
    }

    public boolean isStandardType(String key) {
        return IDS.contains(key);
    }
}
