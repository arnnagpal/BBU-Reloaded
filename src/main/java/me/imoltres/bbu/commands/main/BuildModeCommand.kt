package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
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
        bbuPlayer.build = !bbuPlayer.build

        if (bbuPlayer.build) {
            player.isFlying = true
            player.allowFlight = true
            player.sendMessage(CC.translate("&7Build mode &aenabled&7."))

        } else {
            player.isFlying = false
            player.allowFlight = false
            player.sendMessage(CC.translate("&7Build mode &cdisabled&7."))

        }
    }
}