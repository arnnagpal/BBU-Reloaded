package me.imoltres.bbu.commands.team

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.command

val TeamsCommand = command(
    "teams",
    "listteams"
) {
    permission("bbu.command.teams")
    defaultExecutor { sender ->
        sender.sendMessage(CC.translate("&3Teams:"))

        for (team in BBU.getInstance().teamController.allTeams) {
            val players = StringBuilder()
            for (player in team.players) {
                players.append(player.name).append(", ")
            }

            if (players.isNotEmpty()) {
                players.setLength(players.length - 2)
            }

            sender.sendMessage(
                CC.translate(
                    " &f* " + team.getRawDisplayName() + " &8- &f" + players.toString()
                )
            )
        }
    }

    subcommand("clear") {
        permission("bbu.command.teams.clear")
        defaultExecutor { sender ->
            BBU.getInstance().teamController.clearTeams()

            sender.sendMessage(CC.translate("&aDone clearing teams!"))
        }
    }
}