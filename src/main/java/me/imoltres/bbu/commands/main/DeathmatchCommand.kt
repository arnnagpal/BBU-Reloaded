package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandInfo(
    name = "bbu.deathmatch",
    permission = "bbu.command.deathmatch",
    desc = "Sets the game to deathmatch, will remove all beacons",
    usage = "&c/bbu deathmatch",
    senderType = CommandInfo.SenderType.BOTH
)
class DeathmatchCommand : SubCommand {
    override fun execute(cmd: CommandArgs?) {
        val sender = cmd?.getSender<Player>()

        sender?.sendMessage(CC.translate("&aStarted deathmatch procedure."))
        BBU.getInstance().game.gameState = GameState.DEATH_MATCH
    }
}