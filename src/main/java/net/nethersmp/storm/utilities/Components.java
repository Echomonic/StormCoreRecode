package net.nethersmp.storm.utilities;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

@UtilityClass
public class Components {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public Component color(String text) {
        return miniMessage.deserialize(text).decoration(TextDecoration.ITALIC, false);
    }

}
