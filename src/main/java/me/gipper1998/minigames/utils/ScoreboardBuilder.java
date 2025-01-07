package me.gipper1998.minigames.utils;

/***
 * General scoreboard builder.
 * Build It uses a different one.
 */

import java.util.*;

import me.gipper1998.minigames.files.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardBuilder
{
    private Scoreboard scoreboard;
    private Objective objective;

    // Main constructor.
    public ScoreboardBuilder(String title)
    {
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.objective = scoreboard.registerNewObjective(title, Criteria.DUMMY, title);
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    // Set the title.
    public void setTitle(String message)
    {
        message = MessageManager.getInstance().translateColor(message);
        objective.setDisplayName(message);
    }

    // Clear the scoreboard.
    public void clearScoreboard()
    {

        // Get the set of strings and reset.
        Set<String> entries = scoreboard.getEntries();

        for (String line : entries)
        {
            objective.getScore(line).resetScore();
        }

    }

    // Set lines for scoreboard.
    public void setLines(List<String> messages)
    {
        clearScoreboard();
        int index = messages.size();

        for(String line : messages)
        {
            objective.getScore(line).setScore(index);
            index--;
        }
    }

    // Add player to scoreboard.
    public void addPlayer(Player p)
    {
        p.setScoreboard(scoreboard);
    }

    // Remove player from scoreboard.
    public void removePlayer(Player p)
    {
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
