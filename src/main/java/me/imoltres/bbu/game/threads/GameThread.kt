package me.imoltres.bbu.game.threads

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.general.DateUtils
import me.imoltres.bbu.utils.general.PlayerUtils
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.math.BigDecimal
import java.util.*

/**
 * Main game thread loop, swaps the game states, checks the teams, and ticks the game.
 */
class GameThread(val game: Game) : BukkitRunnable() {

    var tick: Int = 0

    //TODO: replace with some sort of deathmatch feature
    private val shrinkTo = 75.0

    private var shrinking = false
    private var pvp = false

    var started = false

    val teamCheckQueue = LinkedList<BBUTeam>()

    /**
     * Game loop
     */
    override fun run() {
        started = true
        if (game.gameState == GameState.POST_GAME)
            return
        if (isCancelled) {
            return
        }

        swapGameStateIfAvailable()
        checkTeams()
        checkWinConditions()

        when (game.gameState) {
            GameState.PVP -> {
                if (!pvp) {
                    PlayerUtils.broadcastTitle(
                        "&cPvP is now enabled.",
                        "&7" + DateUtils.readableTime(BigDecimal(GameState.PVP_BORDER_SHRINK.startsAfterTick - (tick / 20))) + " till border shrink."
                    )

                    pvp = !pvp
                }
            }

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
        return (border / 6) * 60
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

    private fun checkWinConditions() {
        val teamsLeft = BBU.getInstance().game.getAliveTeams().toList()

        if (teamsLeft.size == 1) {
            Bukkit.broadcast(CC.translate("&a" + teamsLeft[0].getRawDisplayName() + " &ahas won the BBU!! GGS"))
            BBU.getInstance().game.stopGame(teamsLeft[0])
        }

    }
}