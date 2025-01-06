package me.gipper1998.minigames.games.bowspleef;

/***
 * BowSpleef Arena Class to define the arena, having the following attributes:
 * - Name
 * - Arena location (using coordinates)
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
import lombok.Setter;
import org.bukkit.Location;

@AllArgsConstructor
public class BowSpleefArena
{

    @Setter @Getter
    private String name;

    @Setter @Getter
    private Location arena;

    @Setter @Getter
    private Location lobby;

    @Setter @Getter
    private Location spectate;

    @Setter @Getter
    private Location exit;

    @Setter @Getter
    private int min;

    @Setter @Getter
    private int max;

}
