package me.imoltres.bbu.game

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.argument.CustomArgumentType
import java.util.concurrent.CompletableFuture

/**
 * Game state
 * @param startTime a time (in seconds) that the game state should start at (if it's -1, then it's a condition based or a manually set state)
 * @param display the display name of the game state
 */
enum class GameState(val startTime: Int, val display: String?) {
    LOBBY(-1, null),
    PRE_GAME(-1, null),
    GRACE(0, null),
    PVP(1800, "PVP"), // 1800 seconds = 30 minutes, switch to PVP after 30 minutes of grace
    PVP_BORDER_SHRINK(3600, "Border"), // 3600 seconds = 1 hour, border will start shrinking 30 minutes after pvp
    DEATHMATCH(
        -1,
        "Deathmatch"
    ), // deathmatch will start when the border shrinks to a certain size, so it doesn't have a set time
    POST_GAME(-1, null)
    ;

    /**
     * @return the next game state after the current one
     */
    operator fun next(): GameState? {
        return if (this != POST_GAME) entries[ordinal + 1] else null
    }

    /**
     * @return is the game state allowing pvp
     */
    fun isPvp(): Boolean {
        return this == PVP || this == PVP_BORDER_SHRINK || this == DEATHMATCH || this == POST_GAME
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
            val secondTick = tick / 20 // convert minecraft ticks to seconds

            var s: GameState? = null
            for (state in entries) {
                if (state.startTime == -1) continue

                // check if we've passed the start time of the state
                if (secondTick >= state.startTime) {
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