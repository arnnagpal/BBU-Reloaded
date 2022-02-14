package me.imoltres.bbu.scoreboard;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Controller for all the {@link BBUScoreboardAdapter} instances
 */
public class BBUScoreboard {

    private BBUScoreboardThread thread;

    /**
     * Make a new instance of the 'controller'
     */
    public BBUScoreboard() {
        thread = new BBUScoreboardThread();

        thread.start();
    }

    /**
     * Cleanup all the scoreboards still being updated
     */
    public void cleanup() {
        if (this.thread != null) {
            this.thread.interrupt();
            this.thread = null;
        }

        for (BBUPlayer player : BBU.getInstance().getPlayerController().getPlayers()) {
            cleanup(player);
        }
    }

    /**
     * Cleanup the scoreboard of an individual player
     *
     * @param player player
     */
    public void cleanup(BBUPlayer player) {
        Player bukkitPlayer = Bukkit.getPlayer(player.getUniqueId());

        if (bukkitPlayer != null && bukkitPlayer.isOnline()) {
            bukkitPlayer.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }

        if (player.getScoreboard() == null)
            return;

        player.getScoreboard().remove();
        player.setScoreboard(null);
    }
}
