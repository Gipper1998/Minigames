package me.gipper1998.minigames.utils;

/***
 * Creating an item for the player.
 */

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBuilder
{
    private ItemStack is;
    private ItemMeta im;

    // Constructor for the item.
    public ItemBuilder(Material material, String name) {
        this.is = new ItemStack(material);
        this.im = is.getItemMeta();

        // If the item were to be a bow for BowSpleef.
        if (material.equals(Material.BOW))
        {
            im.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
            im.addEnchant(Enchantment.INFINITY, 2, true);
            im.addEnchant(Enchantment.FLAME, 1, true);
        }

        // Setting the name, unbreakable, and meta.
        im.setDisplayName(name);
        im.setUnbreakable(true);
        is.setItemMeta(im);
    }

    // Another constructor, but with an amount.
    public ItemBuilder(Material material, String name, int num)
    {
        this.is = new ItemStack(material, num);
        this.im = is.getItemMeta();

        if (material.equals(Material.BOW))
        {
            im.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
            im.addEnchant(Enchantment.INFINITY, 2, true);
            im.addEnchant(Enchantment.FLAME, 1, true);
        }

        im.setDisplayName(name);
        is.setItemMeta(im);
    }

    // Getter methods just in case.
    public ItemStack getIs()
    {
        return this.is;
    }

    public ItemMeta getIm()
    {
        return this.im;
    }
}
