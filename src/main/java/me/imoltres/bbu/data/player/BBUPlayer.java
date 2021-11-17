package me.imoltres.bbu.data.player;

import lombok.Data;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.scoreboard.BBUScoreboard;
import me.imoltres.bbu.scoreboard.ScoreboardType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Data
public class BBUPlayer {

    private transient Player player;

    private final UUID uniqueId;
    private final String name;

    private BBUScoreboard scoreboardUsed;

    private BBUTeam team = null;

    public Player getPlayer() {
        if (player == null) {
            if (Bukkit.getPlayer(uniqueId) == null) {
                System.out.printf("'%s' is offline, can't retrieve bukkit player.\n", name);
                return null;
            }

            player = Bukkit.getPlayer(uniqueId);
        }
        return player;
    }

    public boolean displayBoard(ScoreboardType type) {
        Player player = getPlayer();
        if (player == null) {
            return false;
        }

        return BBUScoreboard.display(type.scoreboard(), player);
    }

}
