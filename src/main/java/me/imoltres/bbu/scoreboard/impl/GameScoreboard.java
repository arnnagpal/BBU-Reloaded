package me.imoltres.bbu.scoreboard.impl;

import lombok.NonNull;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.scoreboard.BBUScoreboardImpl;
import me.imoltres.bbu.scoreboard.ScoreboardType;

import java.util.List;

public class GameScoreboard extends BBUScoreboardImpl {

    public GameScoreboard(BBUPlayer bbuPlayer) {
        super(bbuPlayer);
    }

    @Override
    @NonNull
    public ScoreboardType getType() {
        return ScoreboardType.GAME;
    }

    @Override
    public List<String> getOverrideLines() {
        return null;
    }

}
