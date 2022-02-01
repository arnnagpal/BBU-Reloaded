package me.imoltres.bbu.scoreboard;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BBUScoreboardThread extends Thread {

    public void run() {
        while (true) {
            try {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                //UPDATE
                for (Player player : Bukkit.getOnlinePlayers()) {
                    BBUPlayer bbuPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());
                    if (bbuPlayer.getScoreboard() != null)
                        bbuPlayer.getScoreboard().update();
                }

                sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
    }

}
