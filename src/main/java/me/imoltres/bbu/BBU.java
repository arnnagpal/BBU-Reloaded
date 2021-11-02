package me.imoltres.bbu;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import lombok.Getter;
import me.imoltres.bbu.controllers.PlayerController;
import me.imoltres.bbu.controllers.TeamController;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.utils.CC;
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
    private BasicConfigurationFile teamColoursConfig;

    @Getter
    private PlayerController playerController;
    @Getter
    private TeamController teamController;

    @Override
    public void onLoad() {
        println("&aSetting up instance...");
        instance = this;
        println("&aDone!");

        println("&aLoading config files...");
        mainConfig = new BasicConfigurationFile(this, "config");
        messagesConfig = new BasicConfigurationFile(this, "messages");
        teamColoursConfig = new BasicConfigurationFile(this, "team-colours");

        println("&aInitialising controllers...");
        playerController = new PlayerController(this);
        teamController = new TeamController(this);

        println("&aSetting up team colours...");
        setupTeams();
    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }

    private void setupTeams() {
        for (BBUTeamColour colour : BBUTeamColour.values()) {
            println("&aTeam '" + colour.name() + "' created " + (teamController.createTeam(colour) ? "successful" : "&cunsuccessfully"));
        }
    }

    private void println(String... lines) {
        for (String line : lines) {
            Bukkit.getConsoleSender().sendMessage(CC.translate(line));
        }
    }

}
