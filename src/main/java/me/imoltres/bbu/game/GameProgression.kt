package me.imoltres.bbu.game

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import java.util.stream.Collectors

class GameProgression {
    var gameState = GameState.LOBBY

    fun getTeams(hasBeacon: Boolean): Set<BBUTeam> {
        return getTeamsAlive()
            .stream()
            .filter { team: BBUTeam -> hasBeacon == team.hasBeacon() }
            .collect(Collectors.toSet())
    }

    fun getTeamsAlive(): Set<BBUTeam> {
        return BBU.getInstance().teamController.allTeams.filter { bbuTeam -> bbuTeam.hasBeacon() || bbuTeam.players.size > 0 }
            .toSet()
    }
}