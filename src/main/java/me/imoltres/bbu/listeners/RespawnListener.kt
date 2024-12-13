package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.player.BBUPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerRespawnEvent


// @sinender (taken from the closed PR)
// --------------------------------------------------------
class RespawnListener : Listener {
    @EventHandler
    fun onRespawn(e: PlayerRespawnEvent) {
        val bukkitPlayer: Player = e.player
        val player: BBUPlayer = BBU.getInstance().playerController.getPlayer(bukkitPlayer.uniqueId) ?: return
        if (player.team == null) return
        val team = player.team
        if (bukkitPlayer.respawnLocation != null) {
            bukkitPlayer.teleport(bukkitPlayer.respawnLocation!!)
            return
        }


        if (team!!.hasBeacon()) {
            bukkitPlayer.teleport(team.beacon!!.toWorldPosition("world").toBukkitLocation().add(0.5, 0.5, 0.5))
        } else {
            bukkitPlayer.teleport(team.cage!!.spawnPosition.toBukkitLocation())
        }
    }
}

// --------------------------------------------------------