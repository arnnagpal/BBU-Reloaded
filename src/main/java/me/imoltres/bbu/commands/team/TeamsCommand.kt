package me.imoltres.bbu.commands.team

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.entity.Player


@CommandInfo(
    name = "bbu.teams",
    permission = "bbu.command.teams"
)
class TeamsCommand : SubCommand {

    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<Player>()
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

}