package me.imoltres.bbu.data.player;

import lombok.Data;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.scoreboard.BBUScoreboard;

import java.util.UUID;

@Data
public class BBUPlayer {

    private final UUID uniqueId;
    private final String name;

    private BBUScoreboard scoreboardUsed;

    private BBUTeam team = null;

}
