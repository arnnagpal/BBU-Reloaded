package me.imoltres.bbu.commands.game;

import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.command.Command;
import me.imoltres.bbu.utils.command.CommandArgs;
import me.imoltres.bbu.utils.command.CommandInfo;
import me.imoltres.bbu.utils.command.Completer;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

@CommandInfo(
        name = "bbu",
        permission = "bbu.command.help",
        desc = "BBU Main help command, displays all commands with arguments",
        usage = "&c/bbu"
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
    @Completer
    public List<String> tabCompleter(CommandArgs cmd) {
        return null;
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
                "  &b* /bbu player <player> team join <team> &8- &7Adds <player> to team <team>.",
                "  &b* /bbu player <player> team leave &8- &7Removes <player> from whatever team they were previously in.",
                "",
                "&3DEBUG: ",
                "  &b* /bbu debug invsee <player> [armour{true/false}]&8- &7Look/Edit another player's inventory live, [armour] should be true or false if you want to see the player's armour (or not)",
                "  &b* /bbu debug givebeacon <player> &8- &7Gives a beacon to another player (Will invalidate any other beacons for that team if placed.)",
                "  &b* /bbu debug givetracker <player> &8- &7Gives a tracker to another player (only use for debug purposes/person somehow loses it.)",
                "",
                "Made by iUwutres &c\u2665"
        ));
    }

}
