package me.imoltres.bbu.data

import org.bukkit.ChatColor

/**
 * Enum holding all the team colours
 * @param chatColor the colour of the team
 * @param material the material of the team's color This is optional, and is only used for the trackers.
 */
enum class BBUTeamColour(val chatColor: ChatColor, val material: String? = null) {
    RED(ChatColor.RED),
    GREEN(ChatColor.GREEN),
    BLUE(ChatColor.BLUE),
    YELLOW(ChatColor.YELLOW),
    ORANGE(ChatColor.GOLD),
    PURPLE(ChatColor.DARK_PURPLE),
    PINK(ChatColor.LIGHT_PURPLE),
    GRAY(ChatColor.GRAY)
    ;
}