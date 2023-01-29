package me.imoltres.bbu.data.player

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.config.Messages
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

/**
 * Represents a custom player
 */
class BBUPlayer(val uniqueId: UUID, val name: String) {
    @Transient
    var player: Player? = null
        get() {
            if (field == null) {
                if (Bukkit.getPlayer(uniqueId) == null) {
                    System.out.printf("'%s' is offline, can't retrieve bukkit player.\n", name)
                    return null
                }
                field = Bukkit.getPlayer(uniqueId)
            }
            return field
        }
        private set

    var eliminated = false

    var scoreboard: BBUScoreboardAdapter? = null
        set(value) {
            println("Set player's scoreboard.")
            field = value
        }
    var team: BBUTeam? = null
    var build = false

    /**
     * @return the TextComponent version of the display name
     */
    fun getDisplayName(): TextComponent {
        return CC.translate(getRawDisplayName())
    }

    /**
     * @return a raw bukkit string version of the display name
     */
    fun getRawDisplayName(): String {
        team ?: return name

        return "&" + team?.colour?.chatColor?.char + name
    }

    /**
     * Give an item safely to the player, alerting them that they have a full inventory (if it is)
     *
     * @param item item to give
     * @return if it was successful
     */
    fun giveItemSafely(item: ItemStack): Boolean {
        return giveItemSafely(item, false)
    }

    /**
     * Give an item safely to the player
     *
     * @param item item to give
     * @param drop should drop the item if the inventory is full
     * @return if it was successful
     */
    fun giveItemSafely(item: ItemStack, drop: Boolean): Boolean {
        if (player?.inventory?.firstEmpty() == -1) {
            if (drop) {
                player?.location?.let { player?.world?.dropItemNaturally(it, item) }
            } else {
                player?.sendActionBar(CC.translate("&cEmpty a slot first."))
                return false
            }
        } else {
            player?.inventory?.addItem(item)
        }

        player?.sendActionBar(CC.translate("&aAdded ${item.displayName} &ato your inventory."))
        return true
    }

    fun preventMovement() {
        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            player?.addPotionEffect(PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 255, false, false))
            player?.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 255, false, false))
            player?.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 255, false, false))
            player?.walkSpeed = 0.0F
        })
    }

    fun allowMovement() {
        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            player?.activePotionEffects?.forEach { potionEffect ->
                player?.removePotionEffect(potionEffect.type)
            }

            player?.walkSpeed = 0.2F
        })
    }

    /**
     * eliminate this player
     */
    fun eliminate() {
        eliminated = true
        team?.removePlayer(this)

        player?.kick(CC.translate(Messages.FINAL_DEATH.toString()))
    }

    /**
     * revive this player
     */
    fun revive() {
        eliminated = false
        team?.addPlayer(this)
    }

}