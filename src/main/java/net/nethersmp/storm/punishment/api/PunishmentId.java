package net.nethersmp.storm.punishment.api;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.UUID;


public record PunishmentId(String text) {

    @JsonValue
    public String value() {
        return text;
    }

    public static PunishmentId create() {
        UUID randomUUID = UUID.randomUUID();
        return of(randomUUID.toString().replace("-", "").substring(0, 16));
    }

    public static PunishmentId of(String id) {
        return new PunishmentId(id);
    }
}
