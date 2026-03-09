package me.imoltres.bbu.utils.command

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.command.condition.CommandCondition

object CommandManager {
    fun registerCommands(vararg commands: Command) {
        for (cmd in commands) {
            BBU.getInstance().lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
                event.registrar().register(
                    cmd.root.build(),
                    cmd.description,
                    cmd.aliases
                )
            }
        }
    }
}

@CommandDSL
inline fun command(
    name: String,
    vararg aliases: String,
    requirement: CommandCondition? = null,
    block: @CommandDSL Command.() -> Unit
): Command =
    Command(name, *aliases, requirement = requirement).apply(block)
