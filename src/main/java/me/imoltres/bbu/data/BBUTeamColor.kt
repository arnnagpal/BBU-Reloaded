package me.imoltres.bbu.data

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import me.imoltres.bbu.utils.LegacyColor
import java.util.concurrent.CompletableFuture

/**
 * Enum holding all the team colours
 */
enum class BBUTeamColor(val chatColor: LegacyColor) {
    RED(LegacyColor.RED),
    GREEN(LegacyColor.GREEN),
    BLUE(LegacyColor.BLUE),
    YELLOW(LegacyColor.YELLOW),
    ORANGE(LegacyColor.GOLD),
    PURPLE(LegacyColor.DARK_PURPLE),
    PINK(LegacyColor.LIGHT_PURPLE),
    GRAY(LegacyColor.GRAY)
    ;
}

// custom argument types
class TeamArgumentType : CustomArgumentType.Converted<BBUTeamColor, String> {
    override fun convert(nativeType: String): BBUTeamColor {
        try {
            return BBUTeamColor.valueOf(nativeType.uppercase())
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid team colour: $nativeType")
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for (colour in BBUTeamColor.entries) {
            val name = colour.name.lowercase()

            // Only suggest if the flavor name matches the user input
            if (name.startsWith(builder.remainingLowerCase)) {
                builder.suggest(name)
            }
        }

        return builder.buildFuture()
    }

    override fun getNativeType(): ArgumentType<String> {
        return com.mojang.brigadier.arguments.StringArgumentType.word()
    }
}