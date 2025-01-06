package me.gipper1998.minigames.utils;

/***
 * Create specific potions.
 */

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionBuilder
{
    private PotionEffectType effectType;
    private int duration;
    private int amp;

    // Constructor.
    public PotionBuilder(String effect, int duration, int amp)
    {
        wordEffectType(effect);
        this.duration = duration;
        this.amp = amp;
    }

    // Add player with potion effects.
    public void addPlayer(Player p)
    {
        p.addPotionEffect(new PotionEffect(effectType, duration, amp, false, false));
    }

    // Set the effect type.
    private void wordEffectType(String effect)
    {
        if (effect.equalsIgnoreCase("jump"))
        {
            effectType = PotionEffectType.JUMP_BOOST;
        }
        else if (effect.equalsIgnoreCase("slow"))
        {
            effectType = PotionEffectType.SLOWNESS;
        }
        else if (effect.equalsIgnoreCase("fast"))
        {
            effectType = PotionEffectType.SPEED;
        }
        else if (effect.equalsIgnoreCase("invis"))
        {
            effectType = PotionEffectType.INVISIBILITY;
        }
        else
        {
            effectType = null;
        }
    }
}
