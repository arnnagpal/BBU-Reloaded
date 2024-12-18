package me.imoltres.bbu.data

import org.bukkit.ChatColor

/**
 * Enum holding all the team colours
 */
enum class BBUTeamColor(val chatColor: ChatColor) {
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