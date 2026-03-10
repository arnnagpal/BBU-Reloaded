package me.imoltres.bbu.utils.command

import com.mojang.brigadier.builder.ArgumentBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import me.imoltres.bbu.utils.command.condition.CommandCondition
import me.imoltres.bbu.utils.command.condition.Conditions
import me.imoltres.bbu.utils.command.condition.toPaper

@CommandDSL
class CommandSyntaxDSL(
    var builder: ArgumentBuilder<CommandSourceStack, *>
) {
    var requirement: CommandCondition? = null
    var permissionRequirement: CommandCondition? = null

    /**
     * Adds a requirement to the command syntax that only allows players to execute the command.
     *
     * This is a shorthand for `requires(Conditions.playerOnly)`.
     */
    fun onlyPlayers() {
        requires(CommandCondition(Conditions::playerOnly))
    }

    /**
     * Adds a requirement to the command syntax that only allows senders with the specified permission to execute the command.
     */
    fun permission(permission: String) {
        val permCondition = CommandCondition { sender ->
            sender.hasPermission(permission)
        }
        permissionRequirement = if (permissionRequirement != null) {
            Conditions.all(permissionRequirement!!, permCondition)
        } else permCondition

    }

    /**
     * Adds a requirement to the command syntax that only allows the console to execute the command.
     *
     * This is a shorthand for `requires(Conditions.consoleOnly)`.
     */
    fun onlyConsole() {
        requires(CommandCondition(Conditions::consoleOnly))
    }

    /**
     * Adds a requirement to the command syntax.
     * If a requirement already exists, the new requirement will be combined with the existing one using a logical AND.
     *
     * @param predicate The condition that must be met for the command to be executed.
     */
    fun requires(predicate: CommandCondition) {
        requirement = if (requirement != null) {
            Conditions.all(requirement!!, predicate)
        } else predicate
    }

    @CommandDSL
    inline fun executor(
        crossinline block: @CommandDSL CommandExecutor
    ) {
        val pred = requirement

        if (permissionRequirement != null) {
            builder.requires(permissionRequirement!!.toPaper())
        }

        builder
            .executes { context ->
            val sender = context.source.sender
                if (pred == null || pred.canUse(sender)) {
                CommandExecutorContext(context).block(sender, context)
            } else {
                sender.sendMessage(NO_PERMISSION_MESSAGE)
            }

            1 // Return a success code
        }
    }

    @CommandDSL
    inline fun executor(
        crossinline block: @CommandDSL SingleCommandExecutor
    ) {
        val pred = requirement

        if (permissionRequirement != null) {
            builder.requires(permissionRequirement!!.toPaper())
        }

        builder
            .executes { context ->
            val sender = context.source.sender
                if (pred == null || pred.canUse(sender)) {
                CommandExecutorContext(context).block(sender)
            } else {
                sender.sendMessage(NO_PERMISSION_MESSAGE)
            }

            1 // Return a success code
        }
    }

}