package me.imoltres.bbu.game

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

/**
 * Game state
 */
enum class GameState(val startsAfterTick: Int, val display: String?) {
    LOBBY(-1, null),
    PRE_GAME(-1, null),
    GRACE(0, null),
    PVP(1500, "PVP"),
    PVP_BORDER_SHRINK(3600, "Border"),
    POST_GAME(-1, null)
    ;

    val tick: Int
        get() = if (startsAfterTick > 0) values()[ordinal - 1].startsAfterTick + startsAfterTick else startsAfterTick

    /**
     * @return the next game state after the current one
     */
    operator fun next(): GameState? {
        return if (this != POST_GAME) values()[ordinal + 1] else null
    }

    /**
     * @return is the game state allowing pvp
     */
    fun isPvp(): Boolean {
        return this == PVP || this == PVP_BORDER_SHRINK || this == POST_GAME
    }

    /**
     * @return is the game state a spawn state
     */
    fun isSpawn(): Boolean {
        return this == LOBBY || this == PRE_GAME
    }

    override fun toString(): String {
        return name.lowercase()
    }

    companion object {
        /**
         * get a game state based on the tick that the game is in
         * @return the gamestate (nullable)
         */
        fun getGameStateFromTick(tick: Int): GameState? {
            val secondTick = tick / 20

            var s: GameState? = null
            for (state in entries) {
                if (state.startsAfterTick == -1) continue
                if (secondTick >= state.tick) {
                    s = state
                }
            }
            return s
        }
    }
}

// custom argument type
class GameStateArgumentType : CustomArgumentType.Converted<GameState, String> {
    override fun convert(nativeType: String): GameState {
        try {
            return GameState.valueOf(nativeType.uppercase())
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid game state: $nativeType")
        }
    }

    override fun <S : Any> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        for (colour in GameState.entries) {
            val name = colour.name.uppercase()

            // Only suggest if the flavor name matches the user input
            if (name.startsWith(builder.remaining, ignoreCase = true)) {
                builder.suggest(name)
            }
        }

        return builder.buildFuture()
    }

    override fun getNativeType(): ArgumentType<String> {
        return com.mojang.brigadier.arguments.StringArgumentType.word()
    }
}