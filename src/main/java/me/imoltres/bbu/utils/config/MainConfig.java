package me.imoltres.bbu.utils.config;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;

/**
 * Configuration properties imported from config.yml
 */
@RequiredArgsConstructor
public enum MainConfig {

    BORDER("border"),
    LOBBY_SPAWN("lobby-spawn");

    private final String path;

    @Override
    public String toString() {
        return BBU.getInstance().getMainConfig().getString(path);
    }

    public int toInt() {
        return BBU.getInstance().getMainConfig().getInteger(path);
    }

}
