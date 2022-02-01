package me.imoltres.bbu

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile
import me.imoltres.bbu.commands.GameCommand
import me.imoltres.bbu.controllers.CageController
import me.imoltres.bbu.controllers.PlayerController
import me.imoltres.bbu.controllers.TeamController
import me.imoltres.bbu.data.BBUTeamColour
import me.imoltres.bbu.game.Game
import me.imoltres.bbu.listeners.BeaconListener
import me.imoltres.bbu.listeners.DeathListener
import me.imoltres.bbu.listeners.GameListener
import me.imoltres.bbu.listeners.JoinListener
import me.imoltres.bbu.scoreboard.BBUScoreboard
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.Command
import me.imoltres.bbu.utils.command.CommandFramework
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.util.*
import java.util.zip.GZIPOutputStream

class BBU : JavaPlugin() {

    lateinit var mainConfig: BasicConfigurationFile
    lateinit var messagesConfig: BasicConfigurationFile
    lateinit var teamSpawnsConfig: BasicConfigurationFile

    lateinit var schemesFolder: File
    lateinit var tempSchemesFolder: File

    lateinit var playerController: PlayerController
    lateinit var teamController: TeamController
    lateinit var cageController: CageController

    lateinit var commandFramework: CommandFramework

    lateinit var game: Game
    lateinit var scoreboard: BBUScoreboard

    var disabling = false
    var joinable = false
        set(value) {
            field = value

            if (value)
                Bukkit.getConsoleSender().sendMessage(CC.translate("&aServer is now joinable."))
            else {
                for (player in Bukkit.getOnlinePlayers()) {
                    player.kick(CC.translate("&cServer is no longer joinable\n\n&cTry again later."))
                }

                Bukkit.getConsoleSender().sendMessage(CC.translate("&cServer is no longer joinable."))
            }
        }

    override fun onLoad() {
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aSetting up instance..."))
        instance = this
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aLoading config files..."))
        mainConfig = BasicConfigurationFile(this, "config")
        messagesConfig = BasicConfigurationFile(this, "messages")
        teamSpawnsConfig = BasicConfigurationFile(this, "teamSpawns")

        schemesFolder = File(dataFolder, "schematics")
        tempSchemesFolder = File(schemesFolder, "tempSchems")

        if (!schemesFolder.exists() || Objects.requireNonNull(schemesFolder.listFiles()).isEmpty()) {
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("&bCreating schematics folder and/or writing default schems..."))
            writeDefaultSchems()
            tempSchemesFolder.mkdirs()
        }

        Bukkit.getConsoleSender().sendMessage(CC.translate("&aInitialising command framework..."))
        commandFramework = CommandFramework(this)

        Bukkit.getConsoleSender().sendMessage(CC.translate("&aInitialising controllers..."))
        playerController = PlayerController(this)
        teamController = TeamController(this)
        cageController = CageController(this)
    }

    override fun onDisable() {
        disabling = true
        Bukkit.getScheduler().cancelTasks(this)
        scoreboard.cleanup()

        try {
            teamSpawnsConfig.configuration.save(teamSpawnsConfig.file)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun registerCommands() {
        Command.registerCommands( //GAME STUFF
            GameCommand::class.java
        )
    }

    override fun onEnable() {
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aSetting up teams..."))
        setupTeams()
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering listeners..."))
        registerListeners()
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering commands..."))
        registerCommands()
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering game instance..."))
        game = Game()
        scoreboard = BBUScoreboard()
    }

    private fun setupTeams() {
        for (colour in BBUTeamColour.values()) {
            Bukkit.getConsoleSender().sendMessage(
                CC.translate(
                    "&aTeam '&" + colour.chatColor.char + colour.name + "&a' created " + if (teamController.createTeam(
                            colour
                        )
                    ) "successfully" else "&cunsuccessfully"
                )
            )
        }
    }

    private fun registerListeners() {
        val pluginManager = Bukkit.getPluginManager()
        pluginManager.registerEvents(BeaconListener(), this)
        pluginManager.registerEvents(DeathListener(), this)
        pluginManager.registerEvents(GameListener(), this)
        pluginManager.registerEvents(JoinListener(), this)
    }

    private fun writeDefaultSchems() {
        saveResource("schematics" + File.separator + "cage", false)
        val input = File(schemesFolder, "cage")
        val output = File(schemesFolder, "cage.schem")
        zipFile(input, output)
    }

    private fun zipFile(input: File, output: File) {
        try {
            GZIPOutputStream(FileOutputStream(output)).use { gos -> Files.copy(input.toPath(), gos) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        input.delete()
    }

    companion object {
        @JvmStatic
        lateinit var instance: BBU
    }
}