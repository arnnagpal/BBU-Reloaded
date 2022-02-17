package me.imoltres.bbu.utils.config;

import me.imoltres.bbu.BBU;

import java.util.List;

/**
 * Configuration properties imported from nerfs.yml
 */
public class Nerfs<T> extends ConfigGetter<T> {

    public static double POTION_DAMAGE = new Nerfs<Double>("potions.damage").get();
    public static double POTION_WEAKNESS = new Nerfs<Double>("potions.weakness").get();
    public static double POTION_STRENGTH = new Nerfs<Double>("potions.strength").get();

    public static List<String> REMOVE_MOBS_SPAWN = new Nerfs<List<String>>("remove-mobs-spawn").get();

    Nerfs(String path) {
        super(BBU.getInstance().getNerfsConfig(), path);
    }

}
