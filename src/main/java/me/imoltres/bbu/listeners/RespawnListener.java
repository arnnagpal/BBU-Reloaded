package me.imoltres.bbu.listeners;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnListener implements Listener {
    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player bukkitPlayer = e.getPlayer();
        BBUPlayer player = BBU.getInstance().getPlayerController().getPlayer(bukkitPlayer.getUniqueId());
        if (player == null)
            return;
        if (player.getTeam() == null)
            return;
        BBUTeam team = player.getTeam();
        if (bukkitPlayer.getBedSpawnLocation() != null) {
            bukkitPlayer.teleport(bukkitPlayer.getBedSpawnLocation());
            return;
        }
        if (team.hasBeacon()) {
            bukkitPlayer.teleport(team.getBeacon().toWorldPosition("world").toBukkitLocation().add(0.5, 0.5, 0.5));
        } else {
            bukkitPlayer.teleport(team.getCage().getSpawnPosition().toBukkitLocation());
        }
    }
}
