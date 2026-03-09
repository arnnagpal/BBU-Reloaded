package me.imoltres.bbu.utils.command.condition

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender

// https://github.com/Minestom/Minestom/blob/master/src/main/java/net/minestom/server/command/builder/condition/
fun interface CommandCondition {
    fun canUse(sender: CommandSender, command: Command<CommandSourceStack>): Boolean
}