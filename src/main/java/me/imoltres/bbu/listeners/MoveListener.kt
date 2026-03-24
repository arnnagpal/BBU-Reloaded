package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.json.GsonFactory
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class MoveListener : Listener {

    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        val player = e.player
        val world = player.world
        val spawnWorld = BBU.getInstance().game.spawnWorld

        if (world.uid != spawnWorld.uid) return

        if (player.y > MainConfig.lobbyYMin) return

        player.teleportAsync(
            GsonFactory.getCompactGson().fromJson(MainConfig.lobbySpawn, WorldPosition::class.java)
                .toBukkitLocation()
        )
    }

}