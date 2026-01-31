package net.nethersmp.storm.crates.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.nethersmp.storm.crates.api.CrateData;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

@EqualsAndHashCode(callSuper = true)
@RequiredArgsConstructor
@Data
public class CratesUpdateEvent extends Event {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    private final CrateData updatedData;
    private final CrateUpdateType updateType;

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }

    public enum CrateUpdateType {
        ADD,
        DELETE,
        CHANGE_ITEM
    }

}
