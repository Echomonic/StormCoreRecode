package net.nethersmp.storm.punishment.api;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.UUID;


@JsonFormat(shape = JsonFormat.Shape.STRING)
public record PunishmentId(String text) {

    public static PunishmentId create() {
        UUID randomUUID = UUID.randomUUID();
        return of(randomUUID.toString().replace("-", "").substring(0, 16));
    }

    public static PunishmentId of(String id) {
        return new PunishmentId(id);
    }
}
