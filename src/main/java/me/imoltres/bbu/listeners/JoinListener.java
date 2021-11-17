package me.imoltres.bbu.listeners;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.utils.CC;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class JoinListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPreJoin(AsyncPlayerPreLoginEvent e) {
        UUID uniqueId = e.getUniqueId();
        String name = e.getName();

        BBU.getInstance().getPlayerController().createPlayer(uniqueId, name);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.joinMessage(CC.translate("&7[&a+&7] &7" + e.getPlayer().getName()));
    }

}
