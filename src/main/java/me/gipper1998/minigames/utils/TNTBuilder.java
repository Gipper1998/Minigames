package me.gipper1998.minigames.utils;

/***
 * Create TNT on the spot.
 */

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;

public class TNTBuilder
{
    private static TNTBuilder tnt;
    private int delayFuse;
    private int yAbove;

    // Constructor
    public TNTBuilder()
    {
        delayFuse = 3;
        yAbove = 5;
    }

    // Create instance.
    public static TNTBuilder getInstance()
    {
        if (tnt == null)
        {
            tnt = new TNTBuilder();
        }
        return tnt;
    }

    // Create the tnt above specific location.
    public void create(Location location, String name)
    {
        TNTPrimed tntPrimed = location.getWorld().spawn(location.add(0.0, yAbove, 0.0), TNTPrimed.class);
        tntPrimed.setCustomName(name);
        tntPrimed.setFuseTicks(this.delayFuse * 20);
    }

    // Create on specific location, no adjustment.
    public void createOnLocation(Location location, String name)
    {
        TNTPrimed tntPrimed = location.getWorld().spawn(location, TNTPrimed.class);
        tntPrimed.setCustomName(name);
        tntPrimed.setFuseTicks(60);
    }
}
