package me.imoltres.bbu.game.threads

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.DateUtils
import me.imoltres.bbu.utils.PlayerUtils
import org.bukkit.Bukkit
import java.math.BigDecimal
import java.util.*

/**
 * Main game thread loop, swaps the game states, checks the teams, and ticks the game.
 */
class GameThread(val game: Game) : Thread() {

    var tick: Int = 0

    private val shrinkTo = 250.0

    private var shrinking = false

    val teamCheckQueue = LinkedList<BBUTeam>()

    /**
     * Game loop
     */
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
                sleep(50)
            }
        } catch (e: InterruptedException) {
            return
        }
    }

    /**
     * Swap the game state (if it's possible)
     */
    private fun swapGameStateIfAvailable() {
        val gameState = GameState.getGameStateFromTick(tick)
        if (game.gameState != gameState) {
            if (gameState != null) {
                game.gameState = gameState
            }
        }
    }

    /**
     * Get the amount of time that the border should shrink for
     * 
     * based on the size of the border
     */
    private fun getBorderShrinkTime(border: Int): Int {
        var time = 0
        for (i in 1..6) {
            time += ((border / 200 - 2.5 * (0.3 + (i * 0.1))) * 60).toInt()
        }
        return time / 2
    }

    /**
     * Shrink the border globally
     * @param seconds seconds to shrink it for
     */
    private fun shrinkBorder(seconds: Int) {
        PlayerUtils.broadcastTitle(
            "&cShrinking Border to $shrinkTo",
            "&7in" + DateUtils.readableTime(BigDecimal(seconds))
        )

        Bukkit.getScheduler().runTask(BBU.getInstance()) { ->
            for (world in Bukkit.getWorlds()) {
                world.worldBorder.setSize(shrinkTo, seconds.toLong())
            }
        }
    }

    /**
     * Goes through the queue periodically and determines if a team should be eliminated or not.
     */
    private fun checkTeams() {
        while (teamCheckQueue.peek() != null) {
            val team = teamCheckQueue.pop()
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("&aChecking team '&" + team.colour.chatColor.char + team.colour.name + "&a'."))
            if (team.players.size == 0 && !team.hasBeacon()) {
                Bukkit.getConsoleSender()
                    .sendMessage(CC.translate("&cEliminated team '&" + team.colour.chatColor.char + team.colour.name + "&a'."))
                Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
                    team.eliminate(true)
                })
            }
        }
    }
}