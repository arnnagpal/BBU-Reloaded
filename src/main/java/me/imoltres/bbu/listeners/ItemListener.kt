package me.imoltres.bbu.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerDropItemEvent

class ItemListener : Listener {
    /**
     * TODO: allow dropping of beacon,
     * TODO: but only picking up of beacon if it's your own team,
     * TODO: and only dropping of beacon in grace period
     *
     * TODO: delete it if it's on the ground when the grace period ends
     *
     * TODO: make it so you can't drop tracking compass ever
     */

    /**
     * todo: dont allow players to put beacon or compass in any other inventory type
     */

    @EventHandler
    fun onItemDrop(e: PlayerDropItemEvent) {

    }



}