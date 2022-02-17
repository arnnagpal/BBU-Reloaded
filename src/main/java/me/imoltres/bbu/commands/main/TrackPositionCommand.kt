package me.imoltres.bbu.commands.main

import me.imoltres.bbu.utils.command.Command
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import me.imoltres.bbu.utils.general.PlayerUtils
import me.imoltres.bbu.utils.world.WorldPosition

@CommandInfo(
    name = "trackposition",
    senderType = CommandInfo.SenderType.PLAYER
)
class TrackPositionCommand : Command {
    override fun execute(cmd: CommandArgs) {
        PlayerUtils.trackPosition(cmd.getSender(), parseLocation(cmd.arguments), cmd.arguments[4].toBoolean())
    }

    override fun subCommands(): MutableList<SubCommand> {
        return arrayListOf()
    }

    override fun tabCompleter(cmd: CommandArgs?): MutableList<String> {
        return arrayListOf()
    }

    private fun parseLocation(args: Array<String>): WorldPosition {
        return WorldPosition(args[0].toDouble(), args[1].toDouble(), args[2].toDouble(), args[3])
    }

}