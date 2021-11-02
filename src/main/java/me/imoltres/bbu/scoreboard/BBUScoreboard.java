package me.imoltres.bbu.scoreboard;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import lombok.NonNull;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.utils.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public interface BBUScoreboard extends AssembleAdapter {
    BasicConfigurationFile messagesConfig = BBU.getInstance().getMessagesConfig();

    default String getTitle(Player player) {
        return messagesConfig.getString("scoreboard-title");
    }

    default List<String> getLines(Player player) {
        List<String> lines = new ArrayList<>();
        BBUPlayer bbuPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());

        //Check for lines to override the default scoreboard
        List<String> overrideLines = bbuPlayer.getScoreboardUsed().getOverrideLines();
        if (overrideLines != null) {
            //Override the default scoreboard with whatever implementation of scoreboard is being used
            return overrideLines;
        }

        for (String str : messagesConfig.getStringList("scoreboards.default")) {
//            lines.add(CC.translate(str));
        }

        return lines;
    }

    @NonNull
    default ScoreboardType getType() {
        return ScoreboardType.DEFAULT;
    }

    List<String> getOverrideLines();

}
