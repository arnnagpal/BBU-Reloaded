package me.imoltres.bbu.data.player

import me.imoltres.bbu.BBU
import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter
import me.imoltres.bbu.utils.CC
import me.imoltres.bbu.utils.config.MainConfig
import me.imoltres.bbu.utils.config.Messages
import net.kyori.adventure.text.TextComponent
import org.bukkit.*
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.asKotlinRandom


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
    var switchingSpectator = false
    var spectatingTeam: BBUTeam? = null
    var spectatingActionMsgThread: BukkitTask? = null

    var scoreboard: BBUScoreboardAdapter? = null
        set(value) {
            println("Set player's scoreboard.")
            field = value
        }
    var team: BBUTeam? = null
    var build = false
    var teamChat = false

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

        player?.sendActionBar(CC.translate("&aAdded ${item.type.name} &ato your inventory."))
        return true
    }

    fun preventMovement() {
        Bukkit.getScheduler().runTask(BBU.getInstance(), Runnable {
            player?.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 255, false, false))
            player?.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 255, false, false))
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

    private fun showBlood(deathLocation: Location) {
        val particleCount = 50
        val particleRadius = 1.0

        // cobblestone break sound
        deathLocation.world.playSound(deathLocation, Sound.BLOCK_STONE_BREAK, 1.0F, 1.0F)

        for (i in 0..particleCount) {
            val x = deathLocation.x + Math.random() * particleRadius - particleRadius / 2
            val y = deathLocation.y + ThreadLocalRandom.current().asKotlinRandom().nextDouble(2.0) * particleRadius
            val z = deathLocation.z + Math.random() * particleRadius - particleRadius / 2
            val loc = Location(deathLocation.world, x, y, z)
            deathLocation.world.spawnParticle(
                Particle.DUST,
                loc,
                5,
                Particle.DustOptions(Color.RED, 1.0F)
            )
        }
    }

    private fun showFirework(deathLocation: Location) {
        val loc = deathLocation.toHighestLocation()
        val firework: Firework =
            loc.world.spawnEntity(loc.clone().add(0.0, 50.0, 0.0), EntityType.FIREWORK_ROCKET) as Firework
        val fireworkMeta: FireworkMeta = firework.fireworkMeta

        // Set the type of the firework
        fireworkMeta.addEffect(
            FireworkEffect.builder()
                .withColor(Color.FUCHSIA)
                .with(FireworkEffect.Type.BALL_LARGE)
                .withTrail()
                .withFlicker()
                .build()
        )

        // Set the power of the firework
        fireworkMeta.power = 5


        // Update the firework meta
        firework.fireworkMeta = fireworkMeta
    }

    /**
     * eliminate this player
     */
    fun eliminate(deathLocation: Location) {
        showFirework(deathLocation)
        showBlood(deathLocation)
        eliminated = true

        if (MainConfig.SPECTATE_AFTER_DEATH) {
            spectatingTeam = team
        }

        team?.removePlayer(this)

        if (MainConfig.SPECTATE_AFTER_DEATH) {
            // put player in spectator mode where they can either only spectate their teammates if they have any, otherwise they spectate freely
            player?.gameMode = GameMode.SPECTATOR
            spectatingActionMsgThread = Bukkit.getScheduler().runTaskTimer(BBU.getInstance(), Runnable {
                val str = "&7To switch teammates &eLEFT CLICK &7or &eRIGHT CLICK."
                player?.sendActionBar(
                    CC.translate(
                        "&cYou are spectating." + if ((spectatingTeam?.players?.size
                                ?: 0) > 0
                        ) " $str" else ""
                    )
                )
            }, 0L, 5L)
            if ((spectatingTeam?.players?.size ?: 0) > 0) {
                switchingSpectator = true
                player?.spectatorTarget = spectatingTeam?.players?.first()?.player
            }
        } else {
            player?.kick(CC.translate(Messages.FINAL_DEATH.toString()))
        }
    }

}