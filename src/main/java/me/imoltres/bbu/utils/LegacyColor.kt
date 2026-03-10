package me.imoltres.bbu.utils

import net.kyori.adventure.text.format.NamedTextColor

enum class LegacyColor(val code: Char, val namedTextColor: NamedTextColor?) {
    BLACK('0', NamedTextColor.BLACK),
    DARK_BLUE('1', NamedTextColor.DARK_BLUE),
    DARK_GREEN('2', NamedTextColor.DARK_GREEN),
    DARK_AQUA('3', NamedTextColor.DARK_AQUA),
    DARK_RED('4', NamedTextColor.DARK_RED),
    DARK_PURPLE('5', NamedTextColor.DARK_PURPLE),
    GOLD('6', NamedTextColor.GOLD),
    GRAY('7', NamedTextColor.GRAY),
    DARK_GRAY('8', NamedTextColor.DARK_GRAY),
    BLUE('9', NamedTextColor.BLUE),
    GREEN('a', NamedTextColor.GREEN),
    AQUA('b', NamedTextColor.AQUA),
    RED('c', NamedTextColor.RED),
    LIGHT_PURPLE('d', NamedTextColor.LIGHT_PURPLE),
    YELLOW('e', NamedTextColor.YELLOW),
    WHITE('f', NamedTextColor.WHITE),

    UNDERLINE('n', null),
    BOLD('l', null),
    STRIKETHROUGH('m', null),
    ITALIC('o', null),
    RESET('r', null),
    MAGIC('k', null);

    companion object {
        private val codeMap = entries.associateBy { it.code }

        fun fromCode(code: Char): LegacyColor? {
            return codeMap[code]
        }
    }

    fun isStyle(): Boolean {
        return namedTextColor == null
    }

    fun asTextColor(): NamedTextColor? {
        return namedTextColor
    }

    override fun toString(): String {
        return "&$code"
    }

}