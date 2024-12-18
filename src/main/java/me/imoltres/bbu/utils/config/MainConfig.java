package me.imoltres.bbu.utils.config;

import me.imoltres.bbu.BBU;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Configuration properties imported from config.yml
 */
public class MainConfig<T> extends ConfigGetter<T> {

    public static int BORDER_SIZE = new MainConfig<Integer>("border.size").get();
    public static ArrayList<LinkedHashMap<String, Integer>> BORDER_PHASES = new MainConfig<ArrayList<LinkedHashMap<String, Integer>>>("border.shrink-phases").get();

    public static boolean DEATHMATCH_ENABLED = new MainConfig<Boolean>("deathmatch.enabled").get();
    public static int DEATHMATCH_TIME = new MainConfig<Integer>("deathmatch.time").get();

    public static boolean SPECTATE_AFTER_DEATH = new MainConfig<Boolean>("spectate-after-death").get();

    public static boolean FRIENDLY_FIRE = new MainConfig<Boolean>("friendly-fire").get();

    public static String LOBBY_SPAWN = new MainConfig<String>("lobby-spawn").get();
    public static String DEATHMATCH_SPAWN = new MainConfig<String>("deathmatch.spawn").get();

    MainConfig(String path) {
        super(BBU.getInstance().getMainConfig(), path);
    }

    public static void reload() {
        BBU.getInstance().getMainConfig().reloadConfig();

        //i really wish there was a better way to do this
        BORDER_SIZE = new MainConfig<Integer>("border.size").get();
        BORDER_PHASES = new MainConfig<ArrayList<LinkedHashMap<String, Integer>>>("border.shrink-phases").get();

        DEATHMATCH_ENABLED = new MainConfig<Boolean>("deathmatch.enabled").get();
        DEATHMATCH_TIME = new MainConfig<Integer>("deathmatch.time").get();

        SPECTATE_AFTER_DEATH = new MainConfig<Boolean>("spectate-after-death").get();

        FRIENDLY_FIRE = new MainConfig<Boolean>("friendly-fire").get();

        LOBBY_SPAWN = new MainConfig<String>("lobby-spawn").get();
        DEATHMATCH_SPAWN = new MainConfig<String>("deathmatch.spawn").get();
    }

}
