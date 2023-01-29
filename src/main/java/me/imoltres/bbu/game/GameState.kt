package me.imoltres.bbu.game

/**
 * Game state
 */
enum class GameState(val startsAfterTick: Int, val display: String?) {
    LOBBY(-1, null),
    PRE_GAME(-1, null),
    GRACE(0, null),
    PVP(1800, "PVP"),
    PVP_BORDER_SHRINK(3600, "Border"),
    DEATH_MATCH(4500, "Deathmatch"),
    DEATH_MATCH_PVP(4530, "Deathmatch PVP"),
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
        return this == PVP || this == PVP_BORDER_SHRINK || this == DEATH_MATCH_PVP
    }

    /**
     * @return is the game state a spawn state
     */
    fun isSpawn(): Boolean {
        return this == LOBBY || this == PRE_GAME
    }

    companion object {
        /**
         * get a game state based on the tick that the game is in
         * @return the gamestate (nullable)
         */
        fun getGameStateFromTick(tick: Int): GameState? {
            val secondTick = tick / 20

            var s: GameState? = null
            for (state in values()) {
                if (state.startsAfterTick == -1) continue
                if (secondTick >= state.tick) {
                    s = state
                }
            }
            return s
        }
    }
}