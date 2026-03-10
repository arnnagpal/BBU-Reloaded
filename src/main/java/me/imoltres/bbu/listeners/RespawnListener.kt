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

        // if the team has a beacon, teleport them to it, otherwise teleport them to their cage
        // for some reason hasBeacon returns true in grace period... what idiot coded that (me)
        if (team!!.hasBeacon() && team.beacon != null) {
            bukkitPlayer.teleport(
                team.beacon!!
                    .toWorldPosition(BBU.getInstance().game.overworld.name)
                    .toBukkitLocation()
                    .add(0.5, 0.5, 0.5)
            )
        } else {
            bukkitPlayer.teleport(team.cage!!.spawnPosition.toBukkitLocation())
        }
    }
}

// --------------------------------------------------------