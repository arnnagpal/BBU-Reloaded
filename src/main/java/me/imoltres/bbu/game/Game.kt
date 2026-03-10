package me.imoltres.bbu.game

import kotlinx.coroutines.launch
import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.BBUTeamColor
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.generator.EmptyChunkGenerator
import me.imoltres.bbu.game.threads.GameStartThread
import me.imoltres.bbu.game.threads.GameThread
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter
import me.imoltres.bbu.scoreboard.impl.MainScoreboard
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.general.FileUtils
import me.imoltres.bbu.utils.general.PlayerUtils
import me.imoltres.bbu.utils.json.GsonFactory
import me.imoltres.bbu.utils.world.Position2D
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.command.CommandSender
import org.bukkit.entity.Firework
import org.bukkit.generator.structure.StructureType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.ThreadLocalRandom


const val WORLD_PREFIX = "bbu"

/**
 * Game class to control the state of the game
 */
class Game {
    private val progression = GameProgression()

    var thread = GameThread(this)

    var border: Int = MainConfig.BORDER_SIZE

    lateinit var overworld: World
    lateinit var nether: World
    lateinit var end: World
    lateinit var spawnWorld: World

    lateinit var worlds: Array<World>

    var fortressPosition: Position2D? = null

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
                team.delete()
            }
        }

        // reload the scoreboards


        GameStartThread(this).runTaskTimer(BBU.getInstance(), 0L, 20L)
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
        for (world in worlds) {
            if (world == spawnWorld) continue

            world.setGameRule(GameRules.ADVANCE_WEATHER, true)
            world.setGameRule(GameRules.SPAWN_MOBS, true)
        }

        object : BukkitRunnable() {
            var index = 0

            val teamsList = BBU.getInstance().teamController.teamsWithCages.flatMap { team ->
                team.players.map { player -> Pair(team, player) }
            }

            val playersTeleportedByTeam = mutableMapOf<BBUTeam, Int>()
            var totalDoneTeleporting = 0

            override fun run() {
                println("Teleporting players to cages... (index: $index/${teamsList.size})")
                if (index >= teamsList.size) {
                    gameState = GameState.PRE_GAME

                    if (totalDoneTeleporting >= teamsList.size) {
                        sender?.sendMessage(CC.translate("&aAll players (with a cage) are in a cage."))
                        cancel()
                    }

                    return
                }

                val (team, player) = teamsList[index++]
                var playersTeleported = playersTeleportedByTeam.getOrDefault(team, 0)
                val location = team.cage?.spawnPosition?.toBukkitLocation()
                if (location != null) {
                    location.chunk.load()
                } else {
                    println("Unable to load chunk for team ${team.colour.name} because the spawn position is null.")
                    return // skip this player, they won't be put in a cage but at least the rest of the game can continue
                }

                player.preventMovement()

                Bukkit.getScheduler().runTaskLater(
                    BBU.getInstance(),
                    Runnable {
                        player.player?.teleport(location)
                        sender?.sendMessage(CC.translate("&7${player.getRawDisplayName()} &ahas been put in a cage."))

                        // update counter
                        playersTeleported += 1
                        totalDoneTeleporting += 1
                        playersTeleportedByTeam[team] = playersTeleported

                        Bukkit.getScheduler().runTaskLater(
                            BBU.getInstance(), Runnable {
                                player.allowMovement()

                                // check if all players of the team have been put in cages, if so, distribute their items
                                if (playersTeleported >= team.players.size) {
                                    team.distributeItems()
                                }
                            }, 20L
                        )
                    },
                    20L
                )
            }

        }.runTaskTimer(BBU.getInstance(), 0L, 20L);
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

    fun setupLobbyWorld() {
        val generateSpawn = MainConfig.LOBBY_SPAWN.isEmpty()
        //Get spawn world
        spawnWorld = WorldCreator(WORLD_PREFIX)
            .generator(EmptyChunkGenerator())
            .createWorld()!!

        if (generateSpawn) {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&cCouldn't find a valid lobby spawn. Generating..."))

            spawnWorld.loadChunk(0, 0)
            spawnWorld.setBlockData(0, 99, 0, Material.GLASS.createBlockData())

            BBU.getInstance().mainConfig.configuration.set(
                "lobby-spawn",
                GsonFactory.getCompactGson().toJson(WorldPosition(0.0, 100.0, 0.0, 90.0f, 0.0f, "bbuSpawnWorld"))
            )
            BBU.getInstance().mainConfig.configuration.save(BBU.getInstance().mainConfig.file)

            Bukkit.getConsoleSender().sendMessage(CC.translate("&aGenerated lobby spawn at 0, 100, 0 in bbuSpawnWorld"))
            MainConfig.reload()
        }
    }

    /**
     * Setup all the worlds used by the game.
     * Also starts the building of cages
     */
    fun setupWorlds() {
        val seed = ThreadLocalRandom.current().nextLong()

        overworld = WorldCreator("${WORLD_PREFIX}_overworld")
            .environment(World.Environment.NORMAL)
            .seed(seed)
            .createWorld()!!

        nether = WorldCreator("${WORLD_PREFIX}_nether")
            .environment(World.Environment.NETHER)
            .seed(seed)
            .createWorld()!!

        end = WorldCreator("${WORLD_PREFIX}_the_end")
            .environment(World.Environment.THE_END)
            .seed(seed)
            .createWorld()!!

        worlds = arrayOf(overworld, nether, end, spawnWorld)
        for (world in worlds) {
            world.loadChunk(0, 0)
            world.worldBorder.setCenter(0.0, 0.0)
            world.worldBorder.size = border.toDouble()

            world.setStorm(false)
            world.isThundering = false
            world.setGameRule(GameRules.ADVANCE_WEATHER, false)
            world.setGameRule(GameRules.SPAWN_MOBS, false)
            world.setGameRule(GameRules.ADVANCE_TIME, false)

            world.time = 0
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


    /**
     * Resets the game to its initial state. This includes:
     * - teleporting all players to the spawn world
     * - resetting their inventories and health
     * - resetting the game state and progression
     * - resetting the border
     * - resetting the fortress position
     * - resetting the threads
     * - resetting the worlds (time, weather, etc.)
     * - deleting all cages, beacons, and other game-related entities
     *
     * Note: this does NOT reset teams or their players, but it does reset their beacons and items.
     */
    fun reset() {
        // stop the threads
        if (thread.started) thread.cancel()
        thread = GameThread(this) // create a new thread instance so we can start it again later without issues

        val teamController = BBU.getInstance().teamController
        val cageController = BBU.getInstance().cageController
        val playerController = BBU.getInstance().playerController

        for (player in Bukkit.getOnlinePlayers()) {
            player.teleport(Location(spawnWorld, 0.0, 100.0, 0.0))
            player.inventory.clear()
            player.health = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: 20.0
            player.foodLevel = 20
            player.fireTicks = 0
            player.gameMode = GameMode.ADVENTURE

            val bbuPlayer = playerController.getPlayer(player.uniqueId)
            // disable player scoreboard
            bbuPlayer.scoreboard?.remove()
        }

        // store player's teams so we can reassign them after resetting
        val playerTeams = mutableMapOf<UUID, BBUTeamColor>()
        for (team in BBU.getInstance().teamController.allTeams) {
            for (player in team.players) {
                playerTeams[player.uniqueId] = team.colour
            }
        }

        // clear teams
        teamController.clearTeams()
        cageController.clearConfiguredCages()
        playerController.resetPlayers()

        // reset worlds
        val newSeed = ThreadLocalRandom.current().nextLong()
        regenerateWorld(newSeed, overworld, spawnWorld)
        regenerateWorld(newSeed, nether, spawnWorld)
        regenerateWorld(newSeed, end, spawnWorld)

        // reset game state
        gameState = GameState.LOBBY
        fortressPosition = null

        setupWorlds()

        // set up teams again
        for ((playerId, color) in playerTeams) {
            val player = playerController.getPlayer(playerId)
            val team = teamController.getTeam(color)
            if (player != null) {
                team.addPlayer(player)
                BBU.getInstance().logger.info("Reassigned player ${player.name} to team ${team.colour.name}")

                BBUScoreboardAdapter.display(MainScoreboard::class.java, player.player)
            }
        }
    }

    /**
     * Regenerates the world by unloading it, deleting its folder, and creating a new one with the same name.
     *
     * @param world the world to regenerate
     * @param fallbackWorld the world to teleport players to if they are in the world being regenerated
     */
    fun regenerateWorld(seed: Long, world: World, fallbackWorld: World) {
        if (Bukkit.isTickingWorlds()) {
            Bukkit.getScheduler()
                .runTaskLater(BBU.getInstance(), Runnable { regenerateWorld(seed, world, fallbackWorld) }, 1L)
            return
        }

        val creator = WorldCreator(world.name)
            .environment(world.environment)
            .type(WorldType.NORMAL)

        for (player in world.players) {
            player.teleport(BBU.getInstance().game.spawnWorld.spawnLocation)
        }

        if (!Bukkit.unloadWorld(world, false)) {
            BBU.getInstance().logger.severe("Failed to unload world: ${world.name}")
            return
        }

        FileUtils.deleteFolder(world.worldFolder)

        if (Bukkit.createWorld(creator) == null) {
            BBU.getInstance().logger.severe("Failed to recreate world: ${world.name}")
        }
    }

}