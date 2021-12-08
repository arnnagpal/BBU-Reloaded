package me.imoltres.bbu.scoreboard.impl

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.scoreboard.BBUScoreboard
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.DateUtils
import org.bukkit.entity.Player
import java.math.BigDecimal

class ScoreboardAdapter(player: Player) : BBUScoreboard(1, player) {

    override fun getLines(player: BBUPlayer): ArrayList<String> {
        val lines = ArrayList<String>()
        val game = BBU.getInstance().game

        if (game.gameState.next() != null) {
            val tick = game.gameState.next().tick
            val timeTill = DateUtils.readableTime(BigDecimal(tick - game.gameState.next().startsAfterTick))

            when (game.gameState) {
                GameState.GRACE, GameState.PVP -> lines.add(
                    String.format(
                        "&b%s&f:%s",
                        game.gameState.next().display,
                        timeTill
                    )
                )
                GameState.PVP_BORDER_SHRINK -> lines.add(
                    String.format(
                        "&b%s&f: %s",
                        game.gameState.next().display,
                        game.OVERWORLD().worldBorder.size
                    )
                )
                else -> {}
            }
        }

        if (game.fortressPosition != null) {
            lines.add(
                String.format(
                    "&bFortress&f: %s, %s",
                    game.fortressPosition.x,
                    game.fortressPosition.y
                )
            )
        }

        for (team in BBU.getInstance().teamController.allTeams) {
            val name = CC.capitalize(team.colour.name.lowercase())
            if (team.hasBeacon()) {
                if (team.players.size > 0) {
                    lines.add(
                        String.format(
                            "&%s &f%s&f: &a%d%s",
                            team.colour.chatColor.char + team.colour.name.first().toString(),
                            name,
                            team.players.size,
                            if (team.players.contains(player)) " &7YOU" else ""
                        )
                    )
                } else {
                    lines.add(
                        String.format(
                            "&%s &f%s&f: &c\u2715%s",
                            team.colour.chatColor.char + team.colour.name.first().toString(),
                            name,
                            if (team.players.contains(player)) " &7YOU" else ""
                        )
                    )
                }
            } else {
                lines.add(
                    String.format(
                        "&%s &f%s&f: &a\u2713%s",
                        team.colour.chatColor.char + team.colour.name.first().toString(),
                        name,
                        if (team.players.contains(player)) " &7YOU" else ""
                    )
                )
            }
        }

        return lines
    }


}