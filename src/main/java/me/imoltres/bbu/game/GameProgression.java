package me.imoltres.bbu.game;

import lombok.Getter;
import lombok.Setter;
import me.imoltres.bbu.data.player.BBUPlayerStatistics;
import me.imoltres.bbu.data.team.BBUTeam;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GameProgression {

    public final Set<BBUPlayerStatistics> playerStatistics = new HashSet<>();
    public final Set<BBUTeam> teamsAlive = new HashSet<>();
    @Getter
    @Setter
    @NotNull
    private GameState gameState = GameState.PRE_GAME;

    public Set<BBUTeam> getTeams(boolean hasBeacon) {
        return teamsAlive
                .stream()
                .filter(team -> hasBeacon == team.hasBeacon())
                .collect(Collectors.toSet());
    }

}