package me.imoltres.bbu.game.threads

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.game.ShrinkPhase
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.general.DateUtils
import me.imoltres.bbu.utils.general.PlayerUtils
import me.imoltres.bbu.utils.item.ItemConstants
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.scheduler.BukkitRunnable
import java.math.BigDecimal
import java.util.*
import kotlin.math.roundToInt

/**
 * Main game thread loop, swaps the game states, checks the teams, and ticks the game.
 */
class GameThread(val game: Game) : BukkitRunnable() {

    var tick: Int = 0

    private var shrinking = false
    private var pvp = false

    var started = false

    val teamCheckQueue = LinkedList<BBUTeam>()

    var shrinkingTime = 0

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
                        "&7" + DateUtils.readableTime(BigDecimal(GameState.PVP_BORDER_SHRINK.startTime - (tick / 20))) + " till border shrink."
                    )

                    // take away the beacon if they haven't placed it yet
                    for (team in game.getTeams(false)) {
                        for (player in team.players) {
                            val bukkitPlayer = Bukkit.getPlayer(player.uniqueId) ?: continue

                            if (bukkitPlayer.inventory.contains(ItemConstants.TEAM_BEACON)) {
                                bukkitPlayer.inventory.removeItemAnySlot(ItemConstants.TEAM_BEACON)
                                bukkitPlayer.sendMessage(CC.translate("&cYou have lost your beacon because you didn't place it in time."))
                            }
                        }
                    }

                    pvp = !pvp
                }

                // @sinender (taken from the closed PR)
                //check and see if any teams beacons are out of the border, This cannot be changed to a listener because there is no event for border shrink
                //I will however make this run every 5 seconds instead of every second
                if (tick % 120 == 0) {
                    for (team in game.getTeams(true)) {
                        val world = game.overworld
                        val beaconLoc = team.beacon!!.toWorldPosition(game.overworld.name).toBukkitLocation()
                        if (team.beacon != null) {
                            if (beaconLoc.distance(world.worldBorder.center) > world.worldBorder.size / 2) {
                                team.beacon!!.toWorldPosition(game.overworld.name).block.type =
                                    Material.AIR
                                team.beacon = null
                                Bukkit.broadcast(
                                    CC.translate("${team.getRawDisplayName()}&c's beacon has been destroyed because it was out of the border.")
                                )
                            }
                        }
                    }
                }
            }

            GameState.PVP_BORDER_SHRINK -> {
                if (shrinkingTime > 0) {
                    shrinkingTime--
                    game.border = game.overworld.worldBorder.size.roundToInt()
                } else {
                    shrinking = false
                }

                if (!shrinking) {
                    val border = game.border
                    val shrinkPhase = getBorderShrinkPhase(border)

                    if (shrinkPhase == null) {
                        // switch to deathmatch
                        game.gameState = GameState.DEATHMATCH
                        return
                    }


                    shrinkBorder(shrinkPhase)
                    shrinking = !shrinking

                    // shrinking till shrinkPhase.length + 10 minutes
                    // in ticks
                    shrinkingTime = 20 * (shrinkPhase.length + 600) // 10 minutes
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
    private fun getBorderShrinkPhase(border: Int): ShrinkPhase? {
        // get the shrink phases
        // [ { size: x, length: y } .. ]
        val shrinkPhases = MainConfig.BORDER_PHASES

        // find the phase that the border is in
        var maxLength = 0
        var maxSize = 0
        for (phaseObj in shrinkPhases) {
            val phase = ShrinkPhase(phaseObj["size"] as Int, phaseObj["length"] as Int)
            // find the max size that the border is in
            if (phase.size in (maxSize + 1)..<border) {
                maxSize = phase.size
                maxLength = phase.length
            }
        }

        if (maxSize == border) {
            return null
        }

        return ShrinkPhase(maxSize, maxLength)
    }

    /**
     * Shrink the border globally
     * @param shrinkPhase the phase to shrink the border to
     */
    private fun shrinkBorder(shrinkPhase: ShrinkPhase) {
        PlayerUtils.broadcastTitle(
            "&cBorder Shrinking!",
            "&7${shrinkPhase.size} in" + DateUtils.readableTime(BigDecimal(shrinkPhase.length.toLong()))
        )

        Bukkit.getScheduler().runTask(BBU.getInstance()) { ->
            for (world in Bukkit.getWorlds()) {
                world.worldBorder.setSize(shrinkPhase.size.toDouble(), shrinkPhase.length.toLong())
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