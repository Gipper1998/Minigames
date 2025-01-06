package me.gipper1998.minigames.softdepends;

/***
 * PAPI Manager file for using holograms to display information.
 */

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.gipper1998.minigames.Minigames;
import me.gipper1998.minigames.utils.DatabaseManager;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PAPIManager extends PlaceholderExpansion
{

    private static PAPIManager pm;

    // Standard PAPI calls.
    public @NotNull String getIdentifier()
    {
        return "minigames";
    }

    public @NotNull String getAuthor()
    {
        return "gipper1998";
    }

    public @NotNull String getVersion()
    {
        return Minigames.getInstance().getDescription().getVersion();
    }

    // Instance.
    public static PAPIManager getInstance()
    {
        if (pm == null)
        {
            pm = new PAPIManager();
        }
        return pm;
    }

    // Mostly leaderboard for holograms.
    public String onRequest(OfflinePlayer p, String identifier)
    {
        if (p == null)
        {
            return "";
        }
        else
        {
            if (identifier.contains("top"))
            {
                String[] data = identifier.split("_");
                String game = data[1];
                String type = data[2];
                String dataType = data[3];
                String pos = data[4];
                String score;
                if (dataType.equalsIgnoreCase("player"))
                {
                    // Basically if score is found, return it.
                    score = DatabaseManager.getInstance().getNamePos(game, type, Integer.parseInt(pos));
                    if (score != null)
                    {
                        return score;
                    }

                    return "";
                }

                if (dataType.equalsIgnoreCase("score"))
                {
                    score = DatabaseManager.getInstance().getScorePos(game, type, Integer.parseInt(pos));
                    if (score != null)
                    {
                        return score;
                    }

                    return "";
                }
            }

            return null;
        }
    }
}
