package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.command.CommandSender

@CommandInfo(
    name = "bbu.movetocages",
    permission = "bbu.command.movetocages",
    desc = "Moves players from the lobby to the cages",
    usage = "&c/bbu movetocages",
    senderType = CommandInfo.SenderType.BOTH
)
class MoveToCagesCommand : SubCommand {
    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<CommandSender>()

        sender.sendMessage(CC.translate("&aStarted pre lobby procedure."))
        BBU.getInstance().game.preLobby(sender)
    }

}
