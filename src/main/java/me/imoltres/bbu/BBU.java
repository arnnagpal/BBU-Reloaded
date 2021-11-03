package me.imoltres.bbu;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import lombok.Getter;
import me.imoltres.bbu.commands.DebugCommand;
import me.imoltres.bbu.controllers.PlayerController;
import me.imoltres.bbu.controllers.TeamController;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.command.Command;
import me.imoltres.bbu.utils.command.CommandFramework;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BBU extends JavaPlugin {

    @Getter
    private static BBU instance;

    @Getter
    private BasicConfigurationFile mainConfig;
    @Getter
    private BasicConfigurationFile messagesConfig;

    @Getter
    private PlayerController playerController;
    @Getter
    private TeamController teamController;

    @Getter
    private CommandFramework commandFramework;

    @Override
    public void onLoad() {
        println("&aSetting up instance...");
        instance = this;
        println("&aDone!");

        println("&aLoading config files...");
        mainConfig = new BasicConfigurationFile(this, "config");
        messagesConfig = new BasicConfigurationFile(this, "messages");

        println("&aInitialising command framework...");
        commandFramework = new CommandFramework(this);

        println("&aInitialising controllers...");
        playerController = new PlayerController(this);
        teamController = new TeamController(this);

        println("&aSetting up team colours...");
        setupTeams();
    }

    @Override
    public void onEnable() {
        println("&aRegistering commands...");
        registerCommands();
    }

    @Override
    public void onDisable() {

    }

    private void registerCommands() {
        Command.registerCommands(
                DebugCommand.class
        );
    }

    private void setupTeams() {
        for (BBUTeamColour colour : BBUTeamColour.values()) {
            println("&aTeam '&" + colour.getChatColor().getChar() + colour.name() + "&a' created " + (teamController.createTeam(colour) ? "successful" : "&cunsuccessfully"));
        }
    }

    public void println(String... lines) {
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(CC.translate(line));
        }
    }

}
