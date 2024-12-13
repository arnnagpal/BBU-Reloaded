package me.imoltres.bbu.utils.nametag;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@Getter
public class NametagListeners implements Listener {

    private NametagHandler handler;

    public NametagListeners(NametagHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        System.out.println("Player joined, adding to nametag board");
        getHandler().getBoards().putIfAbsent(event.getPlayer().getUniqueId(), new NametagBoard(event.getPlayer(), getHandler()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        System.out.println("Player quit, removing from nametag board");
        getHandler().getBoards().remove(event.getPlayer().getUniqueId());
        event.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

}
