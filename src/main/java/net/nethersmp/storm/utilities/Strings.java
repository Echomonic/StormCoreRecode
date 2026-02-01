package net.nethersmp.storm.utilities;

import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

@UtilityClass
public class Strings {

    public String small(String string) {

        return string.replace("A", "ᴀ")
                .replace("B", "ʙ")
                .replace("C", "ᴄ")
                .replace("D", "ᴅ")
                .replace("E", "ᴇ")
                .replace("F", "ꜰ")
                .replace("G", "ɢ")
                .replace("H", "ʜ")
                .replace("I", "ɪ")
                .replace("J", "ᴊ")
                .replace("K", "ᴋ")
                .replace("L", "ʟ")
                .replace("M", "ᴍ")
                .replace("N", "ɴ")
                .replace("O", "ᴏ")
                .replace("P", "ᴘ")
                .replace("Q", "ǫ")
                .replace("R", "ʀ")
                .replace("S", "ѕ")
                .replace("T", "ᴛ")
                .replace("U", "ᴜ")
                .replace("V", "ᴠ")
                .replace("W", "ᴡ")
                .replace("X", "х")
                .replace("Y", "ʏ")
                .replace("Z", "ᴢ");
    }


    public String fixCase(String text) {
        String[] args = text.split("_");
        StringBuilder builder = new StringBuilder();
        for (String arg : args) {
            String upperSegment = arg.substring(0, 1).toUpperCase();
            String lowerSegment = arg.substring(1).toLowerCase();

            builder.append(upperSegment).append(lowerSegment).append(" ");
        }
        return builder.toString().trim();
    }

    public String date(long time) {
        time -= System.currentTimeMillis();

        var days = TimeUnit.MILLISECONDS.toDays(time);
        var hours = TimeUnit.MILLISECONDS.toHours(time) % 24;
        var minutes = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        var seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

        StringBuilder sb = new StringBuilder();

        if (days > 0)
            sb.append(days).append("d ");
        if (hours > 0)
            sb.append(hours).append("h ");
        if (minutes > 0)
            sb.append(minutes).append("m ");

        sb.append(seconds).append("s");
        return sb.toString();
    }


}
