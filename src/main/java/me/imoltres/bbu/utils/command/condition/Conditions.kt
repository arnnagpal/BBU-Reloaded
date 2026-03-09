package me.imoltres.bbu.utils.command.condition

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender

// https://github.com/Minestom/Minestom/blob/master/src/main/java/net/minestom/server/command/builder/condition
class Conditions {
    companion object {
        fun all(vararg conditions: CommandCondition): CommandCondition {
            return CommandCondition { sender, cmdString ->
                for (condition in conditions) {
                    if (!condition.canUse(sender, cmdString)) {
                        return@CommandCondition false
                    }
                }
                true
            }
        }

        fun permission(permission: String): CommandCondition {
            return CommandCondition { sender, cmdString -> sender.hasPermission(permission) }
        }

        fun any(vararg conditions: CommandCondition): CommandCondition {
            return CommandCondition { sender, cmdString ->
                for (condition in conditions) {
                    if (condition.canUse(sender, cmdString)) {
                        return@CommandCondition true
                    }
                }
                false
            }
        }

        fun playerOnly(sender: CommandSender, commandString: Command<CommandSourceStack>): Boolean {
            return sender is org.bukkit.entity.Player
        }

        fun consoleOnly(sender: CommandSender, commandString: Command<CommandSourceStack>): Boolean {
            return sender !is org.bukkit.entity.Player
        }

        fun not(condition: CommandCondition): CommandCondition {
            return CommandCondition { sender, cmdString -> !condition.canUse(sender, cmdString) }
        }
    }
}