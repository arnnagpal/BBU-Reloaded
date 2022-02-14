package me.imoltres.bbu.game

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.generator.EmptyChunkGenerator
import me.imoltres.bbu.game.threads.GameStartThread
import me.imoltres.bbu.game.threads.GameThread
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.world.Position2D
import org.bukkit.*
import org.bukkit.command.CommandSender
import java.util.concurrent.ExecutionException

/**
 * Game class to control the state of the game
 */
class Game {
    private val progression = GameProgression()

    val thread = GameThread(this)

    val border: Int = MainConfig.BORDER.toInt()

    lateinit var overworld: World
    lateinit var nether: World
    lateinit var end: World
    lateinit var spawnWorld: World

    init {
        setupWorlds()
    }

    lateinit var fortressPosition: Position2D

    /**
     * the game state
     */
    var gameState: GameState
        get() = progression.gameState
        set(state) {
            System.out.printf("Changing game state from %s to %s\n", gameState.name, state.name)
            progression.gameState = state
        }

    /**
     * Start the game
     */
    fun startGame() {
        if (thread.isAlive) {
            throw RuntimeException("Game has already started.")
        }

        for (team in BBU.getInstance().teamController.allTeams) {
            if (team.players.size == 0) {
                team.eliminate()
            }
        }

        GameStartThread(this).start()
    }

    /**
     * Stop the game
     */
    fun stopGame() {
        gameState = GameState.POST_GAME
    }

    /**
     * Move the server into a pre-lobby from a pre-game state
     * @param sender person who initiated the move (nullable)
     */
    fun preLobby(sender: CommandSender?) {
        Thread {
            for (team in BBU.getInstance().teamController.teamsWithCages) {
                team.distributeItems()
                for (player in team.players) {
                    Bukkit.getScheduler().runTask(
                        BBU.getInstance(),
                        Runnable {
                            team.cage?.spawnPosition?.toBukkitLocation()?.let {
                                println(it)
                                player.player?.teleportAsync(it)
                            }
                        }
                    )
                    sender?.sendMessage(CC.translate("&7${player.getRawDisplayName()} &ahas been put in a cage."))
                    Thread.sleep(750)
                }
            }

            gameState = GameState.PRE_GAME
            sender?.sendMessage(CC.translate("&aAll players (with a cage) are in a cage."))
        }.start()
    }

    /**
     * Puts the team instance into a queue to be checked<br>
     * determines if the team should be eliminated.
     */
    fun checkTeam(team: BBUTeam) {
        println("Added '" + team.colour.name + "' to the checking queue.")
        thread.teamCheckQueue.offer(team)
    }

    /**
     * Get all the teams with a beacon filter
     * @param hasBeacon do the teams have a beacon?
     * @return a set of teams
     */
    fun getTeams(hasBeacon: Boolean): Set<BBUTeam> {
        return progression.getTeams(hasBeacon)
    }

    /**
     * Get all the teams that are alive
     * @return a set of teams that are alive
     */
    fun getAliveTeams(): Set<BBUTeam> {
        return progression.getTeamsAlive()
    }

    /**
     * Setup all the worlds used by the game.
     * Also starts the building of cages
     */
    private fun setupWorlds() {
        //Get main world
        overworld = Bukkit.getWorld(NamespacedKey.minecraft("overworld"))!!
        //Get nether world
        nether = Bukkit.getWorld(NamespacedKey.minecraft("the_nether"))!!
        //Get end world
        end = Bukkit.getWorld(NamespacedKey.minecraft("the_end"))!!
        //Get spawn world
        spawnWorld = WorldCreator("bbuSpawnWorld").generator(EmptyChunkGenerator()).createWorld()!!

        overworld.worldBorder.setCenter(0.0, 0.0)
        overworld.worldBorder.size = border.toDouble()

        nether.worldBorder.setCenter(0.0, 0.0)
        nether.worldBorder.size = border.toDouble()

        val netherSpawn = Location(nether, 0.0, 70.0, 0.0)
        val location = nether.locateNearestStructure(netherSpawn, StructureType.NETHER_FORTRESS, border, false)
        if (location != null) {
            fortressPosition = Position2D(location.x, location.z).toIntPosition()
        } else {
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("Unable to find a fortress within border, you might have to find a new seed."))
        }

        Bukkit.getScheduler().runTaskAsynchronously(BBU.getInstance(), Runnable {
            try {
                BBU.getInstance().cageController.placeCages(overworld, BBU.getInstance().teamController.teamsWithCages)

                Bukkit.getScheduler().runTaskLater(BBU.getInstance(), Runnable {
                    BBU.getInstance().isJoinable = true
                }, 20L)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        })

    }

}