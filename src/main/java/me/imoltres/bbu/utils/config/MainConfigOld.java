package me.imoltres.bbu.utils.config;

import me.imoltres.bbu.BBU;
import me.imoltres.bbu.game.ShrinkPhase;

import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 * Configuration properties imported from config.yml
 * @deprecated This class is deprecated and should not be used. It is only kept for backward compatibility and will be removed in a future update. Replaced by {@link MainConfig}
 */
@Deprecated(forRemoval = true)
public class MainConfigOld<T> extends ConfigGetter<T> {

    public static int BORDER_SIZE = new MainConfigOld<Integer>("border.size").get();
    public static ArrayList<ShrinkPhase> BORDER_PHASES = new ArrayList<>();

    static {
        new MainConfigOld<ArrayList<LinkedHashMap<String, Integer>>>("border.shrink-phases")
                .get()
                .forEach(phase -> {
                    int size = phase.get("size");
                    int time = phase.get("time");
                    int length = phase.get("length");
                    BORDER_PHASES.add(new ShrinkPhase(size, time, length));
                });
    }

    public static boolean DEATHMATCH_ENABLED = new MainConfigOld<Boolean>("deathmatch.enabled").get();
    public static int DEATHMATCH_TIME = new MainConfigOld<Integer>("deathmatch.time").get();
    public static String DEATHMATCH_SPAWN = new MainConfigOld<String>("deathmatch.spawn").get();

    public static boolean SPECTATE_AFTER_DEATH = new MainConfigOld<Boolean>("spectate-after-death").get();

    public static boolean FRIENDLY_FIRE = new MainConfigOld<Boolean>("friendly-fire").get();

    public static String LOBBY_SPAWN = new MainConfigOld<String>("lobby-spawn").get();

    MainConfigOld(String path) {
        super(BBU.getInstance().getMainConfig(), path);
    }

    public static void reload() {
        BBU.getInstance().getMainConfig().reloadConfig();

        //i really wish there was a better way to do this
        BORDER_SIZE = new MainConfigOld<Integer>("border.size").get();
        BORDER_PHASES = new ArrayList<>();
        new MainConfigOld<ArrayList<LinkedHashMap<String, Integer>>>("border.shrink-phases")
                .get()
                .forEach(phase -> {
                    int size = phase.get("size");
                    int time = phase.get("time");
                    int length = phase.get("length");
                    BORDER_PHASES.add(new ShrinkPhase(size, time, length));
                });

        DEATHMATCH_ENABLED = new MainConfigOld<Boolean>("deathmatch.enabled").get();
        DEATHMATCH_TIME = new MainConfigOld<Integer>("deathmatch.time").get();
        DEATHMATCH_SPAWN = new MainConfigOld<String>("deathmatch.spawn").get();

        SPECTATE_AFTER_DEATH = new MainConfigOld<Boolean>("spectate-after-death").get();

        FRIENDLY_FIRE = new MainConfigOld<Boolean>("friendly-fire").get();

        LOBBY_SPAWN = new MainConfigOld<String>("lobby-spawn").get();
    }

}
