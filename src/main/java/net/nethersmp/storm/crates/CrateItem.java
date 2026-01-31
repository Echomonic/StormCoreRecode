package net.nethersmp.storm.crates;

import dev.triumphteam.gui.builder.item.ItemBuilder;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.nethersmp.storm.StormPlugin;
import net.nethersmp.storm.crates.api.CrateData;
import net.nethersmp.storm.utilities.Strings;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@RequiredArgsConstructor
public class CrateItem {

    private final CrateData crate;
    private final StormPlugin plugin;


    public ItemStack toItemStack() {
        Material material = findMaterial();

        return ItemBuilder.from(material)
                .name(Component.text(Strings.fixCase(crate.name()), TextColor.fromHexString("#00ff6c")))
                .lore(MiniMessage.miniMessage().deserialize(Strings.small("<gray>PLACE THIS <yellow>CRATE</yellow> DOWN AND\n<gray>PUT <aqua>ITEMS</aqua> IN IT.</gray>")))
                .setNbt("crate-id", crate.name())
                .build();
    }

    private Material findMaterial() {
        String color = crate.color().toLowerCase();

        if (color.equalsIgnoreCase("none"))
            return Material.GLASS;

        return Material.getMaterial(color.toUpperCase() + "_SHULKER_BOX");
    }

}
