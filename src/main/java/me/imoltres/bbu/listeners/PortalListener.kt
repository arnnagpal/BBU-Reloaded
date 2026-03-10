package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerPortalEvent

class PortalListener : Listener {

    @EventHandler
    fun onPlayerPortal(e: PlayerPortalEvent) {
        val to = e.to

        when (to.world.environment) {
            World.Environment.NETHER -> {
                e.to = Location(
                    BBU.getInstance().game.nether,
                    to.x,
                    to.y,
                    to.z,
                    to.yaw,
                    to.pitch
                )
            }

            World.Environment.NORMAL -> {
                e.to = Location(
                    BBU.getInstance().game.overworld,
                    to.x,
                    to.y,
                    to.z,
                    to.yaw,
                    to.pitch
                )
            }

            World.Environment.THE_END -> {
                e.to = Location(
                    BBU.getInstance().game.end,
                    to.x,
                    to.y,
                    to.z,
                    to.yaw,
                    to.pitch
                )
            }

            else -> {
                // log unexpected environment
                BBU.getInstance().logger.severe("Unexpected world environment: ${to.world.environment}")
            }
        }
    }
}