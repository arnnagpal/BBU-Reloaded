package me.imoltres.bbu.scoreboard;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Updates every player's scoreboard every x millis.
 */
public class BBUScoreboardThread extends Thread {

    public void run() {
        while (true) {
            try {
                sleep(1000);
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                //UPDATE
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BBUPlayer bbuPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());
                    if (bbuPlayer.getScoreboard() != null)
                        bbuPlayer.getScoreboard().update();
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}
