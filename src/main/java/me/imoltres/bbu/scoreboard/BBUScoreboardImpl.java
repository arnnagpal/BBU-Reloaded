package me.imoltres.bbu.scoreboard;

import lombok.Getter;
import me.imoltres.bbu.data.player.BBUPlayer;

import java.util.List;

public abstract class BBUScoreboardImpl implements BBUScoreboard {

    @Getter
    private final BBUPlayer player;

    public BBUScoreboardImpl(BBUPlayer bbuPlayer) {
        this.player = bbuPlayer;
    }

    public abstract List<String> getOverrideLines();

}
