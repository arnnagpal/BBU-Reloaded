package me.imoltres.bbu.controllers

import com.viaversion.nbt.io.NBTIO
import com.viaversion.nbt.tag.CompoundTag
import kotlinx.coroutines.*
import kotlinx.coroutines.future.asDeferred
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
import org.bukkit.block.data.BlockData
import java.io.IOException
import java.nio.file.Files
import java.util.concurrent.ExecutionException
import kotlin.io.path.Path

const val MAX_RANDOM_POSITION_ATTEMPTS = 500

/**
 * Controls all the cages
 */
class CageController(private val plugin: BBU) {

    private val cageSchematic: HashMap<Position, BlockData>
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
                placeCages(world, BBU.getInstance().game.border, ArrayList())
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
    suspend fun placeCages(world: World, borderSize: Int, teamsWithCages: List<BBUTeam>) {
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
                getRandomValidWorldPosition(world, borderSize, exclusions, 75)
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

    fun clearConfiguredCages() {
        for (team in plugin.teamController.allTeams) {
            plugin.teamSpawnsConfig.configuration.set("team." + team.colour.name, null)
        }

        // save
        plugin.teamSpawnsConfig.configuration.save(plugin.teamSpawnsConfig.file)
    }

    /**
     * Place a singular cage for a team in a given position
     * @param position world-less position to place it in
     * @param bbuTeam team to place it for
     * @param world world to place it in
     */
    fun placeCage(position: Position, bbuTeam: BBUTeam, world: World) {
        pasteSchematic(position.toWorldPosition(world.name)).invokeOnCompletion {
            if (it != null) {
                BBU.getInstance().logger.severe("Failed to paste cage schematic for team ${bbuTeam.colour.name}: ${it.message}")
                return@invokeOnCompletion
            }

            val cage = Cuboid(
                Position(position.x, position.y, position.z),
                Position(position.x + cageCuboid.max.x, position.y + cageCuboid.max.y, position.z + cageCuboid.max.z)
            )

            bbuTeam.cage =
                BBUCage(bbuTeam, cage, cage.center.subtract(0.0, 1.0, 0.0).toWorldPosition(world.name))
            plugin.teamSpawnsConfig.configuration["team." + bbuTeam.colour.name] =
                GsonFactory.getCompactGson().toJson(cage)
            Bukkit.getConsoleSender().sendMessage(
                CC.translate(
                    "&aTeam '&${bbuTeam.colour.chatColor.code}${bbuTeam.colour.name}&a' cage spawned."
                )
            )
        }
    }

    fun pasteSchematic(position: WorldPosition): CompletableDeferred<Unit> =
        CompletableDeferred<Unit>().also { deferred ->
            val loc = position.toBukkitLocation()
            val to = Position(position.x, position.y, position.z)

            val entries = cageSchematic.entries.toList()
            val batchSize = 50 // blocks per tick

            entries.chunked(batchSize).forEachIndexed { index, batch ->
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    for ((pos, data) in batch) {
                        loc.world.getBlockAt(
                            (to.x + pos.x).toInt(),
                            (to.y + pos.y).toInt(),
                            (to.z + pos.z).toInt()
                        ).blockData = data
                    }

                    // only finalize on the last batch
                    if (index == entries.chunked(batchSize).size - 1) {
                        deferred.complete(Unit)
                        return@Runnable
                    }
                }, index.toLong()) // 1 tick delay per batch

            }
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
    private suspend fun findSafePosition(world: World, borderSize: Double): WorldPosition? {
        val world2D = Rectangle(
            Position2D(-(borderSize / 2), -(borderSize / 2)),
            Position2D(borderSize / 2, borderSize / 2)
        )
        val position2D = world2D.randomPosition().toIntPosition()
        val x = position2D.x.toInt()
        val z = position2D.y.toInt()
        val deferred = CompletableDeferred<WorldPosition?>()

        val t0 = System.currentTimeMillis()
        loadChunkAt(world, x, z)
        val t1 = System.currentTimeMillis()

        // run one sync call instead of 3 to avoid round trips (optimization)
        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            val t2 = System.currentTimeMillis()

            val highestY = world.getHighestBlockYAt(x, z + 3)
            val t3 = System.currentTimeMillis()

            val candidate = WorldPosition(x.toDouble(), highestY.toDouble(), z.toDouble(), world.name)
            val safe = candidate.isSafe(4, 4)
            val t4 = System.currentTimeMillis()
            BBU.getInstance().logger.info("chunk load=${t1 - t0}ms | main thread wait=${t2 - t1}ms | highestY=${t3 - t2}ms | isSafe=${t4 - t3}ms")
            deferred.complete(if (safe) candidate else null)
        })

        return deferred.await()
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
        borderSize: Int,
        exclusions: List<WorldPosition>,
        range: Int
    ): WorldPosition = withContext(Dispatchers.IO) {

        val t0 = System.currentTimeMillis()
        repeat(MAX_RANDOM_POSITION_ATTEMPTS) {
            val candidate = findSafePosition(world, borderSize.toDouble()) ?: return@repeat
            if (exclusions.none { candidate.distance(it) < range }) {
                val t1 = System.currentTimeMillis()
                BBU.getInstance().logger.info("Found valid position after ${t1 - t0}ms and ${it + 1} attempts")
                return@withContext candidate
            }
        }

        throw Exception("Failed to find a VALID world position after $MAX_RANDOM_POSITION_ATTEMPTS attempts.")
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
     * Load the chunk at the given coordinates and the surrounding chunks asynchronously.
     */
    private suspend fun loadChunkAt(world: World, x: Int, z: Int) {
        val chunkX = x shr 4
        val chunkZ = z shr 4
        val jobs = (-1..1).flatMap { i ->
            (-1..1).map { j ->
                world.getChunkAtAsync(chunkX + i, chunkZ + j).asDeferred()
            }
        }
        jobs.awaitAll()
    }

}