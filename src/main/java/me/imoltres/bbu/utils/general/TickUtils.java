package me.imoltres.bbu.utils.general;

import me.imoltres.bbu.utils.CC;
import org.bukkit.Bukkit;

/**
 * Used to replicate the TPS display from the default
 * /tps numbers.
 */
public class TickUtils {

    /**
     * Retrieve the formatted TPS at the most recent one
     *
     * @return formatted TPS
     */
    public static String getTPS() {
        return formatTPS(Bukkit.getTPS()[0]);
    }

    /**
     * Format a double as a bukkit TPS value
     *
     * @param tps double
     * @return formatted TPS
     */
    private static String formatTPS(double tps) {
        return ((tps > 18.0) ? CC.GREEN : ((tps > 16.0) ? CC.YELLOW : CC.RED)) + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
    }

}
