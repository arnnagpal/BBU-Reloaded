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

class MainScoreboard(player: Player) : BBUScoreboardAdapter(player) {

    override fun getLines(player: BBUPlayer): ArrayList<String> {
        val lines = ArrayList<String>()
        val game = BBU.getInstance().game

        lines.add(CC.SB_DIV)
        lines.add("&bOnline: &f" + Bukkit.getOnlinePlayers().size)
        lines.add("&bTPS: " + TickUtils.getTPS())

        val nextState = game.gameState.next()
        if (nextState != null && game.gameState in listOf(GameState.GRACE, GameState.PVP)) {
            val timeTill = DateUtils.readableTime(BigDecimal(nextState.startTime - game.thread.tick / 20))
            lines.add("&b${nextState.display}&f:$timeTill")
        }

        if (game.gameState.isPvp()) {
            game.fortressPosition?.let { lines.add("&bFortress&f: ${it.x}, ${it.y}") }
            game.tChamberPosition?.let { lines.add("&bTrial Chamber&f: ${it.x}, ${it.y}") }
        }

        lines.add("")

        if (game.gameState == GameState.PVP_BORDER_SHRINK) {
            lines.add("&b${game.gameState.display}&f: ${"%.1f".format(game.overworld.worldBorder.size)}")
            if (game.nextShrinkPhase != null)
                lines.add("&bNext shrink&f:${DateUtils.readableTime(BigDecimal(game.thread.timeToNextShrink / 20))}")
            if (game.thread.deathmatchTimer > 0)
                lines.add("&bDeathmatch&f:${DateUtils.readableTime(BigDecimal(game.thread.deathmatchTimer / 20))}")
            lines.add("")
        }

        for (team in BBU.getInstance().teamController.allTeams) {
            val name = CC.capitalize(team.colour.name.lowercase())
            val colorPrefix = "&${team.colour.chatColor.code}${team.colour.name.first()} &f$name&f: "
            val youSuffix = if (team.players.contains(player)) " &7YOU" else ""
            val status = when {
                team.hasBeacon() -> "&a\u2713"
                team.players.isNotEmpty() -> "&a${team.players.size}"
                else -> "&c\u2715"
            }

            lines.add("$colorPrefix$status$youSuffix")
        }

        lines.add(CC.SB_DIV)

        return lines
    }


}