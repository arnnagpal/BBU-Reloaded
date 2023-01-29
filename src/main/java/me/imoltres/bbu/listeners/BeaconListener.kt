package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.game.GameState
import me.imoltres.bbu.game.events.team.BBUBreakBeaconEvent
import me.imoltres.bbu.game.events.team.BBUPlaceBeaconEvent
import me.imoltres.bbu.utils.general.BlockUtils
import me.imoltres.bbu.utils.general.BlockUtils.Companion.faces
import me.imoltres.bbu.utils.general.BlockUtils.Companion.getFacesTouching
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.item.ItemConstants
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import java.util.stream.Collectors

class BeaconListener : Listener {

    //todo: figure out a better method to handle beacon place ranges
    @EventHandler
    fun onBlockPlaceNearBeacon(e: BlockPlaceEvent) {
        val materialList =
            getFacesTouching(faces, e.block).stream().collect(Collectors.toMap(Block::getLocation, Block::getType))

        for (entry in materialList.entries) {
            if (entry.value == Material.BEACON) {
                if (BBU.getInstance().teamController.getTeam(entry.key.block) != null) {
                    e.isCancelled = true
                    return
                }
            }
        }
    }

    @EventHandler
    fun onBeaconPlace(e: BlockPlaceEvent) {
        if (e.block.type != Material.BEACON)
            return
        if (!e.itemInHand.isSimilar(ItemConstants.TEAM_BEACON))
            return

        //is bbu beacon :D
        val player = e.player
        val team = BBU.getInstance().teamController.getTeam(player) ?: return
        if (BBU.getInstance().game.gameState != GameState.GRACE)
            return

        val event = BBUPlaceBeaconEvent(
            team,
            BBU.getInstance().playerController.getPlayer(player.uniqueId),
            WorldPosition.fromBukkitLocation(e.block.location)
        )
        Bukkit.getPluginManager().callEvent(event)

        e.isCancelled = event.isCancelled

        if (e.isCancelled) {
            player.sendActionBar(CC.translate("&cCannot place that there."))
        } else {
            team.beacon = WorldPosition.fromBukkitLocation(e.block.location)
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("&aSet `&${team.colour.chatColor.char}${team.colour.name}` &ateam's beacon to ${team.beacon.toString()}"))
        }

    }

    @EventHandler
    fun onBeaconBreak(e: BlockBreakEvent) {
        if (e.block.type != Material.BEACON)
            return

        val team = BBU.getInstance().teamController.getTeam(e.block) ?: return
        val breaker = e.player
        val breakerTeam = BBU.getInstance().teamController.getTeam(breaker)

        e.isDropItems = false
        e.expToDrop = 0


        val event = BBUBreakBeaconEvent(
            e.block,
            team,
            BBU.getInstance().playerController.getPlayer(breaker.uniqueId),
            breakerTeam == team
        )
        Bukkit.getPluginManager().callEvent(event)
        e.isCancelled = event.isCancelled

        if (e.isCancelled) {
            breaker.sendActionBar(CC.translate("&cCannot break that."))
        } else {
            team.beacon = null
            Bukkit.getConsoleSender()
                .sendMessage(CC.translate("&aSet `&${team.colour.chatColor.char}${team.colour.name}` &ateam's beacon to null"))
        }
    }

    @EventHandler
    fun onLiquidFlow(e: BlockFromToEvent) {
        if (e.block.type == Material.WATER || e.block.type == Material.LAVA) {
            //prevent liquid flow onto invalid surfaces for beacon
            if (getFacesTouching(faces, e.toBlock)
                    .any { block -> BBU.getInstance().teamController.getTeam(block) != null }
                || BlockUtils.generatesCobble(faces, e.block.type, e.toBlock)
            ) {
                e.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onEmptyBucket(e: PlayerBucketEmptyEvent) {
        if (getFacesTouching(faces, e.block)
                .any { block -> BBU.getInstance().teamController.getTeam(block) != null }
        ) {
            e.isCancelled = true
            e.player.sendActionBar(CC.translate("&cCannot place that there."))
        }
    }

}