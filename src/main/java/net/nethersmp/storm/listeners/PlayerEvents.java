package net.nethersmp.storm.listeners;

import lombok.RequiredArgsConstructor;
import net.nethersmp.storm.StormPlugin;
import net.nethersmp.storm.user.UserDataModifier;
import net.nethersmp.storm.user.data.UserDataType;
import net.nethersmp.storm.user.data.UserPunishmentDataType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerEvents implements Listener {

    private final StormPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        UUID player = event.getUniqueId();
        plugin.getUserDataAccessor().load(player);
        plugin.getUserDataAccessor().defaults(player, Arrays.asList(
                UserDataType.RANK,
                UserPunishmentDataType.CURRENT_PUNISHMENT,
                UserPunishmentDataType.OLD_PUNISHMENTS
        ));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        UserDataType.USERNAME.set(player.getUniqueId(), player.getName());
        UserDataType.UUID.set(player.getUniqueId(), player.getUniqueId().toString());

        System.out.println(UserDataModifier.data(player.getUniqueId()).getFlat());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getUserDataAccessor().flush(player.getUniqueId());
    }

}
