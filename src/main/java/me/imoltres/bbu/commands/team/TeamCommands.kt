package me.imoltres.bbu.commands.team

import com.mojang.brigadier.arguments.ArgumentType
import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.BBUTeamColor
import me.imoltres.bbu.data.TeamArgumentType
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.argument
import me.imoltres.bbu.utils.command.argumentBoolean
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.command.literal


val TeamCommands = command(
    "team",
) {
    val teamArg = argument("team", TeamArgumentType() as ArgumentType<BBUTeamColor>)
    val eliminatePlayers = argumentBoolean("players")

    permission("bbu.command.team")

    buildSyntax(teamArg, literal("eliminate")) {
        permission("bbu.command.team.eliminate")

        executor { sender ->
            val team = BBU.getInstance().teamController.getTeam(teamArg<BBUTeamColor>())

            team.eliminate(false)
            sender.sendMessage(CC.translate("&aEliminated team " + team.getRawDisplayName() + "!"))

        }
    }

    buildSyntax(teamArg, literal("eliminate"), eliminatePlayers) {
        permission("bbu.command.team.eliminate")

        executor { sender ->
            val team = BBU.getInstance().teamController.getTeam(teamArg<BBUTeamColor>())
            val elimPlayers = eliminatePlayers<Boolean>()

            team.eliminate(elimPlayers)

            sender.sendMessage(CC.translate("&aEliminated team " + team.getRawDisplayName() + (if (elimPlayers) " and their players" else "") + "!"))
        }
    }
}