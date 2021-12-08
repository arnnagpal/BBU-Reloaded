package me.imoltres.bbu.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.game.shrink.ShrinkStatus;
import me.imoltres.bbu.utils.DateUtils;
import me.imoltres.bbu.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

@RequiredArgsConstructor
public class GameThread extends Thread {

    protected transient final Game game;

    @Getter
    private int tick = 0;

    @Override
    public void run() {
        try {
            while (game.getGameState() != GameState.POST_GAME) {
                if (game.isPaused())
                    continue;

                tick++;
                swapGameStateIfAvailable();

                switch (game.getGameState()) {
                    case PVP -> PlayerUtils.broadcastTitle("&cPvP is now enabled.",
                            "&7" + DateUtils.readableTime(new BigDecimal(GameState.PVP_BORDER_SHRINK.getStartsAfterTick())) + " till border shrink.");

                    case PVP_BORDER_SHRINK -> {
                        int border = game.getBorder();
                        shrinkBorder(250, getBorderShrinkTime(border));
                    }
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    BBUPlayer bbuPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());
                    bbuPlayer.getScoreboard().update();
                }

                //sleep to tick to next second
                Thread.sleep(1000);
            }
        } catch (InterruptedException ignored) {
        }
    }

    private int getBorderShrinkTime(int border) {
        int time = 0;

        for (int i = 1; i < 7; i++) {
            time += ((border / 200) - (2.5 * ShrinkStatus.NUM(i))) * 60;
        }

        return time;
    }

    public void shrinkBorder(double amount, int seconds) {
        PlayerUtils.broadcastTitle(
                "&cShrinking Border to " + amount,
                "&7in " + DateUtils.readableTime(new BigDecimal(seconds))
        );

        for (World world : Bukkit.getWorlds()) {
            world.getWorldBorder().setSize(amount, seconds);
        }
    }

    public void swapGameStateIfAvailable() {
        GameState gameState = GameState.getGameStateFromTick(tick);
        if (game.getGameState() != gameState) {
            game.setGameState(gameState);
        }
    }

}
