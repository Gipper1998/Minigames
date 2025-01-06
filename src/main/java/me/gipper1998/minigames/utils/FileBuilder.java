package me.gipper1998.minigames.utils;

/***
 * File Builder to build files.
 */

import me.gipper1998.minigames.Minigames;
import me.gipper1998.minigames.files.MessageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileBuilder
{
    private FileConfiguration dataConfig;
    private File dataConfigFile;
    private String name;

    public FileBuilder(String name)
    {
        this.name = name;
        saveDefaultConfig();
    }

    public void reloadConfig()
    {
        if (dataConfigFile == null)
        {
            dataConfigFile = new File(Minigames.getInstance().getDataFolder(), name);
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataConfigFile);
        InputStream defConfigStream = Minigames.getInstance().getResource(name);
        if (defConfigStream != null)
        {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(defConfigStream));
            dataConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getConfig()
    {
        if (dataConfig == null)
        {
            reloadConfig();
        }
        return dataConfig;
    }

    public void saveConfig()
    {
        if ((dataConfig == null) || (dataConfigFile == null))
        {
            return;
        }
        try
        {
            getConfig().save(dataConfigFile);
        } catch (IOException e)
        {
            MessageManager.getInstance().sendConsoleMessage("<prefix> &cFile &d" + name + " &cFailed to load, backup previous data from &d" + name + " &cand try again.");
        }
    }

    public void saveDefaultConfig()
    {
        if (dataConfigFile == null)
        {
            dataConfigFile = new File(Minigames.getInstance().getDataFolder(), name);
        }
        if (!dataConfigFile.exists())
        {
            Minigames.getInstance().saveResource(name, false);
        }
    }

}