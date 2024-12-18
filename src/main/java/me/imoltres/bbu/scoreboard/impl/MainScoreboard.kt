package me.imoltres.bbu.scoreboard.impl

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.general.DateUtils
import me.imoltres.bbu.utils.general.TickUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigDecimal

class MainScoreboard(player: Player) : BBUScoreboardAdapter(1, player) {

    override fun getLines(player: BBUPlayer): ArrayList<String> {
        val lines = ArrayList<String>()
        val game = BBU.getInstance().game

        lines.add(CC.SB_DIV)
        lines.add("&bOnline: &f" + Bukkit.getOnlinePlayers().size)
        lines.add("&bTPS: " + TickUtils.getTPS())

        if (game.gameState.next() != null) {
            val tick = game.thread.tick / 20
            val timeTill = DateUtils.readableTime(BigDecimal(game.gameState.next()!!.startTime - tick))

            when (game.gameState) {
                GameState.GRACE, GameState.PVP -> lines.add(
                    String.format(
                        "&b%s&f:%s",
                        game.gameState.next()!!.display,
                        timeTill
                    )
                )

                else -> {}
            }
        }

        if (game.gameState.isPvp()) {
            lines.add(
                String.format(
                    "&bFortress&f: %s, %s",
                    game.fortressPosition.x,
                    game.fortressPosition.y
                )
            )
        }

        lines.add("")

        if (game.gameState == GameState.PVP_BORDER_SHRINK) {
            val timeTillShrink = DateUtils.readableTime(BigDecimal((game.thread.shrinkingTime / 20)))

            lines.add(
                String.format(
                    "&b%s&f: %.1f",
                    game.gameState.display,
                    game.overworld.worldBorder.size
                )
            )

            lines.add(
                String.format(
                    "&b%s&f:%s",
                    "Next shrink",
                    timeTillShrink
                )
            )

            lines.add("")
        }


        for (team in BBU.getInstance().teamController.allTeams) {
            val name = CC.capitalize(team.colour.name.lowercase())
            if (!team.hasBeacon()) {
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

        lines.add(CC.SB_DIV)

        return lines
    }


}