package me.imoltres.bbu.controllers

import com.viaversion.nbt.io.NBTIO
import com.viaversion.nbt.tag.CompoundTag
import kotlinx.coroutines.*
import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUCage
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.json.GsonFactory
import me.imoltres.bbu.utils.schematic.SchematicParser
import me.imoltres.bbu.utils.world.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.ExecutionException
import kotlin.io.path.Path

const val MAX_RANDOM_POSITION_ATTEMPTS = 500

/**
 * Controls all the cages
 */
class CageController(private val plugin: BBU) {

    private val cageSchematic: HashMap<Position, Material>
    private val cageCuboid: Cuboid

    init {
        try {
            val cagesSchematicPath = Path(plugin.schemesFolder.path, "cage.schem")
            val fileReader = NBTIO.reader(CompoundTag::class.java).named()

            // validate cage schematic exists
            if (!Files.exists(cagesSchematicPath)) {
                throw Exception("Cage schematic does not exist at path: $cagesSchematicPath")
            }

            val tag = fileReader.read(cagesSchematicPath, true)

            cageSchematic = SchematicParser.parseSchematic(tag)

            val min = Position(0.0, 0.0, 0.0)
            // find largest position
            var max = Position(0.0, 0.0, 0.0)
            for (pos in cageSchematic.keys) {
                if (pos.x > max.x) {
                    max = Position(pos.x, max.y, max.z)
                }
                if (pos.y > max.y) {
                    max = Position(max.x, pos.y, max.z)
                }
                if (pos.z > max.z) {
                    max = Position(max.x, max.y, pos.z)
                }
            }

            cageCuboid = Cuboid(min, max)

            println("Loaded cage schematic")
        } catch (e: IOException) {
            throw Exception("Failed to load cage schematic")
        }
    }

    val scope = CoroutineScope(
        Dispatchers.IO +
                SupervisorJob() +
                CoroutineExceptionHandler { _, exception ->
                    BBU.getInstance().logger.severe("An error occurred in the CageController coroutine scope: ${exception.message}")
                }
    )

    /**
     * Delete all the cages, then place them all back
     * @param world world to place the cages in
     */
    fun resetCages(world: World) {
        scope.launch {
            deleteCages(world)
            try {
                placeCages(world, ArrayList())
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Places all the cages with the exceptions provided
     * @param world world to place the cages in
     * @param teamsWithCages teams to ignore the cages for
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Throws(ExecutionException::class, InterruptedException::class)
    suspend fun placeCages(world: World, teamsWithCages: List<BBUTeam>) {
        val exclusions: MutableList<WorldPosition> = ArrayList()
        for (team in teamsWithCages) {
            val a = team.cage!!.cuboid
            exclusions.add(WorldPosition(a.min.x, a.min.y, a.min.z, world.name))
        }

        val teams: MutableList<BBUTeam> = ArrayList(plugin.teamController.allTeams)
        teams.removeAll(teamsWithCages)

        for (team in teams) {
            System.out.printf("Exclusions: %s\n", exclusions.toTypedArray().contentToString())

            val cuboid = getConfiguredCage(team)
            val position = if (cuboid == null) {
                println("Getting random position...")
                getRandomValidWorldPosition(world, exclusions, 75)
            } else {
                println("Using configured position...")
                WorldPosition(cuboid.min.x, cuboid.min.y, cuboid.min.z, world.name)
            }

            exclusions.add(position)
            placeCage(Position(position.x, position.y, position.z), team, world)
        }

    }

    /**
     * Get the configured cage for the world for a team
     * @param team the team to get the cage for
     * @return a cuboid for the cage (nullable)
     */
    private fun getConfiguredCage(team: BBUTeam): Cuboid? {
        val cageStr = plugin.teamSpawnsConfig.getString("team." + team.colour.name)
        if (cageStr != null && cageStr.isNotEmpty()) {
            return GsonFactory.getCompactGson().fromJson(cageStr, Cuboid::class.java)
        }

        return null
    }

    /**
     * Place a singular cage for a team in a given position
     * @param position world-less position to place it in
     * @param bbuTeam team to place it for
     * @param world world to place it in
     */
    fun placeCage(position: Position, bbuTeam: BBUTeam, world: World) {
        println("Placing cage for team: " + bbuTeam.colour.name)
        println("Position: $position")
        val to = Position(position.x, position.y, position.z)
        val cage = Cuboid(
            Position(to.x, to.y, to.z),
            Position(
                to.x + cageCuboid.max.x, to.y + cageCuboid.max.y, to.z + cageCuboid.max.z
            )
        )
        println("Cage: $cage")

        Bukkit.getScheduler().runTask(plugin, Runnable {
            // paste the schematic
            for ((pos, material) in cageSchematic) {
                val block = world.getBlockAt(
                    (to.x + pos.x).toInt(),
                    (to.y + pos.y).toInt(),
                    (to.z + pos.z).toInt()
                )
                block.type = material
            }

            bbuTeam.cage = BBUCage(bbuTeam, cage, cage.center.subtract(0.0, 1.0, 0.0).toWorldPosition(world.name))

            Bukkit.getConsoleSender().sendMessage(
                CC.translate(
                    "&aTeam '&" + bbuTeam.colour.chatColor.char + bbuTeam.colour.name + "&a' cage spawned at &7" + bbuTeam.cage!!.spawnPosition
                        .toString() + "&a."
                )
            )
            plugin.teamSpawnsConfig.configuration["team." + bbuTeam.colour.name] =
                GsonFactory.getCompactGson().toJson(cage)
        })
    }

    fun deleteCage(team: BBUTeam, world: World) {
        val cage = team.cage ?: return
        val cuboid = cage.cuboid

        for (position in cuboid) {
            val worldPos = position.toWorldPosition(world.name)
            Bukkit.getScheduler().runTask(plugin, Runnable {
                worldPos.block.type = Material.AIR
            })
        }
    }

    /**
     * Delete all the cages in a world
     * @param world world
     */
    fun deleteCages(world: World) {
        val teams: MutableList<BBUTeam> = ArrayList(plugin.teamController.allTeams)
        for (team in teams) {
            val cage = team.cage ?: continue
            val cuboid = cage.cuboid

            for (position in cuboid) {
                val worldPos = position.toWorldPosition(world.name)
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    worldPos.block.type = Material.AIR
                })
            }
        }
    }

    /**
     * Get a random world position inside the world border
     * @param world world to get the position in
     */
    private suspend fun getRandomWorldPosition(world: World): WorldPosition = withContext(Dispatchers.IO) {
        val borderSize = getBorderSizeSync(world)

        val world2D = Rectangle(
            Position2D(-(borderSize / 2), -(borderSize / 2)),
            Position2D(borderSize / 2, borderSize / 2)
        )

        var worldPosition: WorldPosition
        var attempts = 0
        do {
            val position2D = world2D.randomPosition().toIntPosition()
            val x = position2D.x
            val z = position2D.y


            // loads the chunk synchronously
            loadChunkAt(world, x.toInt(), z.toInt())

            // why + 3?
            // because the cage is 3 blocks tall,
            // so we need to make sure the highest block is at least 3 blocks below the spawn position
            // to avoid suffocating the player in the block when they spawn
            val highestY = getHighestYAtSync(world, x.toInt(), z.toInt() + 3)
            worldPosition = WorldPosition(
                x,
                highestY.toDouble(),
                z,
                world.name
            )

            println("Rolling position: $x, ${worldPosition.y.toInt()}, $z")

            attempts++
            if (attempts > MAX_RANDOM_POSITION_ATTEMPTS) {
                throw Exception("Failed to find a SAFE world position after $MAX_RANDOM_POSITION_ATTEMPTS attempts. Consider increasing the world border or decreasing the range.")
            }
        } while (!worldPosition.isSafe(4, 4))

        return@withContext worldPosition;
    }

    /**
     * Get a random position inside the configured border.
     * Ensures a min {@param range} between {@param exclusions} positions.
     *
     * @param world world
     * @param exclusions positions to avoid within a range
     * @param range range to avoid positions in
     * @return a position in the world
     */
    private suspend fun getRandomValidWorldPosition(
        world: World,
        exclusions: List<WorldPosition>,
        range: Int
    ): WorldPosition = withContext(Dispatchers.IO) {
        var worldPos: WorldPosition
        var attempts = 0
        do {
            worldPos = getRandomWorldPosition(world)
            BBU.getInstance().logger.info("Checking position: ${worldPos.x}, ${worldPos.y}, ${worldPos.z}")

            attempts++
            if (attempts > MAX_RANDOM_POSITION_ATTEMPTS) {
                throw Exception("Failed to find a VALID world position after $MAX_RANDOM_POSITION_ATTEMPTS attempts. Consider increasing the world border or decreasing the range.")
            }
        } while (exclusions.any { worldPos.distance(it) < range })

        worldPos
    }

    /**
     * Clean up all coroutines in a blocking way, used for when the plugin is disabling
     * (for java calls)
     */
    fun cleanupBlocking() {
        runBlocking { cleanup() }
    }

    /**
     * Cancel all coroutines when the plugin is disabled
     */
    suspend fun cleanup() {
        scope.coroutineContext[Job]?.cancelAndJoin() // Access the Job from the context
        scope.cancel("Plugin disabled") // Cancel the scope itself
    }

    /**
     * Loads a chunk at a given position
     * @param world world to load the chunk in
     * @param x x position
     * @param z z position
     */
    private suspend fun loadChunkAt(world: World, x: Int, z: Int) {
        // loads the chunk
        println("Loading chunk at $x, $z")

        val chunkX = x shr 4
        val chunkZ = z shr 4

        val deferred = CompletableDeferred<Unit>()

        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            // load the chunks 6x6 around the chunk
            for (i in -3..3) {
                for (j in -3..3) {
                    world.loadChunk(chunkX + i, chunkZ + j, true)
                }
            }

            println("Loaded chunk at $x, $z")
            deferred.complete(Unit)
        })

        return deferred.await()
    }

    /**
     * Gets the world border size synchronously
     * @param world world to load the chunk in
     * @return the world border size
     */
    private suspend fun getBorderSizeSync(world: World): Double {
        val deferred = CompletableDeferred<Double>()

        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            deferred.complete(world.worldBorder.size)
        })

        return deferred.await()
    }

    /**
     * Gets the highest Y at a given position synchronously
     * @param world world to load the chunk in
     * @param x x position
     * @param z z position
     * @return the highest Y at the given position
     */
    private suspend fun getHighestYAtSync(world: World, x: Int, z: Int): Int {
        val deferred = CompletableDeferred<Int>()

        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            deferred.complete(world.getHighestBlockYAt(x, z))
        })

        return deferred.await()
    }
}