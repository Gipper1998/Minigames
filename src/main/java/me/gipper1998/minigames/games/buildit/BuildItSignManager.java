package me.gipper1998.minigames.games.buildit;

/***
 * Create the join and leave signs.
 */

import me.gipper1998.minigames.Minigames;
import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.utils.FileBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BuildItSignManager implements Listener
{
    private static BuildItSignManager signManager;
    private FileBuilder signs;
    private int taskID = 0;
    private HashMap<String, Long> signCooldown = new HashMap();

    // Constructor.
    public BuildItSignManager()
    {
        Minigames.getInstance().getServer().getPluginManager().registerEvents(this, Minigames.getInstance());
        this.signs = new FileBuilder("buildit\\builditsigns.yml");
    }

    // Grab the instance.
    public static BuildItSignManager getInstance()
    {
        if (signManager == null)
        {
            signManager = new BuildItSignManager();
        }

        return signManager;
    }

    // Start the updater for the signs.
    public void startUpdater()
    {
        if (this.taskID != 0)
        {
            Bukkit.getScheduler().cancelTask(this.taskID);
            this.taskID = 0;
            this.startUpdater();
        }
        else
        {
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(Minigames.getInstance(), new Runnable()
            {
                @Override
                public void run()
                {
                    updateSigns();
                }
            }, 0L, 20L);
        }

    }

    // Update signs.
    public void updateSigns()
    {
        if (this.signs.getConfig().contains("Signs"))
        {
            // Iterate through each arena.
            for (String arenaName : signs.getConfig().getConfigurationSection("Signs").getKeys(false))
            {
                BuildItArena arena = BuildItArenaManager.getInstance().findArena(arenaName);
                BuildItGameManager game = BuildItArenaManager.getInstance().findGame(arenaName);

                if (arena != null)
                {
                    // Get the list of signs.
                    List<String> signLists = new ArrayList();
                    if (this.signs.getConfig().contains("Signs." + arenaName))
                    {
                        signLists = this.signs.getConfig().getStringList("Signs." + arenaName);
                    }

                    // Update each one of them.
                    for (int i = 0; i < signLists.size(); i++)
                    {
                        String[] location = signLists.get(i).split(";");
                        String type = location[0];
                        int x = Integer.valueOf(location[1]);
                        int y = Integer.valueOf(location[2]);
                        int z = Integer.valueOf(location[3]);
                        World world = Bukkit.getWorld(location[4]);

                        // Check if the chunk is loaded.
                        if ((world != null) && (world.isChunkLoaded(x >> 4, z >> 4)))
                        {
                            Block block = world.getBlockAt(x, y, z);
                            if (block.getType().name().contains("SIGN"))
                            {
                                Sign sign = (Sign) block.getState();

                                // Once found, update the actual sign.
                                updateSignType(sign, type, arena, game);
                            }
                        }
                    }
                }
            }
        }
    }

    // Getting the sign status.
    private String getSignStatus(BuildItGameManager gameManager)
    {
        if ((gameManager.getStatus() != BuildItGameStatus.GAME_BUILD_WORD) && (gameManager.getStatus() != BuildItGameStatus.GAME_PICK_WORD) && (gameManager.getStatus() != BuildItGameStatus.GAME_DISPLAY_WORD))
        {
            if (gameManager.getStatus() == BuildItGameStatus.WINNER)
            {
                return MessageManager.getInstance().getString("buildit.sign_status.reset");
            }
            else if (gameManager.getStatus() == BuildItGameStatus.STOP)
            {
                return MessageManager.getInstance().getString("buildit.sign_status.disabled");
            }
            else
            {
                return MessageManager.getInstance().getString("buildit.sign_status.wait");
            }
        }
        else
        {
            return MessageManager.getInstance().getString("buildit.sign_status.in-game");
        }
    }

    // Update the sign type.
    private void updateSignType(Sign sign, String type, BuildItArena arena, BuildItGameManager game)
    {
        List<String> signListMessages = new ArrayList();
        String status = getSignStatus(game);

        if (type.equalsIgnoreCase("join"))
        {
            signListMessages = MessageManager.getInstance().getSignStringList("buildit.sign_join");
        }

        if (type.equalsIgnoreCase("leave"))
        {
            signListMessages = MessageManager.getInstance().getSignStringList("buildit.sign_leave");
        }

        // Once the message is created, place it on the sign.
        for(int i = 0; i < signListMessages.size(); i++)
        {
            String currentLine = signListMessages.get(i);
            currentLine = currentLine.replaceAll("<arenaname>", arena.getName());
            currentLine = currentLine.replaceAll("<status>", status);
            currentLine = currentLine.replaceAll("<in_game>", Integer.toString(game.getTotalPlayers().size()));
            currentLine = currentLine.replaceAll("<maximum>", Integer.toString(arena.getMaximum()));
            sign.setLine(i, currentLine);
            sign.update();
        }

    }

    // Sign placement when creating the sign.
    @EventHandler
    public void onSignPlacement(SignChangeEvent event)
    {
        // Check if they have perms before even continuing.
        if (event.getPlayer().hasPermission("minigames.admin"))
        {
            if (event.getLine(0).equals("[BuildIt]"))
            {
                if (event.getLine(1) != null && BuildItArenaManager.getInstance().findArena(event.getLine(1)) != null)
                {
                    // After all the checks, place it in file.
                    if (event.getLine(3).equalsIgnoreCase("[leave]") || event.getLine(3).equalsIgnoreCase("[join]") || event.getLine(3).equalsIgnoreCase("[cheat]"))
                    {
                        String type = event.getLine(3).toUpperCase();
                        type = type.replaceAll("[\\[\\](){}]","");
                        String key = event.getLine(1).toUpperCase();
                        BuildItGameManager gm = BuildItArenaManager.getInstance().findGame(event.getLine(1));
                        List<String> listedSigns = new ArrayList<>();
                        if (signs.getConfig().contains("Signs." + key))
                        {
                            listedSigns = signs.getConfig().getStringList("Signs." + gm.getArena().getName());
                        }
                        listedSigns.add(type + ";" + event.getBlock().getX() + ";" + event.getBlock().getY() + ";" + event.getBlock().getZ() + ";" + event.getBlock().getWorld().getName());
                        signs.getConfig().set("Signs." + key, listedSigns);
                        MessageManager.getInstance().sendMessage("buildit.sign_creation", event.getPlayer());
                        for (int i = 0; i < 4; i++)
                        {
                            event.setLine(i, "");
                        }
                        signs.saveConfig();
                    }
                }
            }
        }
    }

    // Deleting the sign.
    @EventHandler
    public void onSignDelete(BlockBreakEvent event)
    {
        Block block = event.getBlock();

        // Once the block was checked and if they have admin perms, continue.
        if (event.getPlayer().hasPermission("buildit.admin"))
        {
            if (block.getType().name().contains("SIGN"))
            {
                if (signs.getConfig().contains("Signs"))
                {
                    for (String arenaName : signs.getConfig().getConfigurationSection("Signs").getKeys(false))
                    {
                        // Grab the list of signs check if it's a match.
                        List<String> listedSigns = new ArrayList<>();
                        if (signs.getConfig().contains("Signs." + arenaName))
                        {
                            listedSigns = signs.getConfig().getStringList("Signs." + arenaName);
                        }
                        for (int i = 0; i < listedSigns.size(); i++)
                        {
                            String[] location = listedSigns.get(i).split(";");
                            int x = Integer.valueOf(location[1]);
                            int y = Integer.valueOf(location[2]);
                            int z = Integer.valueOf(location[3]);
                            World world = Bukkit.getWorld(location[4]);
                            if (world != null)
                            {
                                // Checking if it's a match, if so, delete.
                                if (block.getX() == x && block.getY() == y && block.getZ() == z && world.getName().equals(block.getWorld().getName()))
                                {
                                    if (event.getPlayer().isSneaking())
                                    {
                                        listedSigns.remove(i);
                                        signs.getConfig().set("Signs." + arenaName, listedSigns);
                                        signs.saveConfig();
                                        MessageManager.getInstance().sendMessage("buildit.sign_deletion", event.getPlayer());
                                        return;
                                    }
                                    else
                                    {
                                        event.setCancelled(true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // When the player clicks the sign.
    @EventHandler
    public void onPlayerClick(PlayerInteractEvent event)
    {
        Player p = event.getPlayer();
        Block block = event.getClickedBlock();
        if ((block != null) && (block.getType().name().contains("SIGN")) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK))
        {
            if (signs.getConfig().contains("Signs"))
            {
                for (String arenaName : signs.getConfig().getConfigurationSection("Signs").getKeys(false))
                {
                    if (signs.getConfig().contains("Signs." + arenaName))
                    {
                        BuildItArena arena = BuildItArenaManager.getInstance().findArena(arenaName);
                        if (arena != null)
                        {
                            if (signCooldown.containsKey(p.getUniqueId().toString()) && signCooldown.get(p.getUniqueId().toString()) > System.currentTimeMillis())
                            {
                                return;
                            }
                            signCooldown.put(p.getUniqueId().toString(), System.currentTimeMillis() + 3000L);

                            BuildItGameManager gm = BuildItArenaManager.getInstance().findGame(arenaName);
                            List<String> listedSigns = new ArrayList<>();
                            if (signs.getConfig().contains("Signs." + arenaName))
                            {
                                listedSigns = signs.getConfig().getStringList("Signs." + arenaName);
                            }
                            for (int i = 0; i < listedSigns.size(); i++)
                            {
                                String[] location = listedSigns.get(i).split(";");
                                String type = location[0];
                                int x = Integer.valueOf(location[1]);
                                int y = Integer.valueOf(location[2]);
                                int z = Integer.valueOf(location[3]);
                                World world = Bukkit.getWorld(location[4]);
                                if (world != null)
                                {
                                    if ((block.getX() == x) && (block.getY() == y) && (block.getZ() == z) && world.getName().equals(block.getWorld().getName()))
                                    {
                                        if (type.equalsIgnoreCase("join"))
                                        {
                                            if (!gm.getTotalPlayers().contains(p))
                                            {
                                                gm.addPlayer(p);
                                            }
                                        }
                                        else if (type.equalsIgnoreCase("leave"))
                                        {
                                            if (gm.getTotalPlayers().contains(p))
                                            {
                                                gm.removePlayer(p);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
