package me.imoltres.bbu.game.threads

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.game.ShrinkPhase
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.general.DateUtils
import me.imoltres.bbu.utils.general.PlayerUtils
import me.imoltres.bbu.utils.item.ItemConstants
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToInt
import kotlin.times

/**
 * Main game thread loop, swaps the game states, checks the teams, and ticks the game.
 */
class GameThread(val game: Game) : BukkitRunnable() {

    var tick: Int = 0

    private var shrinking = false
    private var pvp = false

    @Volatile
    var started = false

    val teamCheckQueue = LinkedList<BBUTeam>()

    var timeToNextShrink = 0

    /**
     * Game loop
     */
    override fun run() {
        started = true
        if (game.gameState == GameState.POST_GAME) return
        if (isCancelled) return

        swapGameStateIfAvailable()
        checkTeams()
        checkWinConditions()

        when (game.gameState) {
            GameState.PVP -> run pvpBranch@{
                if (pvp) return@pvpBranch

                PlayerUtils.broadcastTitle(
                    "&cPvP is now enabled.",
                    "&7" + DateUtils.readableTime(BigDecimal(GameState.PVP_BORDER_SHRINK.startTime - (tick / 20))) + " till border shrink."
                )

                // take away the beacon if they haven't placed it yet
                for (team in game.getTeams(false))
                    for (player in team.players) {
                        val bukkitPlayer = Bukkit.getPlayer(player.uniqueId) ?: continue
                        if (!bukkitPlayer.inventory.contains(ItemConstants.TEAM_BEACON.build())) continue

                        bukkitPlayer.inventory.removeItemAnySlot(ItemConstants.TEAM_BEACON.build())
                        bukkitPlayer.sendMessage(CC.translate("&cYou have lost your beacon because you didn't place it in time."))
                    }

                pvp = !pvp
            }

            GameState.PVP_BORDER_SHRINK -> run shrinkBranch@{
                if (timeToNextShrink > 0) {
                    timeToNextShrink--
                    game.border = game.overworld.worldBorder.size.roundToInt()
                    return@shrinkBranch
                }

                game.currentShrinkPhase = game.nextShrinkPhase // move to next phase
                val shrinkPhase = game.currentShrinkPhase ?: run {
                    game.gameState = GameState.DEATHMATCH
                    return@shrinkBranch
                }

                shrinkBorder(shrinkPhase)

                // shrink will take shrinkPhase.length * 20 ticks
                val shrinkTicks = shrinkPhase.length * 20
                val nextShrinkDelayTicks = game.nextShrinkPhase?.time?.times(20) ?: 0
                timeToNextShrink = shrinkTicks + nextShrinkDelayTicks
            }

            else -> {}
        }

        // @sinender (taken from the closed PR)
        //check and see if any teams beacons are out of the border, This cannot be changed to a listener because there is no event for border shrink
        //I will however make this run every 5 seconds instead of every second
        if (tick % 120 == 0) {
            for (team in game.getTeams(true)) {
                val world = game.overworld
                val beaconLoc = team.beacon!!.toWorldPosition(game.overworld.name).toBukkitLocation()
                if (team.beacon == null) continue;

                // if the beacon is outside of the world border, break it and eliminate the team
                if (!world.worldBorder.isInside(beaconLoc)) {
                    team.beacon!!.toWorldPosition(game.overworld.name).block.type =
                        Material.AIR
                    team.beacon = null
                    Bukkit.broadcast(
                        CC.translate("${team.getRawDisplayName()}&c's beacon has been destroyed because it was out of the border.")
                    )
                }
            }
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
     * Shrink the border globally
     * @param shrinkPhase the phase to shrink the border to
     */
    private fun shrinkBorder(shrinkPhase: ShrinkPhase) {
        PlayerUtils.broadcastTitle(
            "&cBorder Shrinking!",
            "&7${shrinkPhase.size} over" + DateUtils.readableTime(BigDecimal(shrinkPhase.length.toLong()))
        )

        Bukkit.getScheduler().runTask(BBU.getInstance()) { ->
            for (world in Bukkit.getWorlds()) {
                world.worldBorder.changeSize(shrinkPhase.size.toDouble(), shrinkPhase.length * 20L)
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
                .sendMessage(CC.translate("&aChecking team '&" + team.colour.chatColor.code + team.colour.name + "&a'."))
            if (team.players.size == 0 && !team.hasBeacon()) {
                Bukkit.getConsoleSender()
                    .sendMessage(CC.translate("&cEliminated team '&" + team.colour.chatColor.code + team.colour.name + "&a'."))
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