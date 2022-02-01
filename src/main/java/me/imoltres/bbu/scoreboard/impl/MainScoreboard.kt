package me.imoltres.bbu.scoreboard.impl

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.DateUtils
import me.imoltres.bbu.utils.TickUtils
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.math.BigDecimal

class MainScoreboard(player: Player) : BBUScoreboardAdapter(1, player) {

    override fun getLines(player: BBUPlayer): ArrayList<String> {
        val lines = ArrayList<String>()
        val game = BBU.instance.game

        lines.add(CC.SB_DIV)
        lines.add("&bOnline: &f" + Bukkit.getOnlinePlayers().size)
        lines.add("&bTPS: " + TickUtils.getTPS())

        if (game.gameState.next() != null) {
            val tick = game.thread.tick
            val timeTill = DateUtils.readableTime(BigDecimal(game.gameState.next()!!.startsAfterTick - tick))

            when (game.gameState) {
                GameState.GRACE, GameState.PVP -> lines.add(
                    String.format(
                        "&b%s&f:%s",
                        game.gameState.next()!!.display,
                        timeTill
                    )
                )
                GameState.PVP_BORDER_SHRINK -> {
                    lines.add(
                        String.format(
                            "&b%s&f: %.1f",
                            game.gameState.display,
                            game.overworld.worldBorder.size
                        )
                    )
                }
                else -> {}
            }
        }

        if (game.gameState == GameState.PVP || game.gameState == GameState.PVP_BORDER_SHRINK) {
            lines.add(
                String.format(
                    "&bFortress&f: %s, %s",
                    game.fortressPosition.x,
                    game.fortressPosition.y
                )
            )
        }

        lines.add("")

        for (team in BBU.instance.teamController.allTeams) {
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

        lines.add(CC.SB_DIV)

        return lines
    }


}