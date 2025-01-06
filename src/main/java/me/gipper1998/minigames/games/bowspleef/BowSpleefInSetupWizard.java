package me.gipper1998.minigames.games.bowspleef;

/***
 * Player setup wizard to make the game.
 */

import lombok.Getter;
import me.gipper1998.minigames.Minigames;
import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.utils.ItemStoreManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BowSpleefInSetupWizard implements Listener
{
    private static BowSpleefInSetupWizard isw;
    private HashMap<BowSpleefSetupWizard, ItemStoreManager> inWizard;

    @Getter
    private List<Player> playersInWizard;

    // Constructor.
    public BowSpleefInSetupWizard()
    {
        this.inWizard = new HashMap();
        this.playersInWizard = new ArrayList();
    }

    // Get instance when needed.
    public static BowSpleefInSetupWizard getInstance()
    {
        if (isw == null)
        {
            isw = new BowSpleefInSetupWizard();
        }
        return isw;
    }

    // Add player to wizard, store items, and add to maps.
    public boolean addPlayer(Player p, String name)
    {
        if (!this.playersInWizard.contains(p))
        {
            Minigames.getInstance().getServer().getPluginManager().registerEvents(this, Minigames.getInstance());
            ItemStoreManager ism = new ItemStoreManager(p);
            BowSpleefSetupWizard sm = new BowSpleefSetupWizard(p, name);
            inWizard.put(sm, ism);
            playersInWizard.add(p);
            return true;
        }
        else
        {
            return false;
        }
    }

    // Same as above, but for editing.
    public boolean addEditPlayer(Player p, BowSpleefArena arena)
    {
        if (!playersInWizard.contains(p))
        {
            Minigames.getInstance().getServer().getPluginManager().registerEvents(this, Minigames.getInstance());
            ItemStoreManager ism = new ItemStoreManager(p);
            BowSpleefSetupWizard sm = new BowSpleefSetupWizard(p, arena);
            inWizard.put(sm, ism);
            playersInWizard.add(p);
            return true;
        }
        else
        {
            return false;
        }
    }

    // Remove player.
    public void removePlayer(Player p) {
        if (playersInWizard.contains(p))
        {
            for (Map.Entry<BowSpleefSetupWizard, ItemStoreManager> entry : inWizard.entrySet())
            {
                BowSpleefSetupWizard sm = entry.getKey();
                if (inWizard.containsKey(sm))
                {
                    ItemStoreManager ism = this.inWizard.get(sm);
                    ism.giveBackItems();
                    inWizard.remove(sm);
                    playersInWizard.remove(p);
                    return;
                }
            }
        }

    }

    // Remove everyone from wizard.
    public void removeEverybody() {
        for (Player p : playersInWizard)
        {
            removePlayer(p);
        }
    }

    // Player chat events.
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (playersInWizard.contains(event.getPlayer()))
        {
            for (Map.Entry<BowSpleefSetupWizard, ItemStoreManager> entry : inWizard.entrySet())
            {
                {
                    if (entry.getKey().getP().equals(event.getPlayer()))
                    {
                        event.setCancelled(true);
                        if (!(entry.getKey()).fromChat(event.getMessage()))
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.wizard_arena_no_setup_exist", event.getPlayer());
                        }
                    }
                }
            }
        }

    }

    // Prevent block break.
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (this.playersInWizard.contains(event.getPlayer()))
        {
            event.setCancelled(true);
        }

    }

    // Prevent block place.
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (this.playersInWizard.contains(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    // Prevent item pickup.
    @EventHandler
    public void onPickUp(EntityPickupItemEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player player = (Player)event.getEntity();
            if (this.playersInWizard.contains(player))
            {
                event.setCancelled(true);
            }
        }

    }

    // Prevent item drop.
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if (this.playersInWizard.contains(event.getPlayer()))
        {
            event.setCancelled(true);
        }

    }

    // Event for when player quits.
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (playersInWizard.contains(event.getPlayer()))
        {
            for (Map.Entry<BowSpleefSetupWizard, ItemStoreManager> entry : inWizard.entrySet())
            {
                if (entry.getKey().getP().equals(event.getPlayer()))
                {
                    entry.getKey().exitWizard(false);
                }
            }
        }
    }
}
