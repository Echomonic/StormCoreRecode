package net.nethersmp.storm.punishment.api;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public record PunishmentReason(String text) {


    public static PunishmentReason of(String reason) {
        return new PunishmentReason(reason);
    }
}
