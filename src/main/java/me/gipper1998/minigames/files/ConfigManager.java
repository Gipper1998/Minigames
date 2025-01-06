package me.gipper1998.minigames.files;

/***
 * Manage the configuration file.
 */

import java.util.List;
import java.util.Objects;
import java.util.Set;

import me.gipper1998.minigames.utils.FileBuilder;
import org.bukkit.Material;

public class ConfigManager
{
    private static ConfigManager cm;
    private FileBuilder config;

    // Constructor.
    public ConfigManager()
    {
        config = new FileBuilder("config.yml");
    }

    // Get the instance of config.
    public static ConfigManager getInstance()
    {
        if (cm == null)
        {
            cm = new ConfigManager();
        }

        return cm;
    }

    // Reload the config.
    public void reloadConfig()
    {
        config.reloadConfig();
    }

    // Grab the block.
    public Material getBlock(String path)
    {
        String block = this.config.getConfig().getString(path);

        // Grab the block, if error, return air.
        try
        {
            return Material.matchMaterial(block.toUpperCase());
        }
        catch (Exception e)
        {
            return Material.AIR;
        }
    }

    // A lot of get methods.
    public int getInt(String path)
    {
        return config.getConfig().getInt(path);
    }

    public boolean getBoolean(String path)
    {
        return config.getConfig().getBoolean(path);
    }

    public boolean contains(String path)
    {
        return config.getConfig().contains(path);
    }

    public List<String> getStringList(String path)
    {
        return config.getConfig().getStringList(path);
    }

    public String getString(String path)
    {
        return config.getConfig().getString(path);
    }

    public Set<String> getConfigurationSection(String path)
    {
        return Objects.requireNonNull(config.getConfig().getConfigurationSection(path)).getKeys(false);
    }
}
