package net.nethersmp.storm.crates.modules;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.nethersmp.storm.StormPlugin;
import net.nethersmp.storm.cooldown.CooldownModule;
import net.nethersmp.storm.cooldown.api.UserCooldownRecord;
import net.nethersmp.storm.crates.api.CrateData;
import net.nethersmp.storm.crates.commands.CrateCommandNodes;
import net.nethersmp.storm.crates.commands.KeyCommandNodes;
import net.nethersmp.storm.crates.events.CrateKeySpendEvent;
import net.nethersmp.storm.crates.storage.CratesDataHandler;
import net.nethersmp.storm.crates.storage.CratesStorage;
import net.nethersmp.storm.crates.ui.CrateUserInterface;
import net.nethersmp.storm.crates.ui.SpinningCrateUserInterface;
import net.nethersmp.storm.module.api.Module;
import net.nethersmp.storm.module.api.Result;
import net.nethersmp.storm.user.data.api.UserDataKey;
import net.nethersmp.storm.utilities.Strings;
import net.nethersmp.storm.utilities.modules.CommandsModule;
import net.nethersmp.storm.utilities.modules.ListenerModule;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.ShulkerBox;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.nethersmp.storm.user.data.UserCrateKeyDataType.*;

public class CratesModule implements Module<Void> {

    public static final String ID = "crates";
    public static final Set<String> DEPENDENCIES = Set.of("commands", "cooldowns", "listeners");
    public static final int PRIORITY = 960;

    private final StormPlugin plugin;
    private final CommandsModule commands;
    private final ListenerModule events;
    private final CooldownModule cooldown;
    private final CratesDataHandler loader;
    private final CratesStorage storage;


    public CratesModule(StormPlugin plugin, CommandsModule commands, CooldownModule cooldown, ListenerModule events, Path path) {
        this.plugin = plugin;
        this.commands = commands;
        this.cooldown = cooldown;
        this.events = events;
        this.storage = new CratesStorage();
        this.loader = new CratesDataHandler(path, storage);
    }

    @Override
    public Result<Void> load() {
        loader.initialize();
        loader.read();

        commands.register(literal("crates")
                .then(CrateCommandNodes.open(loader, storage))
                .then(CrateCommandNodes.edit(storage))
                .then(CrateCommandNodes.remove(loader, storage))
                .then(CrateCommandNodes.make(storage))
                .then(CrateCommandNodes.give(storage))
                .then(KeyCommandNodes.base(storage))
                .build());

        events.listen(ID, BlockPlaceEvent.class, event -> {
            Player player = event.getPlayer();
            if (!player.hasPermission("stormcore.crates.place")) return;

            ItemStack item = event.getItemInHand();
            if (item == null) return;
            if (!item.hasItemMeta()) return;

            ItemMeta placedMeta = item.getItemMeta();
            PersistentDataContainer container = placedMeta.getPersistentDataContainer();
            String crateId = container.get(NamespacedKey.fromString("crate-id", plugin), PersistentDataType.STRING);
            if (crateId == null) return;

            if (storage.get(crateId) == null) {
                player.sendRichMessage("<red>The crate you placed doesn't exist!");
                player.sendRichMessage("<yellow>Removing crate from your inventory.");
                player.getInventory().remove(item);
                event.setCancelled(true);
                return;
            }

            Block block = event.getBlock();
            ShulkerBox shulkerBlock = (ShulkerBox) block.getState();
            shulkerBlock.getPersistentDataContainer().set(NamespacedKey.fromString("crate-id", plugin), PersistentDataType.STRING, crateId);
            shulkerBlock.update(true, false);
        });
        events.listen(ID, PlayerInteractEvent.class, event -> {
            if (event.getHand() != EquipmentSlot.HAND) return;
            Action interactAction = event.getAction();

            if (interactAction != Action.RIGHT_CLICK_BLOCK && interactAction != Action.LEFT_CLICK_BLOCK) return;

            Block clickedBlock = event.getClickedBlock();
            if (!clickedBlock.getType().name().contains("SHULKER_BOX")) return;

            ShulkerBox shulkerBox = (ShulkerBox) clickedBlock.getState();
            PersistentDataContainer shulkerContainer = shulkerBox.getPersistentDataContainer();

            String shulkerCrateId = shulkerContainer.get(NamespacedKey.fromString("crate-id", plugin), PersistentDataType.STRING);
            if (shulkerCrateId == null) return;

            CrateData crateData = storage.get(shulkerCrateId);

            if (crateData == null) return;

            Player player = event.getPlayer();
            boolean sneaking = player.isSneaking();
            event.setCancelled(true);

            PlayerInventory playerInventory = player.getInventory();

            switch (interactAction) {
                case LEFT_CLICK_BLOCK:
                    if (player.getGameMode() == GameMode.CREATIVE && player.hasPermission("stormcore.crates.break") && sneaking) {
                        event.setCancelled(true);
                        clickedBlock.setType(Material.AIR);
                        player.sendRichMessage("<gray>You <red>broke</red> a <yellow>%s Crate</yellow>.".formatted(Strings.fixCase(shulkerCrateId)));
                        player.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 1, 2));
                        return;
                    }
                    new CrateUserInterface(loader, storage, crateData, false).open(player);
                    break;
                case RIGHT_CLICK_BLOCK:

                    if (crateData.items().isEmpty()) {
                        player.sendRichMessage("<red>This crate doesn't have any times to give.");
                        return;
                    }
                    if (playerInventory.firstEmpty() == -1) {
                        player.sendRichMessage("<red>You can't open this crate, your inventory is full!");
                        return;
                    }
                    if (!events.call(new CrateKeySpendEvent(player, crateData))) return;
                    if (sneaking) {

                        if (!player.hasPermission("stormcore.crates.no-cooldown") && !cooldown.hasCooldown(player.getUniqueId(), "crates"))
                            cooldown.put(player.getUniqueId(), UserCooldownRecord.of("crates", "5s"));
                        else if (!cooldown.isCooldownOver(player.getUniqueId(), "crates")) {
                            player.sendRichMessage("<red>You can use this crate in <yellow>" + cooldown.getTimeLeft(player.getUniqueId(), "crates") + "</yellow>!");
                            player.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1.5f));
                            return;
                        }
                        giveUserReward(playerInventory, crateData);
                    } else {
                        new SpinningCrateUserInterface(plugin, crateData).open(player);
                    }
                    break;
            }

        });
        events.listen(ID, CrateKeySpendEvent.class, event -> {
            Player player = event.getPlayer();
            CrateData purchasedCrate = event.getPurchasedCrate();
            String purchasedCrateName = purchasedCrate.name();

            UserDataKey<Integer> crateKeyUserData = isStandardType(purchasedCrateName) ? getStandardType(purchasedCrateName) : template(purchasedCrateName);
            int playerKeyAmount = crateKeyUserData.get(player.getUniqueId());

            if (playerKeyAmount <= 0) {
                player.sendRichMessage("<gray>You <red>don't</red> have any <green>%s Crate</green> keys!".formatted(Strings.fixCase(purchasedCrateName)));
                player.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 1f, 2f));
                event.setCancelled(true);
                return;
            }
            crateKeyUserData.set(player.getUniqueId(), playerKeyAmount - 1);
        });

        return Result.success();
    }

    @Override
    public void unload() {
    }


    private void giveUserReward(PlayerInventory playerInventory, CrateData crateData) {
        ArrayList<ItemStack> items = new ArrayList<>(crateData.items().values());
        playerInventory.addItem(items.get(ThreadLocalRandom.current().nextInt(items.size())));
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public Set<String> dependencies() {
        return DEPENDENCIES;
    }

    @Override
    public int priority() {
        return PRIORITY;
    }
}
