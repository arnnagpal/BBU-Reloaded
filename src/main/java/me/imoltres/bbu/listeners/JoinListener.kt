package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.scoreboard.impl.MainScoreboard
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.Messages
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPreJoin(e: AsyncPlayerPreLoginEvent) {
        if (!BBU.instance.joinable) {
            e.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                CC.translate("&cServer isn't finished setting up\n\n&cTry again later.")
            )
            return
        }

        val uniqueId = e.uniqueId
        val name = e.name
        val player = BBU.instance.playerController.createPlayer(uniqueId, name)
        if (player.eliminated)
            e.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                CC.translate(Messages.FINAL_DEATH.toString())
            )
    }

    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player
        e.joinMessage(CC.translate("&7[&a+&7] &7" + e.player.name))

        MainScoreboard(player)
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        val bbuPlayer = BBU.instance.playerController.getPlayer(player.uniqueId)
        e.quitMessage(CC.translate("&7[&c-&7] &7" + e.player.name))

        BBU.instance.scoreboard.cleanup(bbuPlayer)
    }
}