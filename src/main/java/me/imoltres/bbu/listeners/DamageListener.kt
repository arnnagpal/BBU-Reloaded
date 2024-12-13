package me.imoltres.bbu.listeners

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.player.BBUPlayer
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.game.events.player.BBUPlayerDeathEvent
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.item.ItemConstants
import me.imoltres.bbu.utils.world.WorldPosition
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack


class DamageListener : Listener {

    //stop lobby damage while we're at it
    @EventHandler
    fun onPlayerDamage(e: EntityDamageEvent) {
        if (e.entity !is Player) {
            return
        }

        e.isCancelled = !BBU.getInstance().game.gameState.isPvp()
    }


    // stop players from damaging each other if friendly fire is disabled
    @EventHandler
    fun onFriendlyFire(e: EntityDamageByEntityEvent) {
        if (e.entity !is Player)
            return
        val damaged = e.entity as Player
        var damager: Player? = null

        // get the player damager (through the projectile or the player)
        if (e.damager is Projectile) {
            val proj = e.damager as Projectile
            if (proj.shooter is Player) {
                damager = proj.shooter as Player
            }
        } else if (e.damager is Player) {
            damager = e.damager as Player
        }

        if (damager == null) {
            println("Damager is null, ignoring damage event")
            return
        }

        var bbuDamager: BBUPlayer? = BBU.getInstance().playerController.getPlayer(damager.uniqueId)

        //if pvp is disabled, cancel the damage
        if (!MainConfig.FRIENDLY_FIRE) {
            val damagedTeam: BBUTeam? = BBU.getInstance().teamController.getTeam(damaged)
            val damagerTeam = bbuDamager!!.team
            if (damagedTeam === damagerTeam) {
                e.isCancelled = true
            }
        }

        e.isCancelled = !BBU.getInstance().game.gameState.isPvp()

        if (e.isCancelled && e.damager is Arrow && bbuDamager != null)
            bbuDamager.giveItemSafely(
                ItemStack(Material.ARROW),
                true
            )
    }

    //todo: add respawn listener

    @EventHandler
    fun onPlayerDeath(e: PlayerDeathEvent) {
        val player = e.player
        val bbuPlayer = BBU.getInstance().playerController.getPlayer(player.uniqueId)
        val team = BBU.getInstance().teamController.getTeam(player) ?: return

        val iterator: MutableIterator<ItemStack> = e.drops.iterator()
        while (iterator.hasNext()) {
            val drop = iterator.next()
            //keep beacon & compass after death
            if (drop.isSimilar(ItemConstants.TEAM_BEACON) || ItemConstants.isSimilar(
                    ItemConstants.TRACKING_COMPASS,
                    drop
                )
            ) {
                iterator.remove()
                e.itemsToKeep.add(drop)
            }
        }

        val killer = BBU.getInstance().playerController.getPlayer(player.killer?.uniqueId)

        val event =
            BBUPlayerDeathEvent(WorldPosition.fromBukkitLocation(player.location), bbuPlayer, killer, !team.hasBeacon())
        Bukkit.getPluginManager().callEvent(event)
    }

}