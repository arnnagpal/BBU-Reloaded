package me.imoltres.bbu;

import lombok.Getter;
import me.imoltres.bbu.commands.GameCommand;
import me.imoltres.bbu.commands.main.TrackPositionCommand;
import me.imoltres.bbu.commands.team.TeamPosCommand;
import me.imoltres.bbu.controllers.CageController;
import me.imoltres.bbu.controllers.PlayerController;
import me.imoltres.bbu.controllers.TeamController;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.game.Game;
import me.imoltres.bbu.listeners.*;
import me.imoltres.bbu.scoreboard.BBUScoreboard;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.command.Command;
import me.imoltres.bbu.utils.command.CommandFramework;
import me.imoltres.bbu.utils.config.type.BasicConfigurationFile;
import me.imoltres.bbu.utils.menu.MenuListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * Main class, used to initialise and serve as the main hub
 * for any controllers, or any other variables needed
 * to be accessed by other classes.
 *
 * @author iMoltres
 */
public class BBU extends JavaPlugin {

    //instance of this BBU object
    private static BBU instance;

    //config files
    @Getter
    private BasicConfigurationFile mainConfig;
    @Getter
    private BasicConfigurationFile messagesConfig;
    @Getter
    private BasicConfigurationFile teamSpawnsConfig;
    @Getter
    private BasicConfigurationFile nerfsConfig;

    //schems folder for cage
    @Getter
    private File schemesFolder;

    //controllers
    @Getter
    private PlayerController playerController;
    @Getter
    private TeamController teamController;
    @Getter
    private CageController cageController;

    //command stuff
    @Getter
    private CommandFramework commandFramework;

    //game stuff & scoreboard 'controller'
    @Getter
    private Game game;
    @Getter
    private BBUScoreboard scoreboard;

    //boolean to hold if the server is done setting up or not
    @Getter
    private boolean joinable = false;

    /**
     * Retrieve the instance for BBU to access
     * any public methods or getters inside of it.
     *
     * @return BBU instance
     */
    public static BBU getInstance() {
        return instance;
    }

    /**
     * Sets up the instance to be grabbed,
     * all of our folders/configs, and initialises
     * the controllers.
     */
    @Override
    public void onLoad() {
        //instance stuff
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aSetting up instance..."));
        instance = this;

        //setup configs / folders
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aLoading config files..."));

        mainConfig = new BasicConfigurationFile(this, "config");
        messagesConfig = new BasicConfigurationFile(this, "messages");
        nerfsConfig = new BasicConfigurationFile(this, "nerfs");
        teamSpawnsConfig = new BasicConfigurationFile(this, new File("world"), "teamSpawns", false);

        schemesFolder = new File(getDataFolder(), "schematics");

        if (!schemesFolder.exists() || Objects.requireNonNull(schemesFolder.listFiles()).length == 0) {
            Bukkit.getConsoleSender()
                    .sendMessage(CC.translate("&bCreating schematics folder and/or writing default schems..."));
            writeDefaultSchems();
        }

        //initialise command framework
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aInitialising command framework..."));
        commandFramework = new CommandFramework(this);

        //initialise controllers
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aInitialising controllers..."));
        playerController = new PlayerController(this);
        teamController = new TeamController(this);
        cageController = new CageController(this);
    }

    /**
     * Sets up the team instances and listeners, as well
     * as any commands. Sets up the scoreboard 'controller' and game logic too
     */
    @Override
    public void onEnable() {
        //team setup
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aSetting up teams..."));
        setupTeams();

        //register listeners
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering listeners..."));
        registerListeners();

        //register commands with the commandframework
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering commands..."));
        registerCommands();

        //setup game instance
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering game instance..."));
        game = new Game();

        //setup scoreboard 'controller'
        scoreboard = new BBUScoreboard();
    }

    /**
     * Saves the team config and cleans up all player scoreboards, stops any ongoing tasks
     */
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        scoreboard.cleanup();

        try {
            teamSpawnsConfig.getConfiguration().save(teamSpawnsConfig.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Set the joinable variable
     * if the value param is passed as 'false'
     * then all players will be kicked
     * and the server won't be able to
     * be joined.
     *
     * @param value boolean
     */
    public void setJoinable(boolean value) {
        this.joinable = value;

        if (value)
            Bukkit.getConsoleSender().sendMessage(CC.translate("&aServer is now joinable."));
        else {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kick(CC.translate("&cServer is no longer joinable\n\n&cTry again later."));
            }

            Bukkit.getConsoleSender().sendMessage(CC.translate("&cServer is no longer joinable."));
        }
    }

    /**
     * Sets up all the commands with the command framework
     */
    private void registerCommands() {
        Command.registerCommands( //GAME STUFF
                GameCommand.class,
                TrackPositionCommand.class,
                TeamPosCommand.class
        );
    }

    /**
     * Sets up all the teams according to the {@link me.imoltres.bbu.data.BBUTeamColour} class
     */
    private void setupTeams() {
        for (BBUTeamColour colour : BBUTeamColour.values()) {
            Bukkit.getConsoleSender().sendMessage(
                    CC.translate(
                            "&aTeam '&" + colour.getChatColor().getChar() + colour.name() + "&a' created " +
                                    ((teamController.createTeam(colour)) ? "successfully" : "&cunsuccessfully")
                    )
            );
        }
    }

    /**
     * Sets up all the listeners
     */
    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new BeaconListener(), this);
        pluginManager.registerEvents(new BlockListener(), this);
        pluginManager.registerEvents(new InteractListener(), this);
        pluginManager.registerEvents(new DamageListener(), this);
        pluginManager.registerEvents(new GameListener(), this);
        pluginManager.registerEvents(new JoinListener(), this);
        pluginManager.registerEvents(new NerfsListener(), this);

        pluginManager.registerEvents(new MenuListener(), this);
    }

    /**
     * Write the default schematic(s)
     * 
     * - cage.schem
     */
    private void writeDefaultSchems() {
        saveResource("schematics" + File.separator + "cage", false);
        File input = new File(schemesFolder, "cage");
        File output = new File(schemesFolder, "cage.schem");
        zipFile(input, output);
    }

    /**
     * Zip a file (deletes the input)
     *
     * @param input  input file
     * @param output destination
     */
    private void zipFile(File input, File output) {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(output))) {
            Files.copy(input.toPath(), gos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        input.delete();
    }

}