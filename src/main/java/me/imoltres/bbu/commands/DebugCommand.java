package me.imoltres.bbu.commands;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.game.Game;
import me.imoltres.bbu.utils.command.CommandArgs;
import me.imoltres.bbu.utils.command.CommandInfo;
import me.imoltres.bbu.utils.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Method;

@CommandInfo(
        name = "bbu.debug",
        permission = "bbu.command.debug",
        desc = "Debug commands related to BBU",
        usage = "&c/bbu debug [option]"
)
public class DebugCommand implements SubCommand {

    @Override
    public void execute(CommandArgs cmd) {
        CommandSender sender = cmd.getSender();
        String[] args = cmd.getArguments();

        switch (args[0].toLowerCase()) {
            case "resetcages" -> {
                try {
                    Method method = Game.class.getDeclaredMethod("resetCages", World.class);
                    method.setAccessible(true);
                    method.invoke(BBU.getInstance().getGame(), Bukkit.getWorlds().get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            case "getcage" -> {
                sender.sendMessage(BBU.getInstance().getTeamController().getTeam(BBUTeamColour.valueOf(args[1].toUpperCase())).getCage().toString());
            }
        }
    }

}
