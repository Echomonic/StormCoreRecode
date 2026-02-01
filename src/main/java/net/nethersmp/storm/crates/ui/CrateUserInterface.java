package net.nethersmp.storm.crates.ui;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import dev.triumphteam.gui.components.GuiContainer;
import dev.triumphteam.gui.components.InteractionModifier;
import dev.triumphteam.gui.components.util.Legacy;
import dev.triumphteam.gui.guis.Gui;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.nethersmp.storm.crates.CrateItem;
import net.nethersmp.storm.crates.api.CrateData;
import net.nethersmp.storm.crates.storage.CratesDataHandler;
import net.nethersmp.storm.crates.storage.CratesStorage;
import net.nethersmp.storm.utilities.Components;
import net.nethersmp.storm.utilities.Strings;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class CrateUserInterface extends Gui {


    public CrateUserInterface(CratesDataHandler cratesDataHandler, CratesStorage storage, CrateData crateData, boolean edit) {
        super(new GuiContainer.Chest(Component.text(Strings.fixCase(crateData.name()) + " Crate"),
                (title, owner, rows) ->
                        Bukkit.createInventory(owner, rows, Legacy.SERIALIZER.serialize(title)),
                3
        ), edit ? Set.of() : Set.of(InteractionModifier.values()));
        loadItems(crateData, edit);
        setCloseGuiAction(event -> {
            if (!edit) return;
            Player player = (Player) event.getPlayer();
            ConcurrentMap<Integer, ItemStack> modifiedItems = new ConcurrentHashMap<>();
            Inventory inventory = event.getInventory();

            for (int slot = 0; slot < inventory.getSize(); slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item == null) continue;
                modifiedItems.put(slot, item);
            }
            crateData.items(modifiedItems);
            System.out.println(crateData.items());
            storage.add(crateData.name(), crateData);

            long time = System.currentTimeMillis();
            cratesDataHandler.write(crateData);
            player.sendRichMessage("<gray><green>Successfully saved</green> your changes!</gray> <yellow>%sms</yellow>".formatted(System.currentTimeMillis() - time));
        });

    }

    private void loadItems(CrateData crateData, boolean edit) {
        ConcurrentMap<Integer, ItemStack> items = crateData.items();
        if (!edit) {
            this.getFiller().fill(ItemBuilder.from(CrateItem.getCrateClassColor(crateData))
                    .asGuiItem());
            if (items.isEmpty()) {
                setItem(13, ItemBuilder.from(Material.ENDER_CHEST)
                        .name(Component.text("No items available!", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false))
                        .lore(Components.color(Strings.small("<gray>THIS <yellow>CRATE</yellow> HAS NO</gray>")),
                                Components.color(Strings.small("<gray>AVAILABLE <aqua>ITEMS</aqua> TO VIEW</gray>")))
                        .asGuiItem());
                return;
            }
        }
        for (Map.Entry<Integer, ItemStack> stackEntry : items.entrySet()) {
            setItem(stackEntry.getKey(), ItemBuilder.from(stackEntry.getValue()).asGuiItem());
        }
    }
}
