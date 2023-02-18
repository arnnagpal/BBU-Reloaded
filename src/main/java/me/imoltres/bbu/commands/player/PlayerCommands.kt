package me.imoltres.bbu.commands.player

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.BBUTeamColour
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.CommandArgs
import me.imoltres.bbu.utils.command.CommandInfo
import me.imoltres.bbu.utils.command.SubCommand
import org.bukkit.entity.Player


@CommandInfo(
    name = "bbu.player",
    permission = "bbu.command.player"
)
class PlayerCommands : SubCommand {

    override fun execute(cmd: CommandArgs) {
        val sender = cmd.getSender<Player>()
        val args = cmd.arguments

        if (args.size < 2) {
            sender.sendMessage(CC.translate("&cSpecify a player."))
            return
        }

        val player: BBUPlayer
        try {
            player = BBU.getInstance().playerController.getPlayer(args[0])
        } catch (e: Exception) {
            sender.sendMessage(CC.translate("&cSpecify a *valid* player."))
            return
        }
        val type = args[1].lowercase()

        when (type) {
            "eliminate" -> {
                // kill player
                player.player?.let { player.player?.health = 0.0 }
                player.player?.let { player.eliminate(it.location) }
            }

            "jointeam" -> {
                if (args.size < 3) {
                    sender.sendMessage(CC.translate("&cSpecify a team."))
                    return
                }

                val colour: BBUTeamColour
                try {
                    colour = BBUTeamColour.valueOf(args[2].uppercase())
                } catch (e: Exception) {
                    sender.sendMessage(CC.translate("&cSpecify a *valid* team."))
                    return
                }
                val team = BBU.getInstance().teamController.getTeam(colour)
                team.addPlayer(player)
                sender.sendMessage(CC.translate("&aAdded '" + player.getRawDisplayName() + "&a' to team '" + team.getRawDisplayName() + "&a'"))
            }

            "leaveteam" -> {
                val team = player?.team
                team?.removePlayer(player)
                sender.sendMessage(CC.translate("&aRemoved '" + player.getRawDisplayName() + "&a' from team '" + team?.getRawDisplayName() + "&a'"))
            }
        }

    }

}