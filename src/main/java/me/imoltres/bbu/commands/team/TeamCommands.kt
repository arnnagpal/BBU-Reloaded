package me.imoltres.bbu.commands.team

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.BBUTeamColor
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.entity.Player


@CommandInfo(
    name = "bbu.team",
    permission = "bbu.command.team"
)
class TeamCommands : SubCommand {

    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<Player>()
        val args = cmd.arguments

        if (args.size < 2) {
            sender.sendMessage(CC.translate("&cSpecify a team."))
            return
        }

        val colour: BBUTeamColor
        try {
            colour = BBUTeamColor.valueOf(args[0].uppercase())
        } catch (e: Exception) {
            sender.sendMessage(CC.translate("&cSpecify a *valid* team."))
            return
        }
        val team = BBU.getInstance().teamController.getTeam(colour)
        val type = args[1].lowercase()

        when (type) {
            "eliminate" -> {
                var players = false
                if (args.size == 3) {
                    try {
                        players = args[2].toBoolean()
                    } catch (e: Exception) {
                        sender.sendMessage(CC.translate("&cUse a valid boolean type (true / false)"))
                    }
                }

                team.eliminate(players)
            }
        }

    }

}