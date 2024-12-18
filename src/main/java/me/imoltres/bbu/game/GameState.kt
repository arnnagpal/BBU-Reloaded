package me.imoltres.bbu.game

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
    DEATHMATCH(5400, "Deathmatch"), // 5400 seconds = 1 hour 30 minutes, will switch to deathmatch 30min after border shrink
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