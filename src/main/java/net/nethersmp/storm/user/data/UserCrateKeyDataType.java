package net.nethersmp.storm.user.data;

import lombok.experimental.UtilityClass;
import net.nethersmp.storm.user.data.api.UserDataKey;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

@UtilityClass
public class UserCrateKeyDataType {

    public final UserDataKey<Long> COMMON = template("common");
    public final UserDataKey<Long> UNCOMMON = template("uncommon");
    public final UserDataKey<Long> RARE = template("rare");
    public final UserDataKey<Long> VIP = template("vip");
    public final UserDataKey<Long> LEGEND = template("legend");
    public final UserDataKey<Long> ULTIMATE = template("ultimate");


    private final HashMap<String, UserDataKey<Long>> BY_IDS = new HashMap<>();
    private HashSet<UserDataKey<Long>> VALUES;
    static {
        BY_IDS.put("common", COMMON);
        BY_IDS.put("uncommon", UNCOMMON);
        BY_IDS.put("rare", RARE);
        BY_IDS.put("vip", VIP);
        BY_IDS.put("legend", LEGEND);
        BY_IDS.put("ultimate", ULTIMATE);

        VALUES = new HashSet<>(BY_IDS.values());
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
        return BY_IDS.containsKey(key);
    }

    public UserDataKey<Long> getStandardType(String key) {
        return BY_IDS.get(key);
    }

    public Set<UserDataKey<Long>> getStandardTypes() {
        return VALUES;
    }

}
