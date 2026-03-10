package me.imoltres.bbu.utils.general;

import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.LegacyColor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
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
        return CC.translateLegacy(formatTPS(Bukkit.getTPS()[0]));
    }

    /**
     * Format a double as a bukkit TPS value
     *
     * @param tps double
     * @return formatted TPS
     */
    private static TextComponent formatTPS(double tps) {
        TextComponent tpsComponent = Component.text(String.format("%.2f", Math.min(tps, 20.0)));
        if (tps > 18.0) {
            tpsComponent = tpsComponent.color(LegacyColor.GREEN.asTextColor());
        } else if (tps > 16.0) {
            tpsComponent = tpsComponent.color(LegacyColor.YELLOW.asTextColor());
        } else {
            tpsComponent = tpsComponent.color(LegacyColor.RED.asTextColor());
        }

        if (tps > 20.0) {
            tpsComponent = Component.text("*").color(LegacyColor.GREEN.asTextColor()).append(tpsComponent);
        }

        return tpsComponent;
    }

}
