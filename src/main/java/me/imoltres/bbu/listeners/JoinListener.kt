package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.game.events.player.BBUPlayerScoreboardApplyEvent
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter
import me.imoltres.bbu.scoreboard.impl.MainScoreboard
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.config.Messages
import me.imoltres.bbu.utils.json.GsonFactory
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class JoinListener : Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    fun onPreJoin(e: AsyncPlayerPreLoginEvent) {
        if (!BBU.getInstance().isJoinable) {
            e.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                CC.translate("&cServer isn't finished setting up\n\n&cTry again later.")
            )
            return
        }

        val uniqueId = e.uniqueId
        val name = e.name
        val player = BBU.getInstance().playerController.createPlayer(uniqueId, name)
        if (player.eliminated && !MainConfig.SPECTATE_AFTER_DEATH)
            e.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_OTHER,
                CC.translate(Messages.FINAL_DEATH.toString())
            )
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onJoin(e: PlayerJoinEvent) {
        if (!BBU.getInstance().isJoinable) {
            e.joinMessage(CC.translate(""))
            return
        }

        val player = e.player
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId);

        e.joinMessage(CC.translate("&7[&a+&7] &7" + e.player.name))

        if (BBU.getInstance().game.gameState == GameState.LOBBY) {
            player.gameMode = GameMode.ADVENTURE
            player.inventory.clear()
            player.equipment.clear()
            player.activePotionEffects.forEach { potionEffect -> player.removePotionEffect(potionEffect.type) }
            player.addPotionEffect(PotionEffect(PotionEffectType.SATURATION, 999999, 0, false, false))
            player.updateInventory()
            player.exp = 0.0F
            player.level = 0
            player.saturation = 20.0F
            player.foodLevel = 20
            player.health = 20.0
        } else {
            if (!player.isOp && !bbuPlayer.eliminated)
                player.gameMode = GameMode.SURVIVAL

            bbuPlayer.allowMovement()
        }

        try {
            if (BBU.getInstance().game.gameState == GameState.LOBBY) {
                player.teleportAsync(
                    GsonFactory.getCompactGson().fromJson(MainConfig.LOBBY_SPAWN, WorldPosition::class.java)
                        .toBukkitLocation()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Failed to teleport player ${player.name}. Invalid lobby-spawn?")
        }

        val event = BBUPlayerScoreboardApplyEvent(
            bbuPlayer,
            MainScoreboard::class.java
        )
        event.callEvent()

        if (event.isCancelled)
            return

        println("Applied scoreboard to ${player.name}: ${BBUScoreboardAdapter.display(event.scoreboard, player)}")
    }

    @EventHandler
    fun onQuit(e: PlayerQuitEvent) {
        val player = e.player
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
        e.quitMessage(CC.translate("&7[&c-&7] &7" + e.player.name))

        BBU.getInstance().scoreboard.cleanup(bbuPlayer)
    }
}