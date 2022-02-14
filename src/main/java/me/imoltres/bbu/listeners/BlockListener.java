package me.imoltres.bbu.listeners;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.data.player.BBUPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

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
