package me.imoltres.bbu.utils;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlayerUtils {

    public static void broadcastTitle(String title, String subTitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(CC.translate(title), CC.translate(subTitle)));
        }
    }

}
