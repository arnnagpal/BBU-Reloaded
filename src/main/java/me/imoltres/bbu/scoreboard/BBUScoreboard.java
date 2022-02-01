package me.imoltres.bbu.scoreboard;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BBUScoreboard {

    private BBUScoreboardThread thread;

    public BBUScoreboard() {
        thread = new BBUScoreboardThread();

        thread.start();
    }

    public void cleanup() {
        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }

        for (BBUPlayer player : BBU.getInstance().getPlayerController().getPlayers()) {
            cleanup(player);
        }
    }

    public void cleanup(BBUPlayer player) {
        Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());

        if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
            bukkitPlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        player.getScoreboard().remove();
        player.setScoreboard(null);
    }
}
