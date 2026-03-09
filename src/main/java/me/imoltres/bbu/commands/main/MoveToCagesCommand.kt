package me.imoltres.bbu.commands.main

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command

val MoveToCagesCommand = command(
    "movetocages",
    "tpcages",
) {
    permission("bbu.commands.movetocages")

    defaultExecutor { sender ->
        sender.sendMessage(CC.translate("&aStarted pre lobby procedure."))
        BBU.getInstance().game.preLobby(sender)
    }
}