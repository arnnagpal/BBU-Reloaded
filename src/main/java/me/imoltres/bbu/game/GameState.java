package me.imoltres.bbu.game;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GameState {
    PRE_GAME(-1, null),
    GRACE(0, null),
    PVP(1800, "PVP"),
    PVP_BORDER_SHRINK(3600, "Border"),
    POST_GAME(-1, null);

    private final int startsAfterTick;
    private final String display;

    public static GameState getGameStateFromTick(int tick) {
        GameState s = null;
        for (GameState state : values()) {
            if (state.getTick() >= tick) {
                s = state;
            }
        }

        return s;
    }

    public int getTick() {
        if (startsAfterTick > 0)
            return values()[this.ordinal() - 1].startsAfterTick + this.startsAfterTick;
        else
            return startsAfterTick;
    }

    public GameState next() {
        if (this != POST_GAME)
            return values()[ordinal() + 1];
        return null;
    }
}
