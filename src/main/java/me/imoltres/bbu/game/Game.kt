package me.imoltres.bbu.game

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.threads.GameStartThread
import me.imoltres.bbu.game.threads.GameThread
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.world.Position2D
import org.bukkit.*
import java.util.concurrent.ExecutionException

class Game {
    private val progression = GameProgression()

    val thread = GameThread(this)

    val border: Int = BBU.instance.mainConfig.getInteger("border")

    lateinit var overworld: World
    lateinit var nether: World
    lateinit var end: World

    init {
        setupWorlds()
    }

    lateinit var fortressPosition: Position2D
    fun startGame() {
        if (thread.isAlive) {
            throw RuntimeException("Game has already started.")
        }

        for (team in BBU.instance.teamController.allTeams) {
            if (team.players.size == 0) {
                team.eliminate()
            }
        }

        GameStartThread(this).start()
    }

    fun stopGame() {
        gameState = GameState.POST_GAME
    }

    var gameState: GameState
        get() = progression.gameState
        set(state) {
            System.out.printf("Changing game state from %s to %s\n", gameState.name, state.name)
            progression.gameState = state
        }


    fun checkTeam(team: BBUTeam) {
        println("Added '" + team.colour.name + "' to the checking queue.")
        thread.teamCheckQueue.offer(team)
    }

    private fun setupWorlds() {
        //Get main world
        overworld = Bukkit.getWorld(NamespacedKey.minecraft("overworld"))!!
        //Get nether world
        nether = Bukkit.getWorld(NamespacedKey.minecraft("the_nether"))!!
        //Get end world
        end = Bukkit.getWorld(NamespacedKey.minecraft("the_end"))!!

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

        Bukkit.getScheduler().runTaskAsynchronously(BBU.instance, Runnable {
            try {
                BBU.instance.cageController.placeCages(overworld, BBU.instance.teamController.teamsWithCages)

                Bukkit.getScheduler().runTaskLater(BBU.instance, Runnable {
                    BBU.instance.joinable = true
                }, 20L)
            } catch (e: ExecutionException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        })

    }

}