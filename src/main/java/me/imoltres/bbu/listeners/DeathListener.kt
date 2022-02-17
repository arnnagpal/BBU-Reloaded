package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.events.player.BBUPlayerDeathEvent
import me.imoltres.bbu.utils.item.ItemConstants
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack


class DeathListener : Listener {

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.player
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
        val team = BBU.getInstance().teamController.getTeam(player) ?: return

        val iterator: MutableIterator<ItemStack> = e.drops.iterator()
        while (iterator.hasNext()) {
            val drop = iterator.next()
            //keep beacon & compass after death
            if (drop.isSimilar(ItemConstants.TEAM_BEACON) || ItemConstants.isSimilar(ItemConstants.TRACKING_COMPASS, drop)) {
                iterator.remove()
                e.itemsToKeep.add(drop)
            }
        }

        val killer = BBU.getInstance().playerController.getPlayer(player.killer?.uniqueId)

        val event = BBUPlayerDeathEvent(bbuPlayer, killer, !team.hasBeacon())
        Bukkit.getPluginManager().callEvent(event)
    }

}