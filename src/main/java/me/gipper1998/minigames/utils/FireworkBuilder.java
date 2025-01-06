package me.gipper1998.minigames.utils;

/***
 * Create fireworks.
 */

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkBuilder
{

    // Constructor for the fireworks.
    public FireworkBuilder(Location location, int power, String colorName, int effect, int type)
    {
        location.add(0.0, 1.0, 0.0);
        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        Color color = this.color(colorName);
        fwm.setPower(power);
        FireworkEffect.Type ft = getType(type);
        if (ft != null)
        {
            fwm = this.getEffect(fwm, effect, color, ft);
        }
        else
        {
            fwm = this.getEffect(fwm, effect, color);
        }

        fw.setFireworkMeta(fwm);
    }

    // Get type based on switch case.
    private FireworkEffect.Type getType(int num)
    {
        FireworkEffect.Type ft;
        switch (num)
        {
            case 1 -> ft = Type.BALL;
            case 2 -> ft = Type.BALL_LARGE;
            case 3 -> ft = Type.BURST;
            case 4 -> ft = Type.CREEPER;
            case 5 -> ft = Type.STAR;
            default -> ft = null;
        }

        return ft;
    }

    // Get effect based on switch case.
    private FireworkMeta getEffect(FireworkMeta fwm, int num, Color color)
    {
        switch (num)
        {
            case 1 -> fwm.addEffect(FireworkEffect.builder().withColor(color).flicker(true).build());
            case 2 -> fwm.addEffect(FireworkEffect.builder().withColor(color).trail(true).build());
            case 3 -> fwm.addEffect(FireworkEffect.builder().withColor(color).trail(true).flicker(true).build());
            default -> fwm.addEffect(FireworkEffect.builder().withColor(color).build());
        }

        return fwm;
    }

    // Get effect with the effect type.
    private FireworkMeta getEffect(FireworkMeta fwm, int num, Color color, FireworkEffect.Type ft)
    {
        switch (num)
        {
            case 1 -> fwm.addEffect(FireworkEffect.builder().withColor(color).flicker(true).with(ft).build());
            case 2 -> fwm.addEffect(FireworkEffect.builder().withColor(color).trail(true).with(ft).build());
            case 3 -> fwm.addEffect(FireworkEffect.builder().withColor(color).trail(true).flicker(true).with(ft).build());
            default -> fwm.addEffect(FireworkEffect.builder().withColor(color).with(ft).build());
        }

        return fwm;
    }

    // Get the color of firework with a lot of if else statements.
    private Color color(String color)
    {
        if (color.equalsIgnoreCase("aqua")) {
            return Color.AQUA;
        } else if (color.equalsIgnoreCase("black")) {
            return Color.BLACK;
        } else if (color.equalsIgnoreCase("blue")) {
            return Color.BLUE;
        } else if (color.equalsIgnoreCase("fuchsia")) {
            return Color.FUCHSIA;
        } else if (color.equalsIgnoreCase("gray")) {
            return Color.GRAY;
        } else if (color.equalsIgnoreCase("green")) {
            return Color.GREEN;
        } else if (color.equalsIgnoreCase("lime")) {
            return Color.LIME;
        } else if (color.equalsIgnoreCase("maroon")) {
            return Color.MAROON;
        } else if (color.equalsIgnoreCase("navy")) {
            return Color.NAVY;
        } else if (color.equalsIgnoreCase("olive")) {
            return Color.OLIVE;
        } else if (color.equalsIgnoreCase("orange")) {
            return Color.ORANGE;
        } else if (color.equalsIgnoreCase("purple")) {
            return Color.PURPLE;
        } else if (color.equalsIgnoreCase("red")) {
            return Color.RED;
        } else if (color.equalsIgnoreCase("silver")) {
            return Color.SILVER;
        } else if (color.equalsIgnoreCase("teal")) {
            return Color.TEAL;
        } else if (color.equalsIgnoreCase("white")) {
            return Color.WHITE;
        } else {
            return color.equalsIgnoreCase("yellow") ? Color.YELLOW : null;
        }
    }
}
