package net.nethersmp.storm.cooldown;

import net.nethersmp.storm.cooldown.api.UserCooldownRecord;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.utilities.Strings;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CooldownModule implements Module<Void> {

    public static final String ID = "cooldowns";
    public static final Set<String> DEPENDENCIES = Set.of();
    public static final int PRIORITY = 1000;

    private final ConcurrentMap<UUID, ConcurrentMap<String, Long>> userCooldowns = new ConcurrentHashMap<>();

    @Override
    public Result<Void> load() {
        return Result.success();
    }

    @Override
    public void unload() {
        userCooldowns.clear();
    }

    public boolean isCooldownOver(UUID player, String id) {
        ConcurrentMap<String, Long> cooldowns = userCooldowns.getOrDefault(player, new ConcurrentHashMap<>());

        if (!cooldowns.containsKey(id)) return true;
        boolean finished = cooldowns.get(id) - System.currentTimeMillis() <= 0;
        if (finished) {
            remove(player, id);
        }
        return finished;
    }

    public boolean hasCooldown(UUID player, String id) {
        ConcurrentMap<String, Long> cooldowns = userCooldowns.getOrDefault(player, new ConcurrentHashMap<>());
        return cooldowns.containsKey(id);
    }

    public void put(UUID player, UserCooldownRecord record) {
        ConcurrentMap<String, Long> personCooldowns = userCooldowns.getOrDefault(player, new ConcurrentHashMap<>());
        personCooldowns.put(record.id(), record.time());
        userCooldowns.put(player, personCooldowns);
    }

    public void remove(UUID player, String id) {
        ConcurrentMap<String, Long> personalPunishments = userCooldowns.get(player);
        personalPunishments.remove(id);
        userCooldowns.put(player, personalPunishments);
    }

    public void clear(UUID player) {
        userCooldowns.remove(player);
    }

    public String getTimeLeft(UUID player, String id) {
        ConcurrentMap<String, Long> personCooldowns = userCooldowns.getOrDefault(player, new ConcurrentHashMap<>());
        Long cooldownMillis = personCooldowns.get(id);
        return cooldownMillis == null ? null : Strings.date(cooldownMillis - System.currentTimeMillis());
    }


    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<String> dependencies() {
        return DEPENDENCIES;
    }

    @Override
    public int priority() {
        return PRIORITY;
    }
}
