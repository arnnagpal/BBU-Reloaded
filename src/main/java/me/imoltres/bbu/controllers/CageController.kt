package me.imoltres.bbu.controllers

import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.math.BlockVector3
import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUCage
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.GsonFactory
import me.imoltres.bbu.utils.world.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.World
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Controls all the cages
 */
class CageController(private val plugin: BBU) {

    /**
     * Delete all the cages, then place them all back
     * @param world world to place the cages in
     */
    fun resetCages(world: World) {
        deleteCages(world)
        try {
            placeCages(world, ArrayList())
        } catch (e: ExecutionException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
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
    fun placeCages(world: World, teamsWithCages: List<BBUTeam>) {
        val exclusions: MutableList<Position2D> = ArrayList()
        for (team in teamsWithCages) {
            exclusions.add(Position2D(team.cage!!.cuboid.min.x, team.cage!!.cuboid.min.z))
        }

        val teams: MutableList<BBUTeam> = ArrayList(plugin.teamController.allTeams)
        teams.removeAll(teamsWithCages)

        for (team in teams) {
            System.out.printf("Exclusions: %s\n", exclusions.toTypedArray().contentToString())

            val cuboid = getConfiguredCage(team)
            val position2D = if (cuboid == null) {
                getRandom2DPositionInsideWorldBorder(world, exclusions, 75)
            } else {
                Position2D(cuboid.min.x, cuboid.min.z)
            }

            val y = cuboid?.min?.y ?: world.getHighestBlockYAt(position2D.x.toInt(), position2D.y.toInt())

            exclusions.add(position2D)
            placeCage(Position(position2D.x, y.toDouble(), position2D.y), team, world)
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
        val scheme = File(plugin.schemesFolder, "cage.schem")
        val clipboard: Clipboard = try {
            Objects.requireNonNull(ClipboardFormats.findByFile(scheme))!!.load(scheme)
        } catch (e: IOException) {
            e.printStackTrace()
            Bukkit.shutdown()
            return
        }

        val maxOffset = clipboard.maximumPoint.subtract(clipboard.minimumPoint)
        val to = BlockVector3.at(position.x, position.y, position.z)
        val cage = Cuboid(
            Position(to.x.toDouble(), to.y.toDouble(), to.z.toDouble()),
            Position(
                to.add(maxOffset).x.toDouble(), to.add(maxOffset).y.toDouble(), to.subtract(maxOffset).z.toDouble()
            )
        )

        clipboard.paste(BukkitWorld(world), to).flushQueue()

        Bukkit.getScheduler().runTask(plugin, Runnable {
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

    /**
     * Delete all the cages in a world
     * @param world world
     */
    fun deleteCages(world: World) {
        for (team in plugin.teamController.allTeams) {
            team.cage?.cuboid?.forEach { position ->
                val worldPos = position.toWorldPosition(world.name)
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    worldPos.block.type = Material.AIR
                })
            }
        }
    }

    /**
     * Get a random 2d position inside the configured border<br>
     * Ensures a range between the positions
     *
     * @param world world
     * @param exclusions positions to avoid within a range
     * @param range range to avoid positions in
     * @return a 2D position in the world
     */
    @Throws(ExecutionException::class, InterruptedException::class)
    private fun getRandom2DPositionInsideWorldBorder(
        world: World,
        exclusions: List<Position2D>,
        range: Int
    ): Position2D {
        val world2D = Rectangle(
            Position2D(-(world.worldBorder.size / 2), -(world.worldBorder.size / 2)),
            Position2D(world.worldBorder.size / 2, world.worldBorder.size / 2)
        )

        var position2D = world2D.randomPosition().toIntPosition()
        var x = position2D.x
        var z = position2D.y

        if (!world.getChunkAt(x.toInt(), z.toInt()).isLoaded)
            world.getChunkAt(x.toInt(), z.toInt()).load()

        val worldPosition = WorldPosition(
            position2D.x,
            (world.getHighestBlockYAt(x.toInt(), z.toInt()) + 3).toDouble(),
            position2D.y,
            world.name
        )

        while (!worldPosition.isSafe) {
            worldPosition.add(0.0, 1.0, 0.0)
        }

        x = worldPosition.x
        val y = worldPosition.y.toInt()
        z = worldPosition.z
        Bukkit.getConsoleSender().sendMessage(CC.translate("Checking position: $x, $y, $z"))
        for (p in exclusions) {
            while (p.distance(position2D) < range) {
                position2D = getRandom2DPositionInsideWorldBorder(world, exclusions, range)
            }
        }

        return position2D
    }
}