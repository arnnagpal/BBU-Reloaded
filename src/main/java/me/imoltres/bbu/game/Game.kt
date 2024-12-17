package me.imoltres.bbu.game

import kotlinx.coroutines.launch
import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.generator.EmptyChunkGenerator
import me.imoltres.bbu.game.threads.GameStartThread
import me.imoltres.bbu.game.threads.GameThread
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.general.PlayerUtils
import me.imoltres.bbu.utils.world.Position2D
import org.bukkit.*
import org.bukkit.command.CommandSender
import org.bukkit.entity.Firework
import org.bukkit.generator.structure.StructureType
import java.util.concurrent.ExecutionException


/**
 * Game class to control the state of the game
 */
class Game {
    private val progression = GameProgression()

    val thread = GameThread(this)

    val border: Int = MainConfig.BORDER

    lateinit var overworld: World
    lateinit var nether: World
    lateinit var end: World
    lateinit var spawnWorld: World

    lateinit var worlds: Array<World>

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
        if (thread.started) {
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
    fun stopGame(winner: BBUTeam?) {
        gameState = GameState.POST_GAME

        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            if (winner != null) {
                for (player in winner.players) {
                    val f = player.player?.location?.let { player.player?.world?.spawn(it, Firework::class.java) }
                    val fm = f?.fireworkMeta
                    if (fm != null) {
                        fm.addEffect(
                            FireworkEffect.builder()
                                .flicker(true)
                                .trail(true)
                                .with(FireworkEffect.Type.STAR)
                                .with(FireworkEffect.Type.BALL)
                                .with(FireworkEffect.Type.BALL_LARGE)
                                .withColor(Color.AQUA)
                                .withColor(Color.YELLOW)
                                .withColor(Color.RED)
                                .withColor(Color.WHITE)
                                .build()
                        )

                        fm.power = 0
                        f.fireworkMeta = fm
                    }
                }

                PlayerUtils.broadcastTitle(
                    "&cGame ended!",
                    "&7The winning team is ${winner.getRawDisplayName()}&7!"
                )
            }
        })
    }

    /**
     * Move the server into a pre-game from a lobby state
     * @param sender person who initiated the move (nullable)
     */
    fun preLobby(sender: CommandSender?) {
        Thread {
            for (team in BBU.getInstance().teamController.teamsWithCages) {
                for (player in team.players) {
                    player.preventMovement()
                    Bukkit.getScheduler().runTaskLater(
                        BBU.getInstance(),
                        Runnable {
                            team.cage?.spawnPosition?.toBukkitLocation()?.let {
                                println(it)
                                it.chunk.load()

                                player.player?.teleport(it)
                                Bukkit.getScheduler().runTaskLater(
                                    BBU.getInstance(), Runnable {
                                        player.allowMovement()
                                    }, 20L
                                )
                            }
                        },
                        20L
                    )
                    sender?.sendMessage(CC.translate("&7${player.getRawDisplayName()} &ahas been put in a cage."))
                    Thread.sleep(750)
                }
                team.distributeItems()
            }

            gameState = GameState.PRE_GAME
            sender?.sendMessage(CC.translate("&aAll players (with a cage) are in a cage."))
        }.start()
    }

    /**
     * Puts the team instance into a queue to be checked
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

        worlds = arrayOf(overworld, nether, end, spawnWorld)
        for (world in worlds) {
            world.loadChunk(0, 0)
            world.worldBorder.setCenter(0.0, 0.0)
            world.worldBorder.size = border.toDouble()

            world.time = 0
            world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)

            world.setStorm(false)
            world.isThundering = false
        }

        val netherSpawn = Location(nether, 0.0, 70.0, 0.0)
        val location = nether.locateNearestStructure(netherSpawn, StructureType.FORTRESS, border, false)?.location
        if (location != null) {
            fortressPosition = Position2D(location.x, location.z).toIntPosition()
        } else {
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("Unable to find a fortress within border, you might have to find a new seed."))
        }

        try {
            BBU.getInstance().cageController.scope.launch {
                BBU.getInstance().cageController.placeCages(
                    overworld,
                    BBU.getInstance().teamController.teamsWithCages
                )
            }.invokeOnCompletion {
                //after scope is done, allow players to join
                Bukkit.getScheduler().runTaskLater(BBU.getInstance(), Runnable {
                    BBU.getInstance().isJoinable = true
                }, 20L)
            }
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

    }

}