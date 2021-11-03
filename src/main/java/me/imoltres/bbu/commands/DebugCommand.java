package me.imoltres.bbu.commands;

import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.command.Command;
import me.imoltres.bbu.utils.command.CommandArgs;
import me.imoltres.bbu.utils.command.CommandInfo;
import me.imoltres.bbu.utils.command.Completer;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

@CommandInfo(
        name = "gaming",
        permission = "what.a.gamer",
        aliases = {"actually", "gamer"},
        desc = "honestly gaming tbh",
        usage = "&c/gaming [args]"
)
public class DebugCommand implements Command {

    @Override
    public void execute(CommandArgs cmd) {
        CommandSender sender = cmd.getSender();
        String[] args = cmd.getArguments();

        sender.sendMessage(CC.translate(Arrays.toString(args)));
    }

    @Override
    @Completer
    public List<String> tabCompleter(CommandArgs cmd) {
        return Arrays.asList("Jazzing amirite", "chat???");
    }

}
