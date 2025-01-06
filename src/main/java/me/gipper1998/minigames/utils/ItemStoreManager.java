package me.gipper1998.minigames.utils;

/***
 * Store the players inventory.
 */

import java.util.Iterator;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

public class ItemStoreManager
{
    private ItemStack[] inventory;
    private ItemStack[] armor;
    private ItemStack offHandSlot;
    private Player p;
    private double health;
    private double hunger;
    private float xp;

    // Used just in case for later.
    private Location prevLocation;

    private int levels;
    private int selectedItemSlot;
    private GameMode gamemode;

    // Constructor to store everything.
    public ItemStoreManager(Player player)
    {
        this.p = player;
        this.inventory = p.getInventory().getContents();
        this.armor = p.getInventory().getArmorContents();
        this.health = p.getHealth();
        this.hunger = p.getFoodLevel();
        this.xp = this.p.getExp();
        this.gamemode = p.getGameMode();
        this.selectedItemSlot = player.getInventory().getHeldItemSlot();
        this.levels = p.getLevel();
        this.offHandSlot = p.getInventory().getItemInOffHand();
        this.prevLocation = p.getLocation();

        // Leave button complication, just moving one slot helped.
        if (this.selectedItemSlot == 8)
        {
            this.selectedItemSlot = 7;
        }

        p.getInventory().clear();
        p.getInventory().setArmorContents((ItemStack[])null);
        p.getInventory().setHeldItemSlot(0);
        p.updateInventory();
        p.setLevel(0);
        p.setExp(0.0F);

        for (PotionEffect effect : p.getActivePotionEffects())
        {
            p.removePotionEffect(effect.getType());
        }
    }

    // When done, give everything back.
    public void giveBackItems()
    {
        p.getInventory().setContents(inventory);
        p.getInventory().setArmorContents(armor);
        p.getInventory().setItemInOffHand(offHandSlot);
        p.setGameMode(gamemode);
        p.setExp(xp);
        p.setHealth(health);
        p.getInventory().setHeldItemSlot(selectedItemSlot);
        p.setFoodLevel((int) hunger);
        p.updateInventory();
        p.setLevel(levels);
        p.setFireTicks(0);
    }
}
