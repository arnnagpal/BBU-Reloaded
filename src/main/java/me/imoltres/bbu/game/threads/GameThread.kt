package me.imoltres.bbu.game.threads

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.utils.DateUtils
import me.imoltres.bbu.utils.PlayerUtils
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

class GameThread(val game: Game) : Thread() {

    var tick: Int = 0

    private val shrinkTo = 250.0

    private var shrinking = false

    val teamCheckQueue = LinkedList<BBUTeam>()

    override fun run() {
        try {
            while (game.gameState != GameState.POST_GAME) {
                if (currentThread().isInterrupted) {
                    return
                }

                swapGameStateIfAvailable()
                checkTeams()

                when (game.gameState) {
                    GameState.PVP -> PlayerUtils.broadcastTitle(
                        "&cPvP is now enabled.",
                        "&7" + DateUtils.readableTime(BigDecimal(GameState.PVP_BORDER_SHRINK.startsAfterTick)) + " till border shrink."
                    )

                    GameState.PVP_BORDER_SHRINK -> {
                        if (!shrinking) {
                            val border = game.border
                            shrinkBorder(getBorderShrinkTime(border))
                            shrinking = !shrinking
                        }
                    }
                    else -> {}
                }

                tick++

                //sleep to tick to next second
                sleep(5)
            }
        } catch (e: InterruptedException) {
            return
        }
    }

    private fun swapGameStateIfAvailable() {
        val gameState = GameState.getGameStateFromTick(tick)
        if (game.gameState != gameState) {
            if (gameState != null) {
                game.gameState = gameState
            }
        }
    }

    private fun getBorderShrinkTime(border: Int): Int {
        var time = 0
        for (i in 1..6) {
            time += ((border / 200 - 2.5 * (0.3 + (i * 0.1))) * 60).toInt()
        }
        return time / 2
    }

    private fun shrinkBorder(seconds: Int) {
        PlayerUtils.broadcastTitle(
            "&cShrinking Border to $shrinkTo",
            "&7in" + DateUtils.readableTime(BigDecimal(seconds))
        )

        Bukkit.getScheduler().runTask(BBU.instance) { ->
            for (world in Bukkit.getWorlds()) {
                world.worldBorder.setSize(shrinkTo, seconds.toLong())
            }
        }
    }

    private fun checkTeams() {
        while (teamCheckQueue.peek() != null) {
            val team = teamCheckQueue.pop()
            println("&aChecking team '&" + team.colour.chatColor.char + team.colour.name + "&a'.")
            if (team.players.size == 0 && !team.hasBeacon()) {
                team.eliminate()
            }
        }
    }
}