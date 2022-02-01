package me.imoltres.bbu.game

import me.imoltres.bbu.data.player.BBUPlayerStatistics
import me.imoltres.bbu.data.team.BBUTeam
import java.util.stream.Collectors

class GameProgression {
    private val teamsAlive: Set<BBUTeam> = HashSet()

    val playerStatistics: Set<BBUPlayerStatistics> = HashSet()

    var gameState = GameState.PRE_GAME

    fun getTeams(hasBeacon: Boolean): Set<BBUTeam> {
        return teamsAlive
            .stream()
            .filter { team: BBUTeam -> hasBeacon == team.hasBeacon() }
            .collect(Collectors.toSet())
    }
}