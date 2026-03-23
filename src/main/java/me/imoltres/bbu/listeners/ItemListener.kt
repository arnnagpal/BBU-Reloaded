package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.utils.item.ItemConstants
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerPlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.PlayerDropItemEvent


class ItemListener : Listener {

    @EventHandler
    fun onItemDrop(e: PlayerDropItemEvent) {
        // check item type
        val player = e.player
        val item = e.itemDrop

        // cant drop tracking compass
        if (item.itemStack.isSimilar(ItemConstants.TRACKING_COMPASS.build())) {
            e.isCancelled = true
            return
        }

        if (!item.itemStack.isSimilar(ItemConstants.TEAM_BEACON.build())) {
            return
        }

        // get player's team
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId) ?: return
        val team = bbuPlayer.team ?: return

        team.droppedBeaconItem = item

        // make item indestructible
        item.isUnlimitedLifetime = true
        item.isImmuneToFire = true
        item.isImmuneToCactus = true
        item.isImmuneToExplosion = true
        item.isImmuneToLightning = true

        val nmsEntity: net.minecraft.world.entity.Entity = item as net.minecraft.world.entity.Entity

        // for everyone on the team, add the glow effect to the item
        for (member in team.players) {
            val serverPlayer: ServerPlayer = (member.player ?: continue) as ServerPlayer
            glowItem(serverPlayer, nmsEntity)
        }
    }

    @EventHandler
    fun onItemPickup(e: EntityPickupItemEvent) {
        if (!e.item.itemStack.isSimilar(ItemConstants.TEAM_BEACON.build())) {
            return
        }
        val entity = e.entity
        val item = e.item

        if (entity !is Player) {
            e.isCancelled = true
            return
        }

        val player = entity as Player
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
        if (bbuPlayer == null) {
            e.isCancelled = true
            return
        }

        val team = bbuPlayer.team
        // if it aint yours, you cant pick it up
        if (team == null || team.droppedBeaconItem == null || team.droppedBeaconItem!!.uniqueId != item.uniqueId) {
            e.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onHopperPickup(e: InventoryPickupItemEvent) {
        if (!e.item.itemStack.isSimilar(ItemConstants.TEAM_BEACON.build())) {
            return
        }

        e.isCancelled = true
    }

    fun glowItem(player: ServerPlayer, entity: net.minecraft.world.entity.Entity) {
        val data: List<SynchedEntityData.DataValue<Byte>> = listOf(
            SynchedEntityData.DataValue(0, EntityDataSerializers.BYTE, 0x40.toByte())
        )
        val packet = ClientboundSetEntityDataPacket(
            entity.id,
            data
        )

        player.connection.send(packet)
    }

}