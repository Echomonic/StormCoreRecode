package net.nethersmp.storm.punishment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import net.nethersmp.storm.punishment.api.PunishmentDuration;
import net.nethersmp.storm.punishment.api.PunishmentId;
import net.nethersmp.storm.punishment.api.PunishmentReason;
import net.nethersmp.storm.punishment.api.PunishmentType;
import org.jspecify.annotations.NonNull;

import java.util.StringJoiner;

public record UserPunishment(PunishmentId id, PunishmentType type, PunishmentReason reason, PunishmentDuration duration) {

    @Override
    @JsonIgnore
    public @NonNull String toString() {
        return new StringJoiner(", ", UserPunishment.class.getSimpleName() + "[", "]")
                .add("id=" + id.text())
                .add("type=" + type.name())
                .add("reason=" + reason.text())
                .add("future=" + duration.future())
                .toString();
    }
}
