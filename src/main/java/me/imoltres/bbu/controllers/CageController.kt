package me.imoltres.bbu.controllers

import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.extent.clipboard.io.BuiltInClipboardFormat
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats
import com.sk89q.worldedit.function.operation.ForwardExtentCopy
import com.sk89q.worldedit.function.operation.Operations
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.CuboidRegion
import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUCage
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.GsonFactory
import me.imoltres.bbu.utils.world.*
import org.bukkit.Bukkit
import org.bukkit.World
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.ExecutionException

class CageController(val plugin: BBU) {
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

    @Throws(ExecutionException::class, InterruptedException::class)
    fun placeCages(world: World, teamsWithCages: List<BBUTeam>) {
        val exclusions: MutableList<Position2D> = ArrayList()
        for (team in teamsWithCages) {
            exclusions.add(Position2D(team.cage!!.cuboid.min.x, team.cage!!.cuboid.min.z))
        }

        val teams: MutableList<BBUTeam> = ArrayList(BBU.instance.teamController.allTeams)
        teams.removeAll(teamsWithCages)

        for (team in teams) {
            System.out.printf("Exclusions: %s\n", exclusions.toTypedArray().contentToString())

            val position2D = getRandom2DPositionInsideWorldBorder(world, exclusions, 75)
            val y = world.getHighestBlockYAt(position2D.x.toInt(), position2D.y.toInt())
            exclusions.add(position2D)
            placeCage(Position(position2D.x, y.toDouble(), position2D.y), team, world)
        }

    }

    fun placeCage(position: Position, bbuTeam: BBUTeam, world: World) {
        val scheme = File(BBU.instance.schemesFolder, "cage.schem")
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
        val region = CuboidRegion(
            BlockVector3.at(cage.lowerX, cage.lowerY, cage.lowerY),
            BlockVector3.at(cage.upperX, cage.upperY, cage.upperZ)
        )
        val c = BlockArrayClipboard(region)
        WorldEdit.getInstance().newEditSession(BukkitWorld(world)).use { editSession ->
            val forwardExtentCopy = ForwardExtentCopy(
                editSession, region, c, region.minimumPoint
            )
            Operations.complete(forwardExtentCopy)
        }
        try {
            BuiltInClipboardFormat.FAST.getWriter(
                FileOutputStream(
                    File(
                        BBU.instance.tempSchemesFolder,
                        bbuTeam.colour.name + "-CAGE-OVERRIDEN-BLOCKS.schem"
                    )
                )
            ).use { writer -> writer.write(c) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        clipboard.paste(BukkitWorld(world), to).flushQueue()

        Bukkit.getScheduler().runTask(plugin, Runnable {
            bbuTeam.cage = BBUCage(bbuTeam, cage, cage.center.toWorldPosition(world.name))

            Bukkit.getConsoleSender().sendMessage(
                CC.translate(
                    "&aTeam '&" + bbuTeam.colour.chatColor.char + bbuTeam.colour.name + "&a' cage spawned at &7" + bbuTeam.cage!!.spawnPosition
                        .toString() + "&a."
                )
            )
            BBU.instance.teamSpawnsConfig.configuration["team." + bbuTeam.colour.name] =
                GsonFactory.getCompactGson().toJson(cage)
        })
    }

    fun deleteCages(world: World) {
        for (team in BBU.instance.teamController.allTeams) {
            val cageStr = BBU.instance.teamSpawnsConfig.getString("team." + team.colour.name)
            if (cageStr != null && !cageStr.isEmpty()) {
                val oldCage = GsonFactory.getCompactGson().fromJson(cageStr, Cuboid::class.java)
                val cageOverride =
                    File(BBU.instance.tempSchemesFolder, team.colour.name + "-CAGE-OVERRIDEN-BLOCKS.schem")
                if (cageOverride.exists()) {
                    try {
                        BuiltInClipboardFormat.FAST.load(cageOverride).paste(
                            BukkitWorld(world),
                            BlockVector3.at(oldCage.lowerX, oldCage.lowerY, oldCage.lowerY)
                        ).flushQueue()
                        cageOverride.delete()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

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
                if (Bukkit.getScheduler().callSyncMethod(BBU.instance) { BBU.instance.disabling }.get())
                    break

                position2D = getRandom2DPositionInsideWorldBorder(world, exclusions, range)
            }
        }

        return position2D
    }
}