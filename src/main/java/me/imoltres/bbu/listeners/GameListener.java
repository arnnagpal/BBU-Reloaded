package me.imoltres.bbu.listeners;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.game.GameState;
import me.imoltres.bbu.game.events.player.BBUPlayerCompassOpenEvent;
import me.imoltres.bbu.game.events.player.BBUPlayerDeathEvent;
import me.imoltres.bbu.game.events.team.BBUBreakBeaconEvent;
import me.imoltres.bbu.game.events.team.BBUPlaceBeaconEvent;
import me.imoltres.bbu.game.events.team.BBUTeamModificationEvent;
import me.imoltres.bbu.utils.general.BlockUtils;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.item.ItemConstants;
import me.imoltres.bbu.utils.config.Messages;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
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
        } else {
            Player bukkitPlayer = player.getPlayer();
            if (bukkitPlayer.getBedSpawnLocation() != null) {
                bukkitPlayer.teleport(bukkitPlayer.getBedSpawnLocation());
            }
            if (team.hasBeacon()) {
                if (bukkitPlayer == null)
                    return;
                bukkitPlayer.teleport(team.getBeacon().toWorldPosition("world").toBukkitLocation().add(0.5, 0.5, 0.5));
            } else {
                if (bukkitPlayer == null)
                    return;
                bukkitPlayer.teleport(team.getCage().getSpawnPosition().toBukkitLocation());
            }
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
            //This needs some work TODO fix this
            Set<Block> facesTouching = BlockUtils.getFacesTouching(BlockUtils.Companion.getFaces(), e.getPosition().getBlock());
            System.out.println(Arrays.toString(facesTouching.stream().map(Block::getType).toArray()));
            if (facesTouching.size() > 1) {
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
        //This needs to be fixed
        player.giveItemSafely(new ItemStack(Material.DIAMOND, random.nextInt(5) + 1), true);
    }

    @EventHandler
    public void onTeamModification(BBUTeamModificationEvent e) {
        if (BBU.getInstance().getGame().getAliveTeams().contains(e.getTeam()))
            BBU.getInstance().getGame().checkTeam(e.getTeam());
    }

    @EventHandler
    public void onTakenDamage(EntityDamageEvent e) {
        if (BBU.getInstance().getGame().getGameState() == GameState.LOBBY || BBU.getInstance().getGame().getGameState() == GameState.DEATH_MATCH) {
            e.setCancelled(true);
        }
    }

}
