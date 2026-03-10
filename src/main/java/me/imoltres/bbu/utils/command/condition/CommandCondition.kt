package me.imoltres.bbu.utils.command.condition

import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender

// https://github.com/Minestom/Minestom/blob/master/src/main/java/net/minestom/server/command/builder/condition/
fun interface CommandCondition {
    fun canUse(sender: CommandSender): Boolean
}

fun interface PermissionCommandCondition : CommandCondition {
    fun permission(): String

    override fun canUse(sender: CommandSender): Boolean {
        return sender.hasPermission(permission())
    }
}

fun CommandCondition.toPaper(): (CommandSourceStack) -> Boolean {
    return { sender -> canUse(sender.sender) }
}