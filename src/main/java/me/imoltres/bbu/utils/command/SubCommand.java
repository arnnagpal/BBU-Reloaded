package me.imoltres.bbu.utils.command;

/**
 * Represents a child command of a {@link me.imoltres.bbu.utils.command.Command}
 */
public interface SubCommand {

    void execute(CommandArgs cmd);

}
