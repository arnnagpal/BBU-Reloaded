package me.imoltres.bbu.data.player

import me.imoltres.bbu.data.team.BBUTeam
import me.imoltres.bbu.scoreboard.BBUScoreboardAdapter
import me.imoltres.bbu.utils.CC
import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*


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

    var scoreboard: BBUScoreboardAdapter? = null
    var team: BBUTeam? = null

    var build = false

    fun getDisplayName(): TextComponent {
        return CC.translate(getRawDisplayName())
    }

    fun getRawDisplayName(): String {
        team ?: return name

        return "&" + team?.colour?.chatColor?.char + name
    }

    fun giveItemSafely(item: ItemStack): Boolean {
        return giveItemSafely(item, false)
    }

    fun giveItemSafely(item: ItemStack, drop: Boolean): Boolean {
        if (player?.inventory?.firstEmpty() == -1 && !drop) {
            player?.sendActionBar(CC.translate("&cEmpty a slot first."))
            return false
        } else {
            player?.location?.let { player?.world?.dropItemNaturally(it, item) }
        }

        player?.inventory?.addItem(item)
        player?.sendActionBar(CC.translate("&aAdded " + item.displayName + " to your inventory."))
        return true
    }

}