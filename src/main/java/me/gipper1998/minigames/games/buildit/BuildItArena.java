package me.gipper1998.minigames.games.buildit;

/***
 * BuildIt Arena Class to define the arena, having the following attributes:
 * - Name
 * - Builder Location
 * - Lobby Location
 * - Spectator Location
 * - Exit Location
 * - Minimum Integer
 * - Maximum Integer
 *
 * Uses setter and getter function using lombok.
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

@AllArgsConstructor
public class BuildItArena
{
    @Getter
    private String name;

    @Getter
    private int minimum;

    @Getter
    private int maximum;

    @Getter
    private Location builderSpot;

    @Getter
    private Location spectators;

    @Getter
    private Location lobby;

    @Getter
    private Location exit;
}
