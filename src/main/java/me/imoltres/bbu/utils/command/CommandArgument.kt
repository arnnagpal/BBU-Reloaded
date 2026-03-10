package me.imoltres.bbu.utils.command

import com.mojang.brigadier.arguments.*
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import kotlin.reflect.KClass

class CommandArgument<T : Any>(
    val id: String,
    val type: ArgumentType<T>,

    // internally used for type safety when retrieving the argument value from the CommandContext
    val clazz: KClass<out T>
) {
    companion object {
        inline operator fun <reified T : Any> invoke(id: String, type: ArgumentType<T>) =
            CommandNode.Argument(CommandArgument(id, type, T::class))
    }
}

sealed class CommandNode {
    class Literal(val literal: String) : CommandNode()
    class Argument<T : Any>(val argument: CommandArgument<T>) : CommandNode()
}

inline fun <reified T : Any> argument(name: String, type: ArgumentType<T>) =
    CommandArgument(name, type)

fun literal(name: String) = CommandNode.Literal(name)

fun argumentInt(name: String) =
    CommandArgument(name, IntegerArgumentType.integer())

fun argumentString(name: String) =
    CommandArgument(name, StringArgumentType.string())

fun argumentPlayer(name: String) =
    CommandArgument(name, ArgumentTypes.player())

fun argumentDouble(name: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE) =
    CommandArgument(name, DoubleArgumentType.doubleArg(min, max))

fun argumentLocation(name: String) =
    CommandArgument(name, ArgumentTypes.finePosition())

fun argumentBoolean(name: String) =
    CommandArgument(name, BoolArgumentType.bool())

fun argumentWorld(name: String) =
    CommandArgument(name, ArgumentTypes.world())