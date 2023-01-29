package me.imoltres.bbu.listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncChatEvent e) {
        Player p = e.getPlayer();
        Component msg = e.message();
        e.setCancelled(true);

        //TODO: add config / finish this
        String format = "";
        for (Player all : Bukkit.getOnlinePlayers()) {

        }
    }

}
