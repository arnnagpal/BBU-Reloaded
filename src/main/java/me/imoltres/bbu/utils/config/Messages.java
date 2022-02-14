package me.imoltres.bbu.utils.config;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;

/**
 * Messages imported from the messages.yml config
 */
@RequiredArgsConstructor
public enum Messages {

    SCOREBOARD_TITLE("scoreboard-title"),
    BEACON_DESTROYED("beacon-destroyed"),
    FINAL_DEATH("final-death");

    private final String path;

    @Override
    public String toString() {
        return BBU.getInstance().getMessagesConfig().getString(path);
    }
}
