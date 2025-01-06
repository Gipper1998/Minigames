package me.gipper1998.minigames.games.buildit;

/***
 * Setup template to make things easier.
 *
 * Similar with BowSpleef Arena but an extra
 * constructor.
 */

import lombok.Getter;
import lombok.Setter;
import me.gipper1998.minigames.games.bowspleef.BowSpleefArena;
import org.bukkit.Location;

public class BuildItArenaSetupTemplate
{

    @Setter @Getter
    private String name;

    @Setter @Getter
    private Location builderSpot = null;

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

    public BuildItArenaSetupTemplate(String name)
    {
        this.name = name;
    }

    public BuildItArenaSetupTemplate(BuildItArena arena)
    {
        this.name = arena.getName();
        this.builderSpot = arena.getBuilderSpot();
        this.lobby = arena.getLobby();
        this.spectate = arena.getSpectators();
        this.exit = arena.getExit();
        this.minimum = arena.getMinimum();
        this.maximum = arena.getMaximum();
    }
}
