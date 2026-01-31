package net.nethersmp.storm.punishment.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

public record PunishmentDuration(long future) {

    @JsonValue
    public long value() {
        return future;
    }

    @JsonIgnore
    public boolean isOver() {
        if (future == 0L) return false;
        return System.currentTimeMillis() >= future;
    }

    public static PunishmentDuration of(long future) {
        return new PunishmentDuration(future);
    }
}
