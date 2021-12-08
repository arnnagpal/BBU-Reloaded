package me.imoltres.bbu.data.player;

import lombok.Data;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.scoreboard.BBUScoreboard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

@Data
public class BBUPlayer {

    private transient Player player;

    private final UUID uniqueId;
    private final String name;

    private BBUScoreboard scoreboard;

    private BBUTeam team = null;

    private boolean build = false;

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

}
