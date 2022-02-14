package me.imoltres.bbu.utils;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Used to primarily broadcast titles to all players
 */
public class PlayerUtils {

    /**
     * Broadcast a {@link net.kyori.adventure.title.Title} to all players
     *
     * @param title    big title text
     * @param subTitle small title text
     */
    public static void broadcastTitle(String title, String subTitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(CC.translate(title), CC.translate(subTitle)));
        }
    }

}
