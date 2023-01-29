package me.imoltres.bbu.listeners;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import me.imoltres.bbu.data.team.BBUTeam;
import me.imoltres.bbu.utils.general.BlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;

import java.util.Set;

public class BlockListener implements Listener {

    //prevent pistons from pushing blocks near beacons
    @EventHandler
    public void onPistonPush(BlockPistonExtendEvent e) {
        // prevent pistons from pushing blocks near beacons
        for (Block block : e.getBlocks()) {
            // get all blocks touching the end location's block of the piston
            Set<Block> blocksTouching = BlockUtils.getFacesTouching(BlockUtils.Companion.getFaces(), block.getRelative(e.getDirection()));
            for (Block b : blocksTouching) {
                // not a beacon
                if (b.getType() != Material.BEACON) {
                    continue;
                }

                // team beacon exists at this block
                if (BBU.getInstance().getTeamController().getTeam(b) == null) {
                    continue;
                }

                // cancel the event
                e.setCancelled(true);
            }
        }
    }

    //prevent mushrooms from growing on beacons (they can be used to break beacons)
    @EventHandler
    public void onMushroomGrow(BlockFertilizeEvent e) {
        if (e.getBlock().getType() != Material.RED_MUSHROOM && e.getBlock().getType() != Material.BROWN_MUSHROOM) {
            return;
        }

        for (BlockState blockState : e.getBlocks()) {
            Location location = blockState.getLocation();
            BBUTeam team = BBU.getInstance().getTeamController().getTeam(location.getBlock());
            if (team != null) {
                System.out.println("mushroom grown on beacon, cancelling");
                e.setCancelled(true);
                break;
            }

        }
    }

    @EventHandler
    public void onBlockFall(EntityChangeBlockEvent e) {
        // prevent gravel and sand from falling near beacons
        if (!(e instanceof FallingBlock))
            return;

        if (!e.getTo().hasGravity())
            return;

        // get all blocks touching the end location's block of the falling block
        Set<Block> blocksTouching = BlockUtils.getFacesTouching(BlockUtils.Companion.getFaces(), e.getBlock());
        for (Block b : blocksTouching) {
            // not a beacon
            if (b.getType() != Material.BEACON) {
                continue;
            }

            // team beacon exists at this block
            if (BBU.getInstance().getTeamController().getTeam(b) == null) {
                continue;
            }

            // cancel the event
            e.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (BBU.getInstance().getGame().getGameState().isSpawn()) {
            BBUPlayer player = BBU.getInstance().getPlayerController().getPlayer(e.getPlayer().getUniqueId());
            if (!player.getBuild())
                e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (BBU.getInstance().getGame().getGameState().isSpawn()) {
            BBUPlayer player = BBU.getInstance().getPlayerController().getPlayer(e.getPlayer().getUniqueId());
            if (!player.getBuild())
                e.setCancelled(true);
        }
    }

}
