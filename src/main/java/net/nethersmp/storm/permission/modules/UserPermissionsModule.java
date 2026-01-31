package net.nethersmp.storm.permission.modules;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.permission.modifiers.EmptyPermissible;
import net.nethersmp.storm.permission.modifiers.LeafPermissible;
import net.nethersmp.storm.permission.modifiers.PermissibleFields;
import net.nethersmp.storm.utilities.ListenerModule;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissibleBase;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class UserPermissionsModule implements Module<Void> {

    private final ListenerModule events;

    private final ConcurrentHashMap<UUID, PermissibleBase> oldPlayerPermissions = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return "user_permissions";
    }

    @Override
    public int priority() {
        return 990;
    }

    @Override
    public Set<String> dependencies() {
        return Set.of("listeners");
    }

    @Override
    public Result<Void> load() {
        events.listen(id(), PlayerJoinEvent.class, event -> {
            Player player = event.getPlayer();
            Result<Boolean> loadedPlayer = loadPlayer(player);

            if (loadedPlayer.failed())
                player.sendMessage(loadedPlayer.toComponent());
        });
        events.listen(id(), PlayerQuitEvent.class, event -> unloadPlayer(event.getPlayer(), false));
        return Result.success();
    }

    private Result<Boolean> loadPlayer(Player player) {
        try {
            PermissibleBase oldPermissions = (PermissibleBase) PermissibleFields.ENTITY_PERMISSIONS.get(player);
            oldPlayerPermissions.put(player.getUniqueId(), oldPermissions);

            if (oldPermissions instanceof LeafPermissible) return Result.fail("PERMISSIONS", "Permissions already set. (SHOULD NOT HAPPEN!)");
            LeafPermissible injectedPermissible = new LeafPermissible(player);

            injectedPermissible.copy(oldPermissions);

            PermissibleFields.ENTITY_PERMISSIONS.set(player, injectedPermissible);
            return Result.success(true);
        } catch (IllegalAccessException e) {
            return Result.fail("PERMISSIONS", "Failed to load permissions");
        }
    }

    @SneakyThrows
    private void unloadPlayer(Player player, boolean dummy) {
        if (dummy)
            PermissibleFields.ENTITY_PERMISSIONS.set(player, EmptyPermissible.INSTANCE);
        else
            PermissibleFields.ENTITY_PERMISSIONS.set(player, oldPlayerPermissions.get(player.getUniqueId()));

        oldPlayerPermissions.remove(player.getUniqueId());
    }

    @Override
    public void unload() {
        events.unloadOwner(id());
    }
}
