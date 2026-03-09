package me.imoltres.bbu.commands.main

import me.imoltres.bbu.utils.command.argumentBoolean
import me.imoltres.bbu.utils.command.argumentLocation
import me.imoltres.bbu.utils.command.argumentWorld
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.general.PlayerUtils
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.entity.Player

val TrackPositionCommand = command(
    "trackpos",
    "trackposition",
    "bbuteampos"
) {
    val loc = argumentLocation("location")
    val world = argumentWorld("world")
    val sendMsg = argumentBoolean("validate")
    onlyPlayers()

    defaultExecutor { sender ->
        sender as Player
        sender.sendMessage("Usage: /trackpos <location> <validationMsg [true|false]>")
    }

    buildSyntax(loc, world, sendMsg) {
        executor { player, ctx ->
            player as Player
            val location = loc().resolve(ctx.source)
            val sendMessage = sendMsg()

            PlayerUtils.trackPosition(player, WorldPosition.fromFinePosition(location, world()), sendMessage)
        }
    }
}