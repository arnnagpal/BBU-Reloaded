package me.imoltres.bbu.game;

import lombok.Getter;
import lombok.Setter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.controllers.TeamController;
import me.imoltres.bbu.data.BBUTeamColour;
import me.imoltres.bbu.data.player.BBUPlayerStatistics;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GameProgression {

    public final Set<BBUPlayerStatistics> playerStatistics = new HashSet<>();
    public final Set<BBUTeamColour> teamsAlive = new HashSet<>();
    @Getter
    @Setter
    @NotNull
    private GameState gameState = GameState.PRE_GAME;

    public Set<BBUTeamColour> getTeams(boolean hasBeacon) {
        TeamController controller = BBU.getInstance().getTeamController();
        return teamsAlive
                .stream()
                .filter(teamColour -> hasBeacon == controller.getTeam(teamColour).hasBeacon())
                .collect(Collectors.toSet());
    }

}