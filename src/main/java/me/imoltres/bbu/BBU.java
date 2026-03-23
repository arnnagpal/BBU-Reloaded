package me.imoltres.bbu;

import lombok.Getter;
import me.imoltres.bbu.commands.GameCommandKt;
import me.imoltres.bbu.commands.main.TrackPositionCommandKt;
import me.imoltres.bbu.commands.team.TeamPosCommandKt;
import me.imoltres.bbu.controllers.CageController;
import me.imoltres.bbu.controllers.PlayerController;
import me.imoltres.bbu.controllers.TeamController;
import me.imoltres.bbu.game.Game;
import me.imoltres.bbu.game.GameKt;
import me.imoltres.bbu.game.ShrinkPhase;
import me.imoltres.bbu.listeners.*;
import me.imoltres.bbu.nametags.NametagAdapterImpl;
import me.imoltres.bbu.scoreboard.BBUScoreboard;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.command.CommandManager;
import me.imoltres.bbu.utils.config.MainConfigOld;
import me.imoltres.bbu.utils.config.type.BasicConfigurationFile;
import me.imoltres.bbu.utils.general.FileUtils;
import me.imoltres.bbu.utils.json.GsonFactory;
import me.imoltres.bbu.utils.menu.MenuListener;
import me.imoltres.bbu.utils.nametag.NametagHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.DedicatedServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.Objects;

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

    //game stuff & scoreboard 'controller'
    @Getter
    private Game game;
    @Getter
    private BBUScoreboard scoreboard;

    //nametag stuff
    private NametagHandler nametagHandler;

    //boolean to hold if the server is done setting up or not
    @Getter
    private boolean joinable = false;

    private boolean deleteStaleWorldsOnShutdown = false;

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

        // check for stale worlds and delete them if they exist
        File staleWorldsFile = new File("stale_worlds.txt");
        if (staleWorldsFile.exists()) {
            try {
                var lines = Files.readAllLines(staleWorldsFile.toPath());
                for (String worldName : lines) {
                    File worldFolder = new File(worldName);
                    if (worldFolder.exists() && worldFolder.isDirectory()) {
                        FileUtils.deleteFolder(worldFolder);
                        getLogger().info("Deleted stale world: " + worldName);
                    }
                }
                // clear the file after processing
                Files.delete(staleWorldsFile.toPath());
            } catch (IOException e) {
                getLogger().severe("Failed to process stale worlds file: " + e.getMessage());
            }
        }

        //setup configs / folders
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aLoading config files..."));

        mainConfig = new BasicConfigurationFile(this, "config");
        messagesConfig = new BasicConfigurationFile(this, "messages");
        nerfsConfig = new BasicConfigurationFile(this, "nerfs");
        teamSpawnsConfig = new BasicConfigurationFile(this, new File(GameKt.WORLD_PREFIX + "_overworld"), "teamSpawns", false);

        //setup game instance
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering game instance..."));
        game = new Game();

        Bukkit.getConsoleSender().sendMessage(CC.translate("&aSetting up worlds..."));
        if (setDefaultWorld(GameKt.WORLD_PREFIX)) {
            getLogger().warning("World settings changed, shutting down...");
            this.deleteStaleWorldsOnShutdown = true;
            Bukkit.shutdown();
            Bukkit.getPluginManager().disablePlugin(this);
        }

        //print shrink phases
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aParsed shrink phases:"));
        // get the shrink phases
        // [ { size: x, length: y } .. ]
        var shrinkPhases = MainConfigOld.BORDER_PHASES;
        var i = 0;
        for (LinkedHashMap<String, Integer> phaseObj : shrinkPhases) {
            var phase = new ShrinkPhase(phaseObj.get("size"), phaseObj.get("length"));
            Bukkit.getConsoleSender().sendMessage(CC.translate("&a[" + ++i + "]  Size: " + phase.getSize() + " Length: " + phase.getLength()));
        }

        Bukkit.getConsoleSender().sendMessage(CC.translate("Actual config value: " + GsonFactory.getPrettyGson().toJson(shrinkPhases)));

        schemesFolder = new File(getDataFolder(), "schematics");

        if (!schemesFolder.exists() || Objects.requireNonNull(schemesFolder.listFiles()).length == 0) {
            Bukkit.getConsoleSender()
                    .sendMessage(CC.translate("&bCreating schematics folder and/or writing default schems..."));
            writeDefaultSchems();
        }

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
        //setup nametag handler
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aSetting up nametag handler..."));
        nametagHandler = new NametagHandler(this, new NametagAdapterImpl());

        //team setup
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aSetting up teams..."));
        teamController.setupTeams();

        //register listeners
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering listeners..."));
        registerListeners();

        //register commands with the command manager
        Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistering commands..."));
        registerCommands();

        // setup game
        game.setupLobbyWorld();
        game.setupWorlds();

        //setup scoreboard 'controller'
        scoreboard = new BBUScoreboard();
    }

    /**
     * Saves the team config and cleans up all player scoreboards, stops any ongoing tasks
     */
    @Override
    public void onDisable() {
        joinable = false;

        if (deleteStaleWorldsOnShutdown) {
            // make a note to delete any worlds with the prefix of the old level name so that
            // the plugin can delete it onload the next time server boots
            var server = (DedicatedServer) MinecraftServer.getServer();
            var props = server.getProperties();
            var oldLevelName = props.levelName;

            // levels to delete
            // - oldLevelName
            // - oldLevelName_nether
            // - oldLevelName_the_end

            File file = new File("stale_worlds.txt");
            try {
                if (!file.exists()) {
                    file.createNewFile();
                }
                Files.write(file.toPath(),
                        (oldLevelName + System.lineSeparator() +
                                oldLevelName + "_nether" + System.lineSeparator() +
                                oldLevelName + "_the_end").getBytes(),
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING
                );
            } catch (IOException e) {
                getLogger().severe("Failed to mark world for deletion: " + e.getMessage());
            }
        }

        if (cageController != null) {
            cageController.cleanupBlocking();
        }

        if (nametagHandler != null) {
            nametagHandler.cleanup();
        }

        Bukkit.getScheduler().cancelTasks(this);
        if (scoreboard != null) {
            scoreboard.cleanup();
        }

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
        // if the value is changing and is being set to true, send a message in console
        if (joinable != value && value)
            Bukkit.getConsoleSender().sendMessage(CC.translate("&aServer is now joinable."));
        else if (!value) { // if the server is no longer joinable, kick all players and send a message in console
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.kick(CC.translate("&cServer is no longer joinable\n\n&cTry again later."));
            }

            Bukkit.getConsoleSender().sendMessage(CC.translate("&cServer is no longer joinable."));
        }

        // update the joinable variable
        this.joinable = value;
    }

    /**
     * Sets up all the commands with the command framework
     */
    private void registerCommands() {
        CommandManager.INSTANCE.registerCommands(
                GameCommandKt.getGameCommand(),
                TrackPositionCommandKt.getTrackPositionCommand(),
                TeamPosCommandKt.getTeamPosCommand()
        );
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
        pluginManager.registerEvents(new ChatListener(), this);
        pluginManager.registerEvents(new RespawnListener(), this);
        pluginManager.registerEvents(new WeatherListener(), this);

        pluginManager.registerEvents(new PortalListener(), this);

        //for menus
        pluginManager.registerEvents(new MenuListener(), this);
    }

    /**
     * Write the default schematic(s)
     * <p>
     * - cage.schem
     */
    private void writeDefaultSchems() {
        saveResource("schematics" + File.separator + "cage.schem", false);
    }

    private boolean setDefaultWorld(String worldName) {
        // change in server.properties
        var server = (DedicatedServer) MinecraftServer.getServer();
        var props = server.getProperties();

        if (!props.levelName.equalsIgnoreCase(worldName)) {
            props.properties.setProperty("level-name", worldName);
            props.store(server.getFile("server.properties"));
            getLogger().info("Set default world to " + worldName + " in server.properties.");
            return true;
        }

        return false; // false = does not need restart
    }

}