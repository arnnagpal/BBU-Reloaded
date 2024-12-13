package me.imoltres.bbu.game.threads;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.game.Game;
import me.imoltres.bbu.utils.CC;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.time.Duration;

/**
 * Run before the {@link GameThread} thread, basically an async countdown
 */
@RequiredArgsConstructor
public class GameStartThread extends Thread {

    private final Game game;
    private int time = 3;

    @Override
    public void run() {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                return;
            }

            if (time > 0) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showTitle(Title.title(CC.translate("&c&l" + time), CC.translate(""), Title.Times.of(Duration.ofMillis(500), Duration.ofSeconds(1), Duration.ofMillis(500))));
                    player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 1, 1);
                }
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Bukkit.getScheduler().runTask(BBU.getInstance(), () -> {
                        player.setGameMode(GameMode.SURVIVAL);
                    });

                    player.showTitle(Title.title(CC.translate("&a&lGO!"), CC.translate("&7Good luck, have fun!"), Title.Times.of(Duration.ofMillis(500), Duration.ofSeconds(2), Duration.ofMillis(500))));
                    player.playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.25f, 1);
                }

                Bukkit.getScheduler().runTask(BBU.getInstance(), () -> {
                    for (World world : BBU.getInstance().getGame().getWorlds()) {
                        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
                    }
                });

                BBU.getInstance().getCageController().deleteCages(game.overworld);

                game.getThread().runTaskTimer(BBU.getInstance(), 0L, 1L);
                return;
            }

            time--;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
