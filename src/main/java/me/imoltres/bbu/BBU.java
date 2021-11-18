package me.imoltres.bbu;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import lombok.Getter;
import me.imoltres.bbu.commands.GameCommand;
import me.imoltres.bbu.controllers.PlayerController;
import me.imoltres.bbu.controllers.TeamController;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.game.Game;
import me.imoltres.bbu.listeners.JoinListener;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.command.Command;
import me.imoltres.bbu.utils.command.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

public class BBU extends JavaPlugin {

    private static BBU instance;

    @Getter
    private BasicConfigurationFile mainConfig;
    @Getter
    private BasicConfigurationFile messagesConfig;
    @Getter
    private BasicConfigurationFile teamSpawnsConfig;

    @Getter
    private File schemsFolder;

    @Getter
    private File tempSchemsFolder;

    @Getter
    private PlayerController playerController;
    @Getter
    private TeamController teamController;

    @Getter
    private CommandFramework commandFramework;

    @Getter
    private Game game;

    @Getter
    private boolean disabling = false;

    @Override
    public void onLoad() {
        println("&aSetting up instance...");
        instance = this;

        println("&aLoading config files...");
        mainConfig = new BasicConfigurationFile(this, "config");
        messagesConfig = new BasicConfigurationFile(this, "messages");
        teamSpawnsConfig = new BasicConfigurationFile(this, "teamSpawns");

        schemsFolder = new File(this.getDataFolder(), "schematics");
        tempSchemsFolder = new File(schemsFolder, "tempSchems");
        if (!schemsFolder.exists() || Objects.requireNonNull(schemsFolder.listFiles()).length == 0) {
            println("&bCreating schematics folder and/or writing default schems...");
            writeDefaultSchems();

            tempSchemsFolder.mkdirs();
        }

        println("&aInitialising command framework...");
        commandFramework = new CommandFramework(this);

        println("&aInitialising controllers...");
        playerController = new PlayerController(this);
        teamController = new TeamController(this);

    }

    //for some reason kotlin doesn't want to recognise the getter on the variable
    public static BBU getInstance() {
        return instance;
    }

    @Override
    public void onDisable() {
        disabling = true;

        Bukkit.getScheduler().cancelTasks(this);
        try {
            teamSpawnsConfig.getConfiguration().save(teamSpawnsConfig.getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void registerCommands() {
        Command.registerCommands(
                //GAME STUFF
                GameCommand.class
        );
    }

    @Override
    public void onEnable() {
        println("&aSetting up teams...");
        setupTeams();

        println("&aRegistering listeners...");
        registerListeners();

        println("&aRegistering commands...");
        registerCommands();

        println("&aRegistering game instance...");
        game = new Game();


    }

    private void setupTeams() {
        for (BBUTeamColour colour : BBUTeamColour.values()) {
            println("&aTeam '&" + colour.getChatColor().getChar() + colour.name() + "&a' created " + (teamController.createTeam(colour) ? "successfully" : "&cunsuccessfully"));
        }
    }

    private void writeDefaultSchems() {
        saveResource("schematics" + File.separator + "cage", false);
        File input = new File(getSchemsFolder(), "cage");
        File output = new File(getSchemsFolder(), "cage.schem");
        zipFile(input, output);
    }

    private void zipFile(File input, File output) {
        try (GZIPOutputStream gos = new GZIPOutputStream(new FileOutputStream(output))) {
            Files.copy(input.toPath(), gos);
        } catch (IOException e) {
            e.printStackTrace();
        }

        input.delete();
    }

    public void println(String... lines) {
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(CC.translate(line));
        }
    }

    private void registerListeners() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new JoinListener(), this);
    }

}
