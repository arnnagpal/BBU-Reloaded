package me.imoltres.bbu.scoreboard.impl

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.scoreboard.BBUScoreboard
import me.imoltres.bbu.scoreboard.ScoreboardType
import me.imoltres.bbu.utils.CC
import org.bukkit.Bukkit
import org.bukkit.entity.Player


class LobbyScoreboard(player: Player) : BBUScoreboard(1, player, ScoreboardType.LOBBY) {

    override fun getLines(player: BBUPlayer): ArrayList<String> {
        val lines = ArrayList<String>()
        lines.add("&bOnline: &f" + Bukkit.getOnlinePlayers().size)
        lines.add("&bTPS: &r" + CC.formatTPS(Bukkit.getTPS()[0]))

        lines.add("")
        for (team in BBU.getInstance().teamController.allTeams) {
            val name = CC.capitalize(team.colour.name.lowercase())
            if (team.hasBeacon()) {
                if (team.players.size > 0) {
                    lines.add(
                        "&" + team.colour.chatColor.char + team.colour.name.split("")[0] + " &f" + name + "&f: &a" + team.players.size.toString() +
                                if (team.players.contains(player)) " &7YOU" else ""
                    )
                } else {
                    lines.add(
                        "&" + team.colour.chatColor.char + team.colour.name.split("")[0] + " &f" + name + "&f: " + "&c\u2715" +
                                if (team.players.contains(player)) " &7YOU" else ""
                    )
                }
            } else {
                lines.add(
                    "&" + team.colour.chatColor.char + team.colour.name.split("")[0] + " &f" + name.toString() + "&f: " + "&a\u2713" +
                            if (team.players.contains(player)) " &7YOU" else ""
                )
            }
        }

        return lines
    }


}