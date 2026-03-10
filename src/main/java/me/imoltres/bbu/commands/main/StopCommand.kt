package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command
import org.bukkit.entity.Player

val StopCommand = command(
    "stop",
) {
    permission("bbu.command.stop")
    onlyPlayers()

    defaultExecutor { player ->
        player as Player
        player.sendMessage(CC.translate("&aAttempting to stop the game..."))

        try {
            BBU.getInstance().game.stopGame(null)
            player.sendMessage(CC.translate("&aStopped successfully."))
        } catch (e: Exception) {
            player.sendMessage("&c" + e.message)
        }
    }
}