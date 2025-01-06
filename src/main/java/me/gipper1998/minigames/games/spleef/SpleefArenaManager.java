package me.gipper1998.minigames.games.spleef;

/***
 * Arena Manager to help make things run smoothly.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.utils.FileBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SpleefArenaManager
{

    // Static variable for later use.
    private static SpleefArenaManager spleefArenaManager;

    // Map of all arenas with the game manager respectively.
    private HashMap<SpleefArena, SpleefGameManager> activeArenas;

    // File for arenas.
    private FileBuilder arenas;

    // Constructor for it.
    public SpleefArenaManager()
    {
        activeArenas = new HashMap();
        arenas = new FileBuilder("spleef\\spleefarenas.yml");
    }

    // Get instance when needed.
    public static SpleefArenaManager getInstance()
    {
        if (spleefArenaManager == null)
        {
            spleefArenaManager = new SpleefArenaManager();
        }

        return spleefArenaManager;
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

        for (Map.Entry<SpleefArena, SpleefGameManager> set : activeArenas.entrySet())
        {
            set.getValue().removeEverybody();
            set.getValue().setStatus(SpleefGameStatus.STOP);
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
        for (Map.Entry<SpleefArena, SpleefGameManager> set : activeArenas.entrySet())
        {
            arenaNames.add(set.getKey().getName());
        }
        return arenaNames;
    }

    // Find the right game to join.
    public SpleefGameManager findGame(String name)
    {
        name = name.toUpperCase();
        if (activeArenas.isEmpty())
        {
            return null;
        }

        for (Map.Entry<SpleefArena, SpleefGameManager> set : activeArenas.entrySet())
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
    public SpleefGameManager findPlayerInGame(Player p)
    {
        if (activeArenas.isEmpty())
        {
            return null;
        }

        for (Map.Entry<SpleefArena, SpleefGameManager> set : activeArenas.entrySet())
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
        SpleefGameManager gm = this.findPlayerInGame(p);
        return (gm == null);
    }

    // Find the arena.
    public SpleefArena findArena(String name) {
        name = name.toUpperCase();
        if (activeArenas.isEmpty())
        {
            return null;
        }

        for (Map.Entry<SpleefArena, SpleefGameManager> set : activeArenas.entrySet())
        {
            if (set.getKey().getName().equalsIgnoreCase(name))
            {
                return set.getKey();
            }
        }
        return null;
    }

    // Disable the arena.
    public boolean disableArena(Player p, SpleefArena arena)
    {
        if (activeArenas.isEmpty())
        {
            return false;
        }

        for (Map.Entry<SpleefArena, SpleefGameManager> set : activeArenas.entrySet())
        {
            if (set.getKey().equals(arena))
            {
                if (set.getValue().getStatus() == SpleefGameStatus.WAIT)
                {
                    set.getValue().removeEverybody();
                    set.getValue().setStatus(SpleefGameStatus.STOP);
                    MessageManager.getInstance().sendMessage("spleef.arena_disabled", p);
                    return true;
                }
            }
        }

        MessageManager.getInstance().sendMessage("spleef.arena_already_disabled", p);
        return false;
    }

    // Enable the arena.
    public boolean enableArena(Player p, SpleefArena arena)
    {
        if (activeArenas.isEmpty())
        {
            return false;
        }

        for (Map.Entry<SpleefArena,SpleefGameManager> set : activeArenas.entrySet())
        {
            if (set.getKey().equals(arena))
            {
                if (set.getValue().getStatus() == SpleefGameStatus.STOP)
                {
                    set.getValue().setStatus(SpleefGameStatus.WAIT);
                    MessageManager.getInstance().sendMessage("spleef.arena_enabled", p);
                    return true;
                }
            }
        }
        MessageManager.getInstance().sendMessage("spleef.arena_already_enabled", p);
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

        for (Map.Entry<SpleefArena, SpleefGameManager> set : activeArenas.entrySet())
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
    public void createArena(SpleefArenaSetupTemplate temp)
    {
        arenas.getConfig().set("Arenas." + temp.getName(), temp.getName().toUpperCase());
        arenas.getConfig().set("Arenas." + temp.getName() + ".Minimum", temp.getMinimum());
        arenas.getConfig().set("Arenas." + temp.getName() + ".Maximum", temp.getMaximum());

        this.arenas.saveConfig();

        saveLocation("Arenas." + temp.getName() + ".Arena_Spawn.", temp.getArena());
        saveLocation("Arenas." + temp.getName() + ".Lobby_Spawn.", temp.getLobby());
        saveLocation("Arenas." + temp.getName() + ".Spectate_Spawn.", temp.getSpectate());
        saveLocation("Arenas." + temp.getName() + ".Exit_Spawn.", temp.getExit());

        launchArena(new SpleefArena(temp.getName(), temp.getArena(), temp.getLobby(), temp.getSpectate(), temp.getExit(), temp.getMinimum(), temp.getMaximum()));
    }

    // Make arena live.
    public void launchArena(SpleefArena spleefArena)
    {
        SpleefGameManager spleefGameManager = new SpleefGameManager(spleefArena);
        activeArenas.put(spleefArena, spleefGameManager);
        spleefGameManager.setStatus(SpleefGameStatus.WAIT);
    }

    // Load arenas from config.
    public void loadArenas()
    {
        activeArenas.clear();
        if (this.arenas.getConfig().getConfigurationSection("Arenas") != null)
        {
            for(String name : arenas.getConfig().getConfigurationSection("Arenas").getKeys(false))
            {
                int minimum = arenas.getConfig().getInt("Arenas." + name + ".Minimum");
                int maximum = this.arenas.getConfig().getInt("Arenas." + name + ".Maximum");
                Location arena = this.loadLocation("Arenas." + name + ".Arena_Spawn.");
                Location lobby = this.loadLocation("Arenas." + name + ".Lobby_Spawn.");
                Location spectate = this.loadLocation("Arenas." + name + ".Spectate_Spawn.");
                Location exit = this.loadLocation("Arenas." + name + ".Exit_Spawn.");
                launchArena(new SpleefArena(name, arena, lobby, spectate, exit, minimum, maximum));
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
