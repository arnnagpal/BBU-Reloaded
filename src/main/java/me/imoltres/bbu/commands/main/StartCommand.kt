package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command
import org.bukkit.entity.Player

val StartCommand = command(
    "start",
) {
    permission("bbu.command.start")
    onlyPlayers()

    defaultExecutor { player ->
        player as Player
        player.sendMessage(CC.translate("&aAttempting to start the game..."))

        try {
            BBU.getInstance().game.startGame()
            player.sendMessage(CC.translate("&aStarted successfully."))
        } catch (e: Exception) {
            player.sendMessage("&c" + e.message)
        }
    }
}