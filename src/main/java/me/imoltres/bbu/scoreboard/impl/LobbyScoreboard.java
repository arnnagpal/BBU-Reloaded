package me.imoltres.bbu.scoreboard.impl;

import lombok.NonNull;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.scoreboard.BBUScoreboardImpl;
import me.imoltres.bbu.scoreboard.ScoreboardType;

import java.util.List;

public class LobbyScoreboard extends BBUScoreboardImpl {

    public LobbyScoreboard(BBUPlayer bbuPlayer) {
        super(bbuPlayer);
    }

    @Override
    @NonNull
    public ScoreboardType getType() {
        return ScoreboardType.LOBBY;
    }

    @Override
    public List<String> getOverrideLines() {
        return null;
    }

}
