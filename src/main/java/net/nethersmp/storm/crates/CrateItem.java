package net.nethersmp.storm.crates;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.nethersmp.storm.crates.api.CrateData;
import net.nethersmp.storm.utilities.Components;
import net.nethersmp.storm.utilities.Strings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class CrateItem {

    private final CrateData crate;

    public static Material getCrateClassColor(CrateData crateData) {
        return crateData.color().equals("none") ? Material.GLASS_PANE : Material.getMaterial(crateData.color().toUpperCase() + "_STAINED_GLASS_PANE");
    }

    public ItemStack toItemStack() {
        Material material = findMaterial();

        if (material == null) {
            return null;
        }
        return ItemBuilder.from(material)
                .name(Component.text(Strings.fixCase(crate.name()), TextColor.fromHexString("#00ff6c")).decoration(TextDecoration.ITALIC, false))
                .lore(Components.color(Strings.small("<gray>PLACE THIS <yellow>CRATE</yellow> DOWN AND")),
                        Components.color(Strings.small("<gray>PUT <aqua>ITEMS</aqua> IN IT.</gray>")))
                .setNbt("crate-id", crate.name())
                .build();
    }

    private Material findMaterial() {
        String color = crate.color().toLowerCase();

        if (color.equalsIgnoreCase("none"))
            return Material.SHULKER_BOX;

        return Material.getMaterial(color.toUpperCase() + "_SHULKER_BOX");
    }

}
