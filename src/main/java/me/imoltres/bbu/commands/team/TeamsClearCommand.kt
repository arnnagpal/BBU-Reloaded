package me.imoltres.bbu.commands.team

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.entity.Player


@CommandInfo(
    name = "bbu.teams.clear",
    permission = "bbu.command.teams.clear"
)
class TeamsClearCommand : SubCommand {

    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<Player>()

        for (team in BBU.getInstance().teamController.allTeams) {
            team.players.clear()
        }

        sender.sendMessage(CC.translate("&aDone clearing teams!"))
    }

}