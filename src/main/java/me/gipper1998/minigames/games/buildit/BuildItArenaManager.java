package me.gipper1998.minigames.games.buildit;

/***
 * Arena Manager to help make things run smoothly.
 */

import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.utils.FileBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildItArenaManager
{

    // Static variable for later use.
    private static BuildItArenaManager buildItArenaManager;

    // Map of all arenas with the game manager respectively.
    private HashMap<BuildItArena, BuildItGameManager> activeArenas;

    // File for arenas.
    private FileBuilder arenas;

    // Constructor for it.
    public BuildItArenaManager()
    {
        activeArenas = new HashMap();
        arenas = new FileBuilder("buildit\\builditarenas.yml");
    }

    // Get instance when needed.
    public static BuildItArenaManager getInstance()
    {
        if (buildItArenaManager == null)
        {
            buildItArenaManager = new BuildItArenaManager();
        }

        return buildItArenaManager;
    }

    // When needing to reload arenas.
    public void reloadArenas()
    {
        shutGamesDown();
        arenas.reloadConfig();
        loadArenas();

    }

    // Shut the games down.
    public void shutGamesDown()
    {
        if (activeArenas.isEmpty())
        {
            return;
        }

        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            set.getValue().removeEverybody();
            set.getValue().setStatus(BuildItGameStatus.STOP);
        }
    }

    // Get list of names for the arenas.
    // (Mainly for tab and extra things).
    public List<String> getArenaNames()
    {
        if (activeArenas.isEmpty())
        {
            return null;
        }

        List<String> arenaNames = new ArrayList<>();
        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            arenaNames.add(set.getKey().getName());
        }
        return arenaNames;
    }

    // Find the right game to join.
    public BuildItGameManager findGame(String name)
    {
        name = name.toUpperCase();
        if (activeArenas.isEmpty())
        {
            return null;
        }

        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            // When found, return the game.
            if (set.getKey().getName().equalsIgnoreCase(name))
            {
                return set.getValue();
            }
        }
        return null;
    }

    // Find the player currently in game.
    public BuildItGameManager findPlayerInGame(Player p)
    {
        if (activeArenas.isEmpty())
        {
            return null;
        }

        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            if (set.getValue().getTotalPlayers().contains(p))
            {
                return set.getValue();
            }
        }
        return null;
    }

    // Check if player is in another arena.
    public boolean isInAnotherArena(Player p)
    {
        BuildItGameManager gm = this.findPlayerInGame(p);
        return (gm == null);
    }

    // Find the arena.
    public BuildItArena findArena(String name) {
        name = name.toUpperCase();
        if (activeArenas.isEmpty())
        {
            return null;
        }

        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            if (set.getKey().getName().equalsIgnoreCase(name))
            {
                return set.getKey();
            }
        }
        return null;
    }

    // Disable the arena.
    public boolean disableArena(Player p, BuildItArena arena)
    {
        if (activeArenas.isEmpty())
        {
            return false;
        }

        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            if (set.getKey().equals(arena))
            {
                if (set.getValue().getStatus() == BuildItGameStatus.WAIT)
                {
                    set.getValue().removeEverybody();
                    set.getValue().setStatus(BuildItGameStatus.STOP);
                    MessageManager.getInstance().sendMessage("BuildIt.arena_disabled", p);
                    return true;
                }
            }
        }

        MessageManager.getInstance().sendMessage("BuildIt.arena_already_disabled", p);
        return false;
    }

    // Enable the arena.
    public boolean enableArena(Player p, BuildItArena arena)
    {
        if (activeArenas.isEmpty())
        {
            return false;
        }

        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            if (set.getKey().equals(arena))
            {
                if (set.getValue().getStatus() == BuildItGameStatus.STOP)
                {
                    set.getValue().setStatus(BuildItGameStatus.WAIT);
                    MessageManager.getInstance().sendMessage("BuildIt.arena_enabled", p);
                    return true;
                }
            }
        }
        MessageManager.getInstance().sendMessage("buildit.arena_already_enabled", p);
        return false;
    }

    // Remove arena.
    public boolean deleteArena(String name)
    {
        name = name.toUpperCase();
        if (activeArenas.isEmpty())
        {
            return false;
        }

        for (Map.Entry<BuildItArena, BuildItGameManager> set : activeArenas.entrySet())
        {
            if (set.getKey().getName().equalsIgnoreCase(name))
            {
                arenas.getConfig().set("Arenas." + name, null);
                arenas.saveConfig();
                loadArenas();
                return true;
            }
        }
        return false;
    }

    // Create the arena.
    public void createArena(BuildItArenaSetupTemplate temp)
    {
        arenas.getConfig().set("Arenas." + temp.getName(), temp.getName().toUpperCase());
        arenas.getConfig().set("Arenas." + temp.getName() + ".Minimum", temp.getMinimum());
        arenas.getConfig().set("Arenas." + temp.getName() + ".Maximum", temp.getMaximum());
        arenas.saveConfig();

        saveLocation("Arenas." + temp.getName() + ".Builder_Spawn.", temp.getBuilderSpot());
        saveLocation("Arenas." + temp.getName() + ".Lobby_Spawn.", temp.getLobby());
        saveLocation("Arenas." + temp.getName() + ".Spectate_Spawn.", temp.getSpectate());
        saveLocation("Arenas." + temp.getName() + ".Exit_Spawn.", temp.getExit());

        launchArena(new BuildItArena(temp.getName(), temp.getMinimum(), temp.getMaximum(), temp.getBuilderSpot(), temp.getSpectate(), temp.getLobby(), temp.getExit()));
    }

    // Make arena live.
    public void launchArena(BuildItArena buildItArena)
    {
        BuildItGameManager buildItGameManager = new BuildItGameManager(buildItArena);
        this.activeArenas.put(buildItArena, buildItGameManager);
        buildItGameManager.setStatus(BuildItGameStatus.WAIT);
    }

    // Load arenas from config.
    public void loadArenas()
    {
        activeArenas.clear();
        if (this.arenas.getConfig().getConfigurationSection("Arenas") != null)
        {
            for(String name : arenas.getConfig().getConfigurationSection("Arenas").getKeys(false))
            {
                int minimum = this.arenas.getConfig().getInt("Arenas." + name + ".Minimum");
                int maximum = this.arenas.getConfig().getInt("Arenas." + name + ".Maximum");
                Location builderSpot = this.loadLocation("Arenas." + name + ".Builder_Spawn.");
                Location lobby = this.loadLocation("Arenas." + name + ".Lobby_Spawn.");
                Location spectate = this.loadLocation("Arenas." + name + ".Spectate_Spawn.");
                Location exit = this.loadLocation("Arenas." + name + ".Exit_Spawn.");
                launchArena(new BuildItArena(name, minimum, maximum, builderSpot, spectate, lobby, exit));
            }
        }

    }

    // Save location in this format.
    private void saveLocation(String path, Location location)
    {
        arenas.getConfig().set(path + "world", location.getWorld().getName());
        arenas.getConfig().set(path + "x", location.getX());
        arenas.getConfig().set(path + "y", location.getY());
        arenas.getConfig().set(path + "z", location.getZ());
        arenas.getConfig().set(path + "pitch", location.getPitch());
        arenas.getConfig().set(path + "yaw", location.getYaw());
        arenas.saveConfig();
    }

    // Load location in this format.
    private Location loadLocation(String path)
    {
        String worldName = arenas.getConfig().getString(path + "world");
        double x = arenas.getConfig().getDouble(path + "x");
        double y = arenas.getConfig().getDouble(path + "y");
        double z = arenas.getConfig().getDouble(path + "z");
        float pitch = (float) arenas.getConfig().getDouble(path + "pitch");
        float yaw = (float) arenas.getConfig().getDouble(path + "yaw");
        World world = Bukkit.getWorld(worldName);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
