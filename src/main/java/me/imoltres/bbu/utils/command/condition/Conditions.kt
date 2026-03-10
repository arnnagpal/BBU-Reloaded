package me.imoltres.bbu.utils.command.condition

import org.bukkit.command.CommandSender

// https://github.com/Minestom/Minestom/blob/master/src/main/java/net/minestom/server/command/builder/condition
class Conditions {
    companion object {
        fun all(vararg conditions: CommandCondition): CommandCondition {
            return CommandCondition { sender ->
                for (condition in conditions) {
                    if (!condition.canUse(sender)) {
                        return@CommandCondition false
                    }
                }
                true
            }
        }

        fun all(vararg conditions: PermissionCommandCondition): CommandCondition {
            return CommandCondition { sender ->
                for (condition in conditions) {
                    if (!condition.canUse(sender)) {
                        return@CommandCondition false
                    }
                }

                true
            }
        }

        fun permission(permission: String): PermissionCommandCondition {
            return PermissionCommandCondition { permission }
        }

        fun any(vararg conditions: CommandCondition): CommandCondition {
            return CommandCondition { sender ->
                for (condition in conditions) {
                    if (condition.canUse(sender)) {
                        return@CommandCondition true
                    }
                }
                false
            }
        }

        fun playerOnly(sender: CommandSender): Boolean {
            return sender is org.bukkit.entity.Player
        }

        fun consoleOnly(sender: CommandSender): Boolean {
            return sender !is org.bukkit.entity.Player
        }

        fun not(condition: CommandCondition): CommandCondition {
            return CommandCondition { sender -> !condition.canUse(sender) }
        }
    }
}