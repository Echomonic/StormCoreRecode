package net.nethersmp.storm.punishment.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

@JsonFormat(shape = JsonFormat.Shape.NUMBER)
public record PunishmentDuration(long future) {

    @JsonIgnore
    public boolean isOver() {
        if (future == 0L) return false;
        return System.currentTimeMillis() >= future;
    }

    public static PunishmentDuration of(long future) {
        return new PunishmentDuration(future);
    }
}
