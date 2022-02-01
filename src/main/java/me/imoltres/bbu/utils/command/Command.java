package me.imoltres.bbu.utils.command;

import me.imoltres.bbu.BBU;

import java.util.List;

public interface Command {

    @SafeVarargs
    static void registerCommands(Class<? extends Command>... classes) {
        CommandFramework framework = BBU.getInstance().getCommandFramework();
        framework.registerCommands(classes);

        for (Class<? extends Command> c : classes)
            System.out.println("&aRegistered command '&c" + c.getSimpleName() + "&a'");
    }

    void execute(CommandArgs cmd);

    List<SubCommand> subCommands();

    List<String> tabCompleter(CommandArgs cmd);

}
