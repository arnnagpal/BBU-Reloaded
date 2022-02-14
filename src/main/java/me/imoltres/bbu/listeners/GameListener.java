package me.imoltres.bbu.listeners;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.game.GameState;
import me.imoltres.bbu.game.events.player.BBUPlayerDeathEvent;
import me.imoltres.bbu.game.events.team.BBUBreakBeaconEvent;
import me.imoltres.bbu.game.events.team.BBUPlaceBeaconEvent;
import me.imoltres.bbu.game.events.team.BBUTeamModificationEvent;
import me.imoltres.bbu.utils.BlockUtils;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.ItemConstants;
import me.imoltres.bbu.utils.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class GameListener implements Listener {

    @EventHandler
    public void onPlayerDeath(BBUPlayerDeathEvent e) {
        BBUPlayer player = e.getPlayer();
        BBUPlayer killer = e.getKiller();
        BBUTeam team = player.getTeam();

        if (team == null)
            return;

        if (e.isFinalDeath()) {
            if (killer != null) {
                Random random = ThreadLocalRandom.current();
                killer.giveItemSafely(new ItemStack(Material.DIAMOND, random.nextInt(5) + 1), true);
            }

            Bukkit.getScheduler().scheduleSyncDelayedTask(BBU.getInstance(), () -> {
                Player bukkitPlayer = player.getPlayer();
                if (bukkitPlayer == null)
                    return;

                if (bukkitPlayer.isDead() && bukkitPlayer.isOnline()) {
                    player.eliminate();
                }

            }, 2L);

        }

    }

    @EventHandler
    public void onBeaconPlace(BBUPlaceBeaconEvent e) {
        Player player = e.getPlacer().getPlayer();
        if (player == null) {
            e.setCancelled(true);
            return;
        }

        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            e.setCancelled(true);
        } else {
            if (BlockUtils.getFacesTouching(BlockUtils.Companion.getFaces(), e.getPosition().getBlock()).size() > 1) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBeaconBreak(BBUBreakBeaconEvent e) {
        BBUPlayer player = e.getBreaker();
        BBUTeam team = BBU.getInstance().getTeamController().getTeam(e.getBeacon());

        if (e.isTeamBreak() && BBU.getInstance().getGame().getGameState().isPvp()) {
            e.setCancelled(true);
            return;
        }

        if (BBU.getInstance().getGame().getGameState() == GameState.GRACE) {
            //if team break then give back else cancel
            if (e.isTeamBreak()) {
                player.giveItemSafely(ItemConstants.TEAM_BEACON);
            } else {
                e.setCancelled(true);
            }
            return;
        }

        //destroyed beacon, reward diamonds, and broadcast
        Bukkit.broadcast(CC.translate(Messages.BEACON_DESTROYED.toString()
                        .replace("{breaker}", player.getRawDisplayName())
                        .replace("{team}", team.getRawDisplayName())
                )
        );

        Random random = ThreadLocalRandom.current();
        player.giveItemSafely(new ItemStack(Material.DIAMOND, random.nextInt(5) + 1), true);
    }

    @EventHandler
    public void onTeamModification(BBUTeamModificationEvent e) {
        if (BBU.getInstance().getGame().getAliveTeams().contains(e.getTeam()))
            BBU.getInstance().getGame().checkTeam(e.getTeam());
    }

}
