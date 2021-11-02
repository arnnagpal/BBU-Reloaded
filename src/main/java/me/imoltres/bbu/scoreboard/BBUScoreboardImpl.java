package me.imoltres.bbu.scoreboard;

import lombok.Getter;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class BBUScoreboardImpl implements BBUScoreboard {

    @Getter
    private final BBUPlayer player;

    public BBUScoreboardImpl(BBUPlayer bbuPlayer) {
        this.player = bbuPlayer;
    }

    @Override
    public List<String> getLines(Player player) {
        BBUPlayer bbuPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());

        //Check for lines to override the default scoreboard
        List<String> overrideLines = ((BBUScoreboardImpl) bbuPlayer.getScoreboardUsed()).getOverrideLines();
        if (overrideLines != null) {
            //Override the default scoreboard with whatever implementation of scoreboard is being used
            return overrideLines;
        }

        return messagesConfig.getStringList("scoreboards.default");
    }

    public abstract List<String> getOverrideLines();

}
