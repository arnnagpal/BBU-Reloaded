package me.imoltres.bbu.utils.command;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.utils.CC;
import org.bukkit.Bukkit;

import java.util.List;

/**
 * Represents a plugin.yml-less command
 */
public interface Command {

    /**
     * Register multiple commands at once by passing in their classes
     *
     * @param classes list of classes
     */
    @SafeVarargs
    static void registerCommands(Class<? extends Command>... classes) {
        CommandFramework framework = BBU.getInstance().getCommandFramework();
        framework.registerCommands(classes);

        for (Class<? extends Command> c : classes)
            Bukkit.getConsoleSender().sendMessage(CC.translate("&aRegistered command '&c" + c.getSimpleName() + "&a'"));
    }

    /**
     * Called when the command has been executed by a sender
     *
     * @param cmd arguments of the command
     */
    void execute(CommandArgs cmd);

    /**
     * Returns a list of any {@link me.imoltres.bbu.utils.command.SubCommand} that are linked to this command.
     *
     * @return
     */
    List<SubCommand> subCommands();

    /**
     * Returns a list of tab completions when requested by the command
     *
     * @param cmd arguments of the tab completion
     * @return a list of possible completions
     */
    List<String> tabCompleter(CommandArgs cmd);

}
