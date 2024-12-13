package me.imoltres.bbu.listeners;

import com.destroystokyo.paper.event.player.PlayerStartSpectatingEntityEvent;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.game.events.player.BBUPlayerCompassOpenEvent;
import me.imoltres.bbu.utils.CC;
import me.imoltres.bbu.utils.config.MainConfig;
import me.imoltres.bbu.utils.general.PlayerUtils;
import me.imoltres.bbu.utils.item.ItemConstants;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InteractListener implements Listener {

    @EventHandler
    public void onStartSpectate(PlayerStartSpectatingEntityEvent e) {
        BBUPlayer player = BBU.getInstance().getPlayerController().getPlayer(e.getPlayer().getUniqueId());
        if (player == null)
            return;

        if (player.getEliminated()) {
            if (player.getSwitchingSpectator()) {
                player.setSwitchingSpectator(false);
            }
        }
    }

    @EventHandler
    public void onStopSpectate(PlayerStopSpectatingEntityEvent e) {
        BBUPlayer player = BBU.getInstance().getPlayerController().getPlayer(e.getPlayer().getUniqueId());
        if (player == null)
            return;
        if (player.getEliminated()) {
            if (!player.getSwitchingSpectator()) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getItem() == null)
            return;

        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        BBUTeam team = BBU.getInstance().getTeamController().getTeam(player);
        BBUPlayer bbuPlayer = BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId());
        if (bbuPlayer == null)
            return;

        if (bbuPlayer.getEliminated()) {
            BBUTeam spectatingTeam = bbuPlayer.getSpectatingTeam();
            if (spectatingTeam == null)
                return;

            if (player.getSpectatorTarget() == null)
                return;

            List<BBUPlayer> players = spectatingTeam.getPlayersAsList();
            BBUPlayer spectatingPlr = BBU.getInstance().getPlayerController().getPlayer(player.getSpectatorTarget().getUniqueId());
            int currIndex = players.indexOf(spectatingPlr);

            Player nextPlayer;

            if (e.getAction().isLeftClick()) {
                // move to next player
                nextPlayer = players.get((currIndex + 1) % players.size()).getPlayer();
            } else if (e.getAction().isRightClick()) {
                // move to previous player
                nextPlayer = players.get((currIndex - 1 + players.size()) % players.size()).getPlayer();
            } else {
                return;
            }

            bbuPlayer.setSwitchingSpectator(true);
            player.setSpectatorTarget(nextPlayer);
            return;
        }

        if (ItemConstants.isSimilar(ItemConstants.TRACKING_COMPASS, item)) {
            if (e.getPlayer().isSneaking()) {
                double distance = 500 * 500;

                Player nearest = null;

                for (Player p : Bukkit.getServer().getOnlinePlayers()) {
                    if (!p.equals(player) && p.getWorld().equals(player.getWorld()) && !p.getGameMode().equals(GameMode.SPECTATOR)) {
                        if (e.getPlayer().getGameMode() == GameMode.SPECTATOR) {
                            continue;
                        }

                        BBUTeam enemyTeam = BBU.getInstance().getTeamController().getTeam(p);

                        if (enemyTeam == null || enemyTeam == team) {
                            continue;
                        }

                        double distanceSquared = p.getLocation().distanceSquared(player.getLocation());
                        if (distanceSquared >= distance) {
                            continue;
                        }

                        distance = distanceSquared;
                        nearest = p;
                    }
                }
                if (nearest == null) {
                    player.sendMessage(CC.RED + "No players in a 1250 radius to track!");
                } else {
                    player.performCommand("trackposition " + PlayerUtils.getLocation(nearest.getLocation()) + " false");
                    player.sendMessage(CC.GREEN + "Tracking one position of '" + nearest.getName() + "'. This won't update until you attempt to track nearby players again.");
                }
            } else {
                BBUPlayerCompassOpenEvent event = new BBUPlayerCompassOpenEvent(BBU.getInstance().getPlayerController().getPlayer(player.getUniqueId()));
                Bukkit.getPluginManager().callEvent(event);

                if (event.isCancelled())
                    return;

                event.getCompassMenu().openMenu(player);
            }
        }

    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player damaged) {
            Player damager = null;
            BBUPlayer bbuDamager = null;
            if (e.getDamager() instanceof Projectile proj) {
                if (proj.getShooter() instanceof Player) {
                    damager = (Player) proj.getShooter();
                }
            } else if (e.getDamager() instanceof Player) {
                damager = (Player) e.getDamager();
            }

            if (damager != null)
                bbuDamager = BBU.getInstance().getPlayerController().getPlayer(damager.getUniqueId());

            if (damager != null) {
                if (!BBU.getInstance().getGame().getGameState().isPvp()) {
                    e.setCancelled(true);
                } else {
                    if (!MainConfig.FRIENDLY_FIRE) {
                        BBUTeam damagedTeam = BBU.getInstance().getTeamController().getTeam(damaged);
                        BBUTeam damagerTeam = bbuDamager.getTeam();
                        if (damagedTeam == damagerTeam) {
                            e.setCancelled(true);
                        }
                    }
                }
            }

            if (e.isCancelled() && e.getDamager() instanceof Arrow && bbuDamager != null)
                bbuDamager.giveItemSafely(new ItemStack(Material.ARROW), true);
        }
    }

}
