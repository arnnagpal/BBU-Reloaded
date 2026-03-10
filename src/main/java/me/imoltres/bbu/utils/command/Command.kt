package me.imoltres.bbu.utils.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.condition.CommandCondition
import me.imoltres.bbu.utils.command.condition.Conditions
import me.imoltres.bbu.utils.command.condition.PermissionCommandCondition
import me.imoltres.bbu.utils.command.condition.toPaper
import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender

inline val NO_PERMISSION_MESSAGE: TextComponent
    get() = CC.translate("&cYou do not have permission to execute this command.")

// kotstom https://github.com/oglassdev/KotStom/blob/55917769ba8370b27749124602106c5d3b65b6ac/src/main/kotlin/net/bladehunt/kotstom/dsl/kommand/Kommand.kt

internal typealias SingleCommandExecutor = CommandExecutorContext.(CommandSender) -> Unit
internal typealias CommandExecutor = CommandExecutorContext.(CommandSender, CommandContext<CommandSourceStack>) -> Unit

@DslMarker
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
annotation class CommandDSL

@CommandDSL
class Command {
    val name: String
    var requirement: CommandCondition?
    var permissionRequirement: CommandCondition?

    constructor(
        name: String,
        vararg aliases: String,
        requirement: CommandCondition? = null,
        permissionRequirement: PermissionCommandCondition? = null
    ) {
        this.name = name
        this.requirement = requirement
        this.permissionRequirement = permissionRequirement
        this.root = Commands.literal(name)
        this.aliases = aliases.toList()
    }

    val root: LiteralArgumentBuilder<CommandSourceStack>
    internal var aliases: List<String>
    internal var description: String = ""

    fun description(description: String) {
        this.description = description
    }

    fun aliases(vararg aliases: String) {
        this.aliases += aliases.toList()
    }

    // pred
    /**
     * Adds a requirement to the command syntax that only allows players to execute the command.
     *
     * This is a shorthand for `requires(Conditions.playerOnly)`.
     */
    fun onlyPlayers() {
        requires(CommandCondition(Conditions::playerOnly))
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
     * Adds a requirement to the command syntax that only allows senders with the specified permission to execute the command.
     *
     * This is a shorthand for `requires(Conditions.permission(permission))`.
     */
    fun permission(permission: String) {
        val predicate = PermissionCommandCondition { permission }

        permissionRequirement = if (permissionRequirement != null) {
            Conditions.all(permissionRequirement!!, Conditions.permission(permission))
        } else predicate
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
    inline fun defaultExecutor(
        crossinline block: @CommandDSL CommandExecutor
    ) {
        val pred = requirement

        if (permissionRequirement != null) {
            root.requires(permissionRequirement!!.toPaper())
        }

        root.executes { context ->
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
    inline fun defaultExecutor(crossinline block: @CommandDSL SingleCommandExecutor) {
        val pred = requirement

        if (permissionRequirement != null) {
            root.requires(permissionRequirement!!.toPaper())
        }

        root.executes { context ->
            val sender = context.source.sender
            if (pred == null || pred.canUse(sender)) {
                CommandExecutorContext(context).block(sender)
            } else {
                sender.sendMessage(NO_PERMISSION_MESSAGE)
            }

            1 // Return a success code
        }
    }

    @CommandDSL
    inline fun subcommand(
        name: String,
        vararg aliases: String,
        block: @CommandDSL Command.() -> Unit
    ) {
        val child = Command(name, *aliases).apply(block).also {
            root.then(it.root)
        }
    }

    @CommandDSL
    fun subcommand(
        vararg child: Command
    ) {
        for (c in child) {
            root.then(c.root)
        }
    }

    @CommandDSL
    inline fun buildSyntax(
        vararg args: CommandNode,
        crossinline block: @CommandDSL CommandSyntaxDSL.() -> Unit
    ) {
        if (args.isEmpty()) {
            return
        }

        // Build the argument chain
        val nodes = args.map { node ->
            when (node) {
                is CommandNode.Literal -> Commands.literal(node.literal)
                is CommandNode.Argument<*> -> Commands.argument(node.argument.id, node.argument.type)
            }
        }

        val syntax = CommandSyntaxDSL(nodes.last()) // last bc inner -> outer
        syntax.block()

        // good god i have no clue what this means
        // * magic *
        val chain = nodes.dropLast(1).foldRight(nodes.last()) { node, acc ->
            node.then(acc)
            node
        }

        root.then(chain)
    }
}


