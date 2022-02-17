package me.imoltres.bbu.utils.config;

import lombok.RequiredArgsConstructor;
import me.imoltres.bbu.BBU;
import me.imoltres.bbu.utils.config.type.BasicConfigurationFile;

/**
 * Configuration properties imported from config.yml
 */
public class MainConfig<T> extends ConfigGetter<T> {

    public static int BORDER = new MainConfig<Integer>("border").get();
    public static String LOBBY_SPAWN = new MainConfig<String>("lobby-spawn").get();

    public static boolean FRIENDLY_FIRE = new MainConfig<Boolean>("friendly-fire").get();

    MainConfig(String path) {
        super(BBU.getInstance().getMainConfig(), path);
    }

}
