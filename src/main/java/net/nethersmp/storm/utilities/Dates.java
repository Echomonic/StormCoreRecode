package net.nethersmp.storm.utilities;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Dates {


    public long segment(String timeSegment) {
        String[] words = timeSegment.replace(" ", ",").split(",");
        long years = 0L;
        long months = 0L;
        long days = 0L;
        long hours = 0L;
        long minutes = 0L;
        long seconds = 0L;

        for (String word : words) {
            long num = Long.parseLong(word.substring(0, word.length() - 1));
            char lastChar = word.charAt(word.length() - 1);
            switch (lastChar) {
                case 'y':
                    years = num * 31557600000L;
                    break;
                case 'M':
                    months = num * 2629800000L;
                    break;
                case 'd':
                    days = num * 86400000L;
                    break;
                case 'h':
                    hours = num * 3600000L;
                    break;
                case 'm':
                    minutes = num * 60000L;
                    break;
                case 's':
                    seconds = num * 1000L;
                    break;
            }
        }
        return System.currentTimeMillis() + (years + months + days + hours + minutes + seconds);
    }

}
