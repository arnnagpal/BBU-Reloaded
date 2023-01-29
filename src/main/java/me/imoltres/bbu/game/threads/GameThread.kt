package me.imoltres.bbu.game.threads

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.general.DateUtils
import me.imoltres.bbu.utils.general.PlayerUtils
import me.imoltres.bbu.utils.world.Position
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import java.math.BigDecimal
import java.util.*

/**
 * Main game thread loop, swaps the game states, checks the teams, and ticks the game.
 */
class GameThread(val game: Game) : Thread() {

    var tick: Int = 0

    private val shrinkTo = 250.0

    private var shrinking = false
    private var pvp = false
    private var deathmatch = false
    private var deathmatchPVP = false

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
                checkWinConditions()

                when (game.gameState) {
                    GameState.PRE_GAME -> { //This should probably be more efficient
                        //check and see if all players are in their cages
                        for (player in game.getAliveTeams().flatMap { it.players }) {
                            if (player.team != null) {
                                if (!player.team!!.cage!!.cuboid.contains(Position(player.player!!.location))) {
                                    player.player!!.teleport(player.team!!.cage!!.spawnPosition.toBukkitLocation())
                                }
                            }
                        }
                    }

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
                        //check and see if any teams beacons are out of the border
                        for (team in game.getTeams(true)) {
                            val world = Bukkit.getWorld("world")
                            val beaconLoc = team.beacon!!.toWorldPosition("world").toBukkitLocation()
                            if (team.beacon != null) {
                                if (beaconLoc.distance(world!!.worldBorder.center) > world.worldBorder.size / 2) {
                                    team.beacon!!.toWorldPosition(BBU.getInstance().game.overworld.name).block.type =
                                        Material.AIR
                                    team.beacon = null
                                    Bukkit.broadcast(
                                        CC.translate("${team.getRawDisplayName()}&c's beacon has been destroyed because it was out of the border.")
                                    )
                                }
                            }
                        }
                    }

                    GameState.DEATH_MATCH -> {
                        if (!deathmatch) {
                            PlayerUtils.broadcastTitle(
                                "&cDeath Match has started!",
                                "&7"
                            )

                            for (team in game.getAliveTeams()) {
                                team.beacon!!.toWorldPosition(BBU.getInstance().game.overworld.name).block.type =
                                    Material.AIR
                                team.beacon = null

                                for (player in team.players) {
                                    // *IMPORTANT* This is where the death match world is set
                                    // May cause entity cramming if too many players are alive, but eh
                                    // TODO make this configurable
                                    player.player!!.teleport(Location(Bukkit.getWorld("bbuDeathMatchWorld"), 0.0, 100.0, 0.0))
                                }
                            }
                            deathmatch = !deathmatch
                        }
                    }

                    GameState.DEATH_MATCH_PVP -> {
                        if (!deathmatchPVP) {
                            PlayerUtils.broadcastTitle(
                                "&cPvP is now enabled.",
                                "&7"
                            )

                            deathmatchPVP = !deathmatchPVP
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

    private fun checkWinConditions() {
        val teamsLeft = BBU.getInstance().game.getAliveTeams().toList()

        if (teamsLeft.size == 1) {
            Bukkit.broadcast(CC.translate("&a" + teamsLeft[0].getRawDisplayName() + " &ahas won the BBU!! GGS"))
            BBU.getInstance().game.stopGame(teamsLeft[0])
        }

    }
}