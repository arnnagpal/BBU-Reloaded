package me.imoltres.bbu.utils.scoreboard

import org.bukkit.ChatColor

object BBUScoreboardUtils {
    @JvmStatic
    fun splitTeamText(_input: String): Array<String> {
        val input = ChatColor.translateAlternateColorCodes('&', _input)

        val inputLength = input.length
        return if (inputLength > 16) {
            // Make the prefix the first 16 characters of our text
            var prefix = input.substring(0, 16)

            // Get the last index of the color char in the prefix
            val lastColorIndex = prefix.lastIndexOf(ChatColor.COLOR_CHAR)
            var suffix: String
            if (lastColorIndex >= 14) {
                prefix = prefix.substring(0, lastColorIndex)
                suffix = ChatColor.getLastColors(input.substring(0, 17)) + input.substring(lastColorIndex + 2)
            } else {
                suffix = ChatColor.getLastColors(prefix) + input.substring(16)
            }
            if (suffix.length > 16) {
                suffix = suffix.substring(0, 16)
            }
            prefix = prefix.replace(ChatColor.COLOR_CHAR, '&')
            suffix = suffix.replace(ChatColor.COLOR_CHAR, '&')
            arrayOf(prefix, suffix)
        } else {
            arrayOf(input, "")
        }

    }
}