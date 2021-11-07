package me.imoltres.bbu.commands;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.command.*;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CommandInfo(
        name = "bbu",
        permission = "bbu.command.help",
        desc = "BBU Main help command, displays all commands with arguments",
        usage = "&cUsage: /bbu"
)
public class GameCommand implements Command {

    @Override
    public void execute(CommandArgs cmd) {
        CommandSender sender = cmd.getSender();

        for (Component component : getHelpMessage()) {
            sender.sendMessage(component);
        }
    }

    @Override
    public List<SubCommand> subCommands() {
        return Arrays.asList(
                new DebugCommand()
        );
    }

    @Override
    @Completer
    public List<String> tabCompleter(CommandArgs cmd) {
        String[] args = cmd.getArguments();
        List<String> completions = new ArrayList<>();
        List<String> options = new ArrayList<>();

        switch (args.length) {
            case 1 -> options.addAll(Arrays.asList(
                    "buildmode",
                    "start",
                    "pause",
                    "stop",
                    "teams",
                    "team",
                    "player",
                    "debug"
            ));

            case 2 -> {
                String prev = args[0];
                switch (prev.toLowerCase()) {
                    case "teams" -> options.add("clear");

                    case "team" -> {
                        for (BBUTeam team : BBU.getInstance().getTeamController().getAllTeams()) {
                            options.add(team.getColour().name());
                        }
                    }

                    case "player" -> {
                        options.addAll(Arrays.asList(
                                "revive",
                                "eliminate"
                        ));
                        options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet()));
                    }

                    case "debug" -> options.addAll(Arrays.asList(
                            "invsee",
                            "givebeacon",
                            "givetracker"
                    ));
                }
            }

            case 3 -> {
                String parentCommand = args[0];
                switch (parentCommand.toLowerCase()) {
                    case "team" -> {
                        options.addAll(Arrays.asList(
                                "revive",
                                "eliminate"
                        ));
                    }

                    case "player" -> options.addAll(Arrays.asList(
                            "jointeam",
                            "leaveteam"
                    ));

                    case "debug" -> options.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toSet()));
                }
            }

            case 4 -> {
                String parentCommand = args[0];
                switch (parentCommand.toLowerCase()) {
                    case "eliminate", "invsee" -> {
                        options.add("true");
                        options.add("false");
                    }
                }
            }
        }

        StringUtil.copyPartialMatches(args[args.length - 1], options, completions);

        return completions;
    }

    private List<Component> getHelpMessage() {
        return CC.translate(Arrays.asList(
                "&3&lBBU Help Menu &8- ",
                "&3MAIN: ",
                "  &b* /bbu buildmode &8- &7Enables building/interacting with the game world.",
                "  &b* /bbu start &8- &7What do you think it does?",
                "  &b* /bbu pause &8- &7What do you think it does?",
                "  &b* /bbu stop &8- &7What do you think it does?",
                "",
                "&3TEAM: ",
                "  &b* /bbu teams &8- &7Lists all the teams with players in them.",
                "  &b* /bbu teams clear &8- &7Clears all teams' players.",
                "  &b* /bbu team <team> revive &8- &7Respawns the team's beacon, and reviving any dead teammates.",
                "  &b* /bbu team <team> eliminate [players] &8- &7Eliminates the team's beacon, and if specified, eliminates any players inside of that team.",
                "",
                "&3PLAYER: ",
                "  &b* /bbu player revive &8- &7Allows the player to come back into the game *if* they got eliminated.",
                "  &b* /bbu player eliminate &8- &7Eliminates the player.",
                "  &b* /bbu player <player> jointeam <team> &8- &7Adds <player> to team <team>.",
                "  &b* /bbu player <player> leaveteam &8- &7Removes <player> from whatever team they were previously in.",
                "",
                "&3DEBUG: ",
                "  &b* /bbu debug invsee <player> [armour] &8- &7Look/Edit another player's inventory live, [armour] should be true or false if you want to see the player's armour (or not)",
                "  &b* /bbu debug givebeacon <player> &8- &7Gives a beacon to another player (Will invalidate any other beacons for that team if placed.)",
                "  &b* /bbu debug givetracker <player> &8- &7Gives a tracker to another player (only use for debug purposes/person somehow loses it.)",
                "",
                "Made by iUwutres &c\u2665"
        ));
    }

}
