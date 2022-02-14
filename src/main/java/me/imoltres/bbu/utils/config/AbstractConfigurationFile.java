package me.imoltres.bbu.utils.config;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * The parent configuration file
 */
public abstract class AbstractConfigurationFile {
    @Getter
    private final JavaPlugin plugin;

    @Getter
    private final String name;

    /**
     * Initialise an instance with a plugin & name defined
     *
     * @param plugin bukkit plugin
     * @param name   name of the configuration file (don't add the yml)
     */
    public AbstractConfigurationFile(JavaPlugin plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    /**
     * Get the property in the config file as a String
     *
     * @param var1 property
     * @return stringified result
     */
    public abstract String getString(String var1);

    /**
     * Get the property in the config file as a {@link java.lang.String}.
     * If it doesn't exist, then return var2
     *
     * @param var1 property
     * @param var2 default value
     * @return stringified result
     */
    public abstract String getStringOrDefault(String var1, String var2);

    /**
     * Get the property in the config file as an {@link java.lang.Integer}.
     *
     * @param var1 property
     * @return integer result
     */
    public abstract int getInteger(String var1);

    /**
     * Get the property in the config file as a {@link java.lang.Double}.
     *
     * @param var1 property
     * @return double result
     */
    public abstract double getDouble(String var1);

    /**
     * Get the property in the config file as a {@link java.lang.Object}.
     *
     * @param var1 property
     * @return object result
     */
    public abstract Object get(String var1);

    /**
     * Get the property in the config file as a {@link java.util.List}.
     *
     * @param var1 property
     * @return list result
     */
    public abstract List<String> getStringList(String var1);


}
