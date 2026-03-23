package me.imoltres.bbu.game

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.utils.config.MainConfig
import java.util.stream.Collectors

class GameProgression {
    var gameState = GameState.LOBBY
    var currentShrinkPhase: ShrinkPhase? = null

    private var shrinkIndex = -1

    fun getOrderedShrinkPhases(): List<ShrinkPhase> {
        return MainConfig.borderPhases.sortedBy { it.time }
    }

    fun getTeams(hasBeacon: Boolean): Set<BBUTeam> {
        return getTeamsAlive()
            .stream()
            .filter { team: BBUTeam -> hasBeacon == team.hasBeacon() }
            .collect(Collectors.toSet())
    }

    fun getTeamsAlive(): Set<BBUTeam> {
        return BBU.getInstance().teamController.allTeams.filter { bbuTeam -> bbuTeam.hasBeacon() || bbuTeam.players.isNotEmpty() }
            .toSet()
    }

    fun getNextPhase(): ShrinkPhase? {
        val orderedShrinkPhases = getOrderedShrinkPhases()
        if (shrinkIndex + 1 >= orderedShrinkPhases.size) return null

        return orderedShrinkPhases[shrinkIndex + 1]
    }

    fun getPreviousPhase(): ShrinkPhase? {
        val orderedShrinkPhases = getOrderedShrinkPhases()
        if (shrinkIndex - 1 >= orderedShrinkPhases.size) return null // last index, no next phase
        if (shrinkIndex - 1 < 0) return null                         // first index, no previous phase

        return orderedShrinkPhases[shrinkIndex - 1]
    }

}