package me.imoltres.bbu.scoreboard;

import com.qrakn.phoenix.lang.file.type.BasicConfigurationFile;
import lombok.NonNull;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.utils.assemble.AssembleAdapter;
import org.bukkit.entity.Player;

import java.util.List;

public interface BBUScoreboard extends AssembleAdapter {
    BasicConfigurationFile messagesConfig = BBU.getInstance().getMessagesConfig();

    default String getTitle(Player player) {
        return messagesConfig.getString("scoreboard-title");
    }

    List<String> getLines(Player player);

    @NonNull
    default ScoreboardType getType() {
        return ScoreboardType.DEFAULT;
    }

}
