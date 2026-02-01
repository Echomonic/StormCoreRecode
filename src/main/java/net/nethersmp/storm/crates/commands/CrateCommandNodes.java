package net.nethersmp.storm.crates.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.triumphteam.gui.guis.Gui;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.nethersmp.storm.crates.CrateItem;
import net.nethersmp.storm.crates.api.CrateData;
import net.nethersmp.storm.crates.storage.CratesDataHandler;
import net.nethersmp.storm.crates.storage.CratesStorage;
import net.nethersmp.storm.crates.ui.CrateUserInterface;
import net.nethersmp.storm.utilities.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;
import static net.nethersmp.storm.brigadier.SetBrigadierSuggestion.keySuggestion;

@UtilityClass
public class CrateCommandNodes {

    private final Set<String> COLORS = Arrays.stream(Material.values()).filter(material -> material.name().endsWith("SHULKER_BOX") && !material.isLegacy()).map(filtered -> {
        String name = filtered.name();
        if (name.equalsIgnoreCase("SHULKER_BOX")) return "none";
        return name.replace("_SHULKER_BOX", "").toLowerCase();
    }).collect(Collectors.toSet());

    public LiteralCommandNode<CommandSourceStack> make(CratesStorage storage) {
        return literal("make").requires(source -> source.getSender().hasPermission("stormcore.crates.make"))
                .then(argument("name", word()).then(argument("color", string()).suggests(keySuggestion(COLORS)).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();

                    if (!(sender instanceof Player player)) {
                        sender.sendRichMessage("<red>You must be a player to use this command.");
                        return 0;
                    }
                    Inventory playerInventory = player.getInventory();

                    if (playerInventory.firstEmpty() == -1) {
                        player.sendRichMessage("<red>Your inventory is full!");
                        return 0;
                    }

                    String crateName = getString(context, "name");

                    if (storage.get(crateName) != null) {
                        player.sendRichMessage("<red>This crate already exists. Run /crates give " + crateName);
                        return 0;
                    }
                    String crateColor = getString(context, "color");
                    CrateData crateData = new CrateData(crateName, crateColor, new ConcurrentHashMap<>());
                    ItemStack crateItem = new CrateItem(crateData).toItemStack();

                    if (crateItem == null) {
                        player.sendRichMessage("<gray><red>Couldn't</red> find the <rainbow>color</rainbow> you provided!");
                        return 0;
                    }
                    playerInventory.addItem(crateItem);

                    player.sendRichMessage("<gray><green>Successfully</green> created the <yellow>" + Strings.fixCase(crateName) + " Crate</yellow>.");
                    player.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 2f));
                    storage.add(crateName, crateData);
                    player.updateCommands();
                    return 1;
                })))

                .build();
    }

    public LiteralCommandNode<CommandSourceStack> open(CratesDataHandler handler, CratesStorage storage) {
        return literal("open").requires(source -> source.getSender().hasPermission("stormcore.crates.open"))
                .then(argument("crate-name", word()).suggests(keySuggestion(storage.keys())).executes(context -> {
                    String crateName = getString(context, "crate-name");
                    CommandSender sender = context.getSource().getSender();
                    Bukkit.dispatchCommand(sender, "crates open " + crateName + " false");
                    return 1;
                }).then(argument("edit", bool()).requires(source -> source.getSender().hasPermission("stormcore.crates.open.edit")).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();

                    if (!(sender instanceof Player player)) {
                        sender.sendRichMessage("<red>You must be a player to use this command.");
                        return 0;
                    }
                    String crateName = getString(context, "crate-name");
                    CrateData crateData = storage.get(crateName);

                    if (crateData == null) {
                        player.sendRichMessage("<red>Couldn't find the specified crate.");
                        return 0;
                    }

                    Gui crateUserInterface = new CrateUserInterface(handler, storage, crateData, getBool(context, "edit"));
                    crateUserInterface.open(player);
                    return 1;
                })))
                .build();
    }

    public LiteralCommandNode<CommandSourceStack> edit() {

        return literal("edit").requires(source -> source.getSender().hasPermission("stormcore.crates.open.edit")).then(argument("crate-name", word()).executes(context -> {
            CommandSender sender = context.getSource().getSender();
            Bukkit.dispatchCommand(sender, "crates open %s true".formatted(getString(context, "crate-name")));
            return 1;
        })).build();
    }

    public LiteralCommandNode<CommandSourceStack> give(CratesStorage storage) {
        return literal("give").requires(source -> source.getSender().hasPermission("stormcore.crates.give"))
                .then(argument("crate-name", word()).suggests(keySuggestion(storage.keys())).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();

                    if (!(sender instanceof Player player)) {
                        sender.sendRichMessage("<red>You must be a player to use this command.");
                        return 0;
                    }

                    Inventory playerInventory = player.getInventory();

                    if (playerInventory.firstEmpty() == -1) {
                        player.sendRichMessage("<red>Your inventory is full!");
                        return 0;
                    }

                    String crateName = getString(context, "crate-name");
                    CrateData crateData = storage.get(crateName);

                    if (crateData == null) {
                        player.sendRichMessage("<red>Couldn't find the specified crate.");
                        return 0;
                    }
                    ItemStack crateItem = new CrateItem(crateData).toItemStack();
                    playerInventory.addItem(crateItem);
                    player.sendRichMessage("<gray><green>Gave</green> you the <yellow>%s Crate</yellow> item.".formatted(Strings.fixCase(crateName)));
                    return 1;
                })).build();
    }

    public LiteralCommandNode<CommandSourceStack> remove(CratesDataHandler handler, CratesStorage storage) {

        return literal("delete").requires(source -> source.getSender().hasPermission("stormcore.crates.remove")).then(argument("crate-name", word()).suggests(keySuggestion(storage.keys())).executes(context -> {
                    CommandSourceStack source = context.getSource();
                    CommandSender sender = source.getSender();

                    String crateName = getString(context, "crate-name");

                    CrateData crateData = storage.get(crateName);

                    if (crateData == null) {
                        sender.sendRichMessage("<red>This crate doesn't exist, there for it can't be removed.");
                        sender.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 1f, 1.5f));
                        return 0;
                    }
                    if (handler.delete(crateData.name())) {
                        sender.sendRichMessage("<green>Successfully</green> <red>deleted</red> <gray>the <yellow>%s Crate</yellow>.".formatted(Strings.fixCase(crateName)));
                        sender.playSound(Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1f, 2f));
                    } else {
                        sender.sendRichMessage("<gray><red>Failed</red> to <red>delete</red> the <yellow>%s Crate</yellow>. <dark_gray>(DOES NOT EXIST)".formatted(Strings.fixCase(crateName)));
                        sender.playSound(Sound.sound(Key.key("entity.enderman.teleport"), Sound.Source.MASTER, 1f, 2f));
                    }
                    if (sender instanceof Player player)
                        player.updateCommands();
                    return 1;
                }))
                .build();
    }
}
