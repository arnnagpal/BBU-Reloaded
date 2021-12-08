package me.imoltres.bbu.utils;

import org.bukkit.Bukkit;

public class TickUtils {

    public static String getTPS() {
        return formatTPS(Bukkit.getTPS()[0]);
    }

    private static String formatTPS(double tps) {
        return ((tps > 18.0) ? CC.GREEN : ((tps > 16.0) ? CC.YELLOW : CC.RED)) + ((tps > 20.0) ? "*" : "") + Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
    }

}
