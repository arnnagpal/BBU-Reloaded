package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.entity.Player

@CommandInfo(
    name = "bbu.buildmode",
    permission = "bbu.command.buildmode",
    desc = "Toggle build mode",
    usage = "&c/bbu buildmode",
    senderType = CommandInfo.SenderType.PLAYER
)
class BuildModeCommand : SubCommand {
    override fun execute(cmd: CommandArgs) {
        val player = cmd.getSender<Player>()
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
        bbuPlayer.isBuild = true
    }
}