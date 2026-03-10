package me.imoltres.bbu.commands.player

import com.mojang.brigadier.arguments.ArgumentType
import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.BBUTeamColor
import me.imoltres.bbu.data.TeamArgumentType
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.command.argument
import me.imoltres.bbu.utils.command.argumentPlayer
import me.imoltres.bbu.utils.command.command
import me.imoltres.bbu.utils.command.literal


val PlayerCommands = command(
    "player",
    "p"
) {
    val playerArg = argumentPlayer("player")
    permission("bbu.command.player")

    buildSyntax(playerArg, literal("eliminate")) {
        permission("bbu.command.player.eliminate")
        executor { sender, ctx ->
            val player = playerArg().resolve(ctx.source).first()

            val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)

            player.health = 0.0
            bbuPlayer.eliminate(player.location)

            sender.sendMessage(CC.translate("&aEliminated player " + bbuPlayer.getRawDisplayName() + "!"))
        }
    }

    val teamArg = argument("team", TeamArgumentType() as ArgumentType<BBUTeamColor>)
    buildSyntax(playerArg, literal("jointeam"), teamArg) {
        permission("bbu.command.player.jointeam")
        executor { sender, ctx ->
            val player = playerArg().resolve(ctx.source).first()
            val colour = teamArg()

            val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
            val team = BBU.getInstance().teamController.getTeam(colour)

            team.addPlayer(bbuPlayer)
            sender.sendMessage(CC.translate("&aAdded '" + bbuPlayer.getRawDisplayName() + "&a' to team '" + team.getRawDisplayName() + "&a'"))
        }
    }

    buildSyntax(playerArg, literal("leaveteam")) {
        permission("bbu.command.player.leaveteam")
        executor { sender, ctx ->
            val player = playerArg().resolve(ctx.source).first()

            val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
            val team = bbuPlayer.team

            team?.removePlayer(bbuPlayer)
            sender.sendMessage(CC.translate("&aRemoved '" + bbuPlayer.getRawDisplayName() + "&a' from team '" + team?.getRawDisplayName() + "&a'"))
        }
    }
}