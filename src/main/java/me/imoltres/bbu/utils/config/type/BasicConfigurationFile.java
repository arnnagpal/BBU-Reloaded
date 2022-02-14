package me.imoltres.bbu.utils.config.type;

import lombok.Getter;
import me.imoltres.bbu.utils.config.AbstractConfigurationFile;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.List;

public class BasicConfigurationFile extends AbstractConfigurationFile {
    @Getter
    private final File file;
    @Getter
    private final YamlConfiguration configuration;

    /**
     * Initialise an instance with a plugin, name, and if
     * the original config file should be overwritten
     *
     * @param plugin    bukkit plugin
     * @param name      name of the configuration file (don't add the yml)
     * @param overwrite overwrite the preexisting config file (if it exists)
     */
    public BasicConfigurationFile(JavaPlugin plugin, String name, boolean overwrite) {
        super(plugin, name);
        this.file = new File(plugin.getDataFolder(), name + ".yml");
        saveResource(name + ".yml", plugin.getDataFolder(), overwrite);
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Initialise an instance with a plugin, name, if
     * the original config file should be overwritten, and
     * a destination folder to put the config file in
     *
     * @param plugin    bukkit plugin
     * @param folder    destination folder
     * @param name      name of the configuration file (don't add the yml)
     * @param overwrite overwrite the preexisting config file (if it exists)
     */
    public BasicConfigurationFile(JavaPlugin plugin, File folder, String name, boolean overwrite) {
        super(plugin, name);
        if (!folder.exists())
            folder.mkdir();

        this.file = new File(folder, name + ".yml");
        saveResource(name + ".yml", folder, overwrite);
        this.configuration = YamlConfiguration.loadConfiguration(this.file);
    }

    /**
     * Initialise an instance with a plugin, name
     * 
     * doesn't overwrite by default.
     *
     * @param plugin bukkit plugin
     * @param name   name of the configuration file (don't add the yml)
     */
    public BasicConfigurationFile(JavaPlugin plugin, String name) {
        this(plugin, name, false);
    }

    public String getString(String path) {
        return this.configuration.contains(path) ? ChatColor.translateAlternateColorCodes('&', this.configuration.getString(path)) : null;
    }

    public String getStringOrDefault(String path, String or) {
        String toReturn = this.getString(path);
        return toReturn == null ? or : toReturn;
    }

    public int getInteger(String path) {
        return this.configuration.contains(path) ? this.configuration.getInt(path) : 0;
    }

    public boolean getBoolean(String path) {
        return this.configuration.contains(path) && this.configuration.getBoolean(path);
    }

    public double getDouble(String path) {
        return this.configuration.contains(path) ? this.configuration.getDouble(path) : 0.0D;
    }

    public Object get(String path) {
        return this.configuration.contains(path) ? this.configuration.get(path) : null;
    }

    public List<String> getStringList(String path) {
        return this.configuration.contains(path) ? this.configuration.getStringList(path) : null;
    }

    private void saveResource(String resourcePath, File outDir, boolean replace) {
        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getPlugin().getResource(resourcePath);
        if (in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + file);
        }

        File outFile = new File(outDir, resourcePath);

        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        try {
            if (!outFile.exists() || replace) {
                OutputStream out = new FileOutputStream(outFile);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                out.close();
                in.close();
            } else {
                System.out.println("Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
            }
        } catch (IOException ex) {
            System.out.println("Could not save " + outFile.getName() + " to " + outFile);
            ex.printStackTrace();
        }
    }

}
