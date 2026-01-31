package net.nethersmp.storm.punishment.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public record PunishmentReason(String text) {

    @JsonValue
    public String value() {
        return text;
    }

    public static PunishmentReason of(String reason) {
        return new PunishmentReason(reason);
    }
}
