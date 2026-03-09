package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command
import org.bukkit.entity.Player

val BuildModeCommand = command(
    "buildmode",
    "build",
) {
    permission("bbu.command.buildmode")

    onlyPlayers()

    defaultExecutor { player ->
        player as Player
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
        bbuPlayer.build = !bbuPlayer.build
        player.sendMessage(CC.translate("&7Build mode is now ${if (bbuPlayer.build) "&aenabled" else "&cdisabled"}&7."))
        bbuPlayer.build = !bbuPlayer.build

        if (bbuPlayer.build) {
            player.isFlying = true
            player.allowFlight = true
        } else {
            player.isFlying = false
            player.allowFlight = false
        }
    }
}