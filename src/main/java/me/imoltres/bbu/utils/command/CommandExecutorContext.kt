package me.imoltres.bbu.utils.command

import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack

// https://github.com/oglassdev/KotStom/
@JvmInline
value class CommandExecutorContext(val context: CommandContext<CommandSourceStack>) {
    /**
     * Retrieves the value of a `CommandArgument` from the `CommandContext`
     *
     * dont ask me what this means
     */
    inline operator fun <reified T : Any> CommandNode.Argument<T>.invoke(): T =
        context.getArgument(this.argument.id, T::class.java)
}