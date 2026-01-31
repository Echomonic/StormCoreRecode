package net.nethersmp.storm.permission;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UserRank(
        String prefix,
        @JsonProperty("color")
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        List<String> colors,
        List<String> permissions
) {

    public String getColor(double phase) {
        if (!isAnimated())
            return colors.getFirst();
        StringBuilder builder = new StringBuilder();
        colors.forEach(color -> builder.append(color).append(":"));

        builder.insert(0, "transition:");
        builder.append(phase);

        return builder.toString();
    }

    public String getColor() {
        return getColor(0.0);
    }

    public String getEndColor() {
        if (isAnimated())
            return "/transition";
        return "/" + getColor();
    }

    public String getFormattedPrefix() {
        return "<" + getColor() + ">" + prefix() + "<" + getEndColor() + ">";
    }

    public boolean isAnimated() {
        return this.colors.size() > 1;
    }

}
