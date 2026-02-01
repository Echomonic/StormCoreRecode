package net.nethersmp.storm.cooldown.api;

import net.nethersmp.storm.utilities.Dates;

public record UserCooldownRecord(String id, long time) {

    public static UserCooldownRecord of(String id, String time) {
        return new UserCooldownRecord(id, Dates.segment(time));
    }

    public static UserCooldownRecord of(String id, long time) {
        return new UserCooldownRecord(id, time);
    }

}
