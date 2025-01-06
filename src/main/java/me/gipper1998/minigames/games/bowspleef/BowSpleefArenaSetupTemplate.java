package me.gipper1998.minigames.games.bowspleef;

/***
 * Setup template to make things easier.
 *
 * Similar with BowSpleef Arena but an extra
 * constructor.
 */

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

public class BowSpleefArenaSetupTemplate
{

    @Setter @Getter
    private String name;

    @Setter @Getter
    private Location arena = null;

    @Setter @Getter
    private Location lobby = null;

    @Setter @Getter
    private Location spectate = null;

    @Setter @Getter
    private Location exit = null;

    @Setter @Getter
    private int minimum = 0;

    @Setter @Getter
    private int maximum = 0;

    public BowSpleefArenaSetupTemplate(String name)
    {
        this.name = name;
    }

    public BowSpleefArenaSetupTemplate(BowSpleefArena arena)
    {
        this.name = arena.getName();
        this.arena = arena.getArena();
        this.lobby = arena.getLobby();
        this.spectate = arena.getSpectate();
        this.exit = arena.getExit();
        this.minimum = arena.getMin();
        this.maximum = arena.getMax();
    }
}
