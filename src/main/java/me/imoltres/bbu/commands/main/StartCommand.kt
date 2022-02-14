package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.entity.Player

@CommandInfo(
    name = "bbu.start",
    permission = "bbu.command.start"
)
class StartCommand : SubCommand {
    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<Player>()
        sender.sendMessage(CC.translate("&aAttempting to start the game..."))

        try {
            BBU.getInstance().game.startGame()
            sender.sendMessage(CC.translate("&aStarted successfully."))
        } catch (e: Exception) {
            sender.sendMessage("&c" + e.message)
        }
    }
}