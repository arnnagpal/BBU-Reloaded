package me.imoltres.bbu.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Useful utility methods containing ways to convert time
 */
public class DateUtils {
    /**
     * Convert a big decimal to a readable time format
     *
     * @param time {@link java.math.BigDecimal} version of time
     * @return a readable time format
     */
    public static String readableTime(BigDecimal time) {
        String text = "";
        if (time.doubleValue() <= 60.0) {
            return text + " " + time + "s";
        }

        if (time.doubleValue() <= 3600.0) {
            int minutes = time.intValue() / 60;
            int seconds = time.intValue() % 60;
            DecimalFormat formatter = new DecimalFormat("00");
            return text + " " + formatter.format(minutes) + ":" + formatter.format(seconds);
        }

        return null;
    }
}
