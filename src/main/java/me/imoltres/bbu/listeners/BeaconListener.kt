package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.events.team.BBUBreakBeaconEvent
import me.imoltres.bbu.game.events.team.BBUPlaceBeaconEvent
import me.imoltres.bbu.utils.BlockUtils
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.ItemConstants
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent

class BeaconListener : Listener {

    @EventHandler
    fun onBeaconPlace(e: BlockPlaceEvent) {
        if (e.block.type != Material.BEACON)
            return
        if (!e.itemInHand.isSimilar(ItemConstants.TEAM_BEACON))
            return

        //is bbu beacon :D
        val player = e.player
        val team = BBU.instance.teamController.getTeam(player) ?: return

        val event = BBUPlaceBeaconEvent(
            team,
            BBU.instance.playerController.getPlayer(player.uniqueId),
            WorldPosition.fromBukkitLocation(e.block.location)
        )
        Bukkit.getPluginManager().callEvent(event)

        e.isCancelled = event.isCancelled

        if (e.isCancelled) {
            player.sendActionBar(CC.translate("&cCannot place that there."))
        } else {
            team.beacon = WorldPosition.fromBukkitLocation(e.block.location)
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("&aSet `&${team.colour.chatColor.char}${team.colour.name}` team's beacon to ${team.beacon.toString()}"))
        }

    }

    @EventHandler
    fun onBeaconBreak(e: BlockBreakEvent) {
        if (e.block.type != Material.BEACON)
            return

        val team = BBU.instance.teamController.getTeam(e.block) ?: return
        val breaker = e.player
        val breakerTeam = BBU.instance.teamController.getTeam(breaker)

        e.isDropItems = false
        e.expToDrop = 0


        val event = BBUBreakBeaconEvent(
            e.block,
            team,
            BBU.instance.playerController.getPlayer(breaker.uniqueId),
            breakerTeam == team
        )
        Bukkit.getPluginManager().callEvent(event)
        e.isCancelled = event.isCancelled

        if (e.isCancelled) {
            breaker.sendActionBar(CC.translate("&cCannot break that."))
        } else {
            team.beacon = null
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("&aSet `&${team.colour.chatColor.char}${team.colour.name}` team's beacon to null"))
        }
    }

    @EventHandler
    fun onLiquidFlow(e: BlockFromToEvent) {
        if (e.block.type == Material.WATER || e.block.type == Material.LAVA) {
            //prevent liquid flow onto invalid surfaces for beacon
            if (BlockUtils.facesTouching(BlockUtils.faces, e.toBlock)
                    .any { block -> BBU.instance.teamController.getTeam(block) != null }
                || BlockUtils.generatesCobble(BlockUtils.faces, e.block.type, e.toBlock)
            ) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEmptyBucket(e: PlayerBucketEmptyEvent) {
        if (BlockUtils.facesTouching(BlockUtils.faces, e.block)
                .any { block -> BBU.instance.teamController.getTeam(block) != null }
        ) {
            e.isCancelled = true
            e.player.sendActionBar(CC.translate("&cCannot place that there."))
        }
    }

}