package me.imoltres.bbu.listeners

import me.imoltres.bbu.utils.item.ItemConstants
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType

class InventoryListener : Listener {

    @EventHandler
    fun onInventoryClick(e: InventoryClickEvent) {
        if (e.clickedInventory == null) return

        val item = e.currentItem
        if (item == null
            || (!item.isSimilar(ItemConstants.TEAM_BEACON.build())
                    && !item.isSimilar(ItemConstants.TRACKING_COMPASS.build()))
        ) return

        if (e.clickedInventory?.type != InventoryType.PLAYER) {
            e.isCancelled = true
        }
    }

}