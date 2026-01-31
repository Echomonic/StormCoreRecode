package net.nethersmp.storm.listeners;

import lombok.RequiredArgsConstructor;
import net.nethersmp.storm.StormPlugin;
import net.nethersmp.storm.user.data.UserCrateKeyDataType;
import net.nethersmp.storm.user.data.UserDataType;
import net.nethersmp.storm.user.data.UserPunishmentDataType;
import net.nethersmp.storm.user.data.api.UserDataKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class PlayerEvents implements Listener {

    private final StormPlugin plugin;

    @EventHandler(priority = EventPriority.HIGH)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        UUID player = event.getUniqueId();
        plugin.getUserDataAccessor().load(player);

        List<UserDataKey<?>> statDefaults = new ArrayList<>(Arrays.asList(
                UserDataType.RANK,
                UserPunishmentDataType.CURRENT_PUNISHMENT,
                UserPunishmentDataType.OLD_PUNISHMENTS
        ));

        statDefaults.addAll(UserCrateKeyDataType.getStandardTypes());

        plugin.getUserDataAccessor().defaults(player, statDefaults);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        UserDataType.USERNAME.set(player.getUniqueId(), player.getName());
        UserDataType.UUID.set(player.getUniqueId(), player.getUniqueId().toString());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getUserDataAccessor().flush(player.getUniqueId());
    }

}
