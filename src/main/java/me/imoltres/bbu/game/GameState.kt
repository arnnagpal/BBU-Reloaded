package me.imoltres.bbu.game

enum class GameState(val startsAfterTick: Int, val display: String?, val pvp: Boolean) {
    PRE_GAME(-1, null, false),
    GRACE(0, null, false),
    PVP(1800, "PVP", true),
    PVP_BORDER_SHRINK(3600, "Border", true),
    POST_GAME(-1, null, true)
    ;

    val tick: Int
        get() = if (startsAfterTick > 0) values()[ordinal - 1].startsAfterTick + startsAfterTick else startsAfterTick

    operator fun next(): GameState? {
        return if (this != POST_GAME) values()[ordinal + 1] else null
    }

    companion object {
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