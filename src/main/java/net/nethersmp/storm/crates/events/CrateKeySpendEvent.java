package net.nethersmp.storm.crates.events;

import lombok.Getter;
import lombok.Setter;
import net.nethersmp.storm.crates.api.CrateData;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class CrateKeySpendEvent extends PlayerEvent implements Cancellable {

    @Getter
    private static final HandlerList handlerList = new HandlerList();

    @Getter
    private final CrateData purchasedCrate;

    @Getter
    @Setter
    private boolean cancelled;

    public CrateKeySpendEvent(@NotNull Player player, CrateData purchasedCrate) {
        super(player);
        this.purchasedCrate = purchasedCrate;
    }


    @Override
    public @NotNull HandlerList getHandlers() {
        return handlerList;
    }
}
