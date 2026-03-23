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
        val team = player.team
        if (team == null) return
        if (bukkitPlayer.respawnLocation != null) {
            // dont handle teleporting if the respawn location is already set
            // player set their respawn location to a bed or something, so we should respect that
            return
        }

        // if the team has a beacon, teleport them to it, otherwise teleport them to their cage
        // for some reason hasBeacon returns true in grace period... what idiot coded that (me)
        if (team.cage != null) {
            e.respawnLocation = team.cage!!.spawnPosition.toBukkitLocation()
        }
    }
}

// --------------------------------------------------------