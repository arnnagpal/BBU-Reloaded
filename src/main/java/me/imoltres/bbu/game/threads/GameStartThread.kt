package me.imoltres.bbu.game.threads

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.utils.CC
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRules
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitRunnable
import java.time.Duration

/**
 * Run before the [GameThread] thread, basically an async countdown
 */
class GameStartThread(val game: Game) : BukkitRunnable() {
    private var time = 3

    override fun run() {
        if (time > 0) {
            for (player in Bukkit.getOnlinePlayers()) {
                player.showTitle(
                    Title.title(
                        CC.translate("&c&l$time"), CC.translate(""), Title.Times.times(
                            Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500)
                        )
                    )
                )
                player.playSound(player.location, Sound.BLOCK_LEVER_CLICK, 1f, 1f)
            }

            time--
            return
        }

        for (player in Bukkit.getOnlinePlayers()) {
            player.gameMode = GameMode.SURVIVAL

            player.showTitle(
                Title.title(
                    CC.translate("&a&lGO!"), CC.translate("&7Good luck, have fun!"), Title.Times.times(
                        Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500)
                    )
                )
            )
            player.playSound(player.location, Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1f)
        }

        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            for (world in BBU.getInstance().game.worlds) {
                world.setGameRule(GameRules.ADVANCE_TIME, true)
            }
        })

        BBU.getInstance().cageController.deleteCages(game.overworld)
        game.thread.runTaskTimer(BBU.getInstance(), 0L, 1L)

        this.cancel() // stop this thread from running, the game thread will take over from here
    }
}
