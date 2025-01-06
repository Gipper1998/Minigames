package me.gipper1998.minigames.games.buildit;

import java.util.*;

import me.gipper1998.minigames.files.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class BuildItScoreboardManager
{
    private Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
    private Objective objective;
    private HashMap<Player, Integer> buildItScores = new HashMap();
    private BuildItGameManager gm;

    // Constructor
    public BuildItScoreboardManager(String title, BuildItGameManager gm)
    {
        this.objective = this.scoreboard.registerNewObjective(title, Criteria.DUMMY, title);
        this.objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        this.gm = gm;
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
            scoreboard.resetScores(line);
        }
    }

    // Specific set lines.
    public void setLines(List<String> messages, int round)
    {
        clearScoreboard();

        Set<String> lines = new HashSet<>(messages);

        

        for (String line : lines)
        {

        }

    }

    // Add player to scoreboard.
    public void addPlayer(Player p)
    {
        p.setScoreboard(scoreboard);
        buildItScores.put(p, 1);
    }

    // Remove player from scoreboard.
    public void removePlayer(Player p)
    {
        p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        buildItScores.remove(p);
    }

    // Add point.
    public void addPoint(Player p, int points)
    {
        buildItScores.put(p, buildItScores.get(p) + points);
    }

    // Remove point if needed.
    public void removePoint(Player p, int points) {
        buildItScores.put(p, buildItScores.get(p) - points);
    }

    // Clear all scores.
    public void clearScores()
    {
        buildItScores.clear();
    }

    // Sort HashMap for later use.
    private HashMap<Player, Integer> sort(HashMap<Player, Integer> hm)
    {
        List<Map.Entry<Player, Integer> > list = new LinkedList<Map.Entry<Player, Integer> >(hm.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<Player, Integer> >()
        {
            public int compare(Map.Entry<Player, Integer> o1, Map.Entry<Player, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
        HashMap<Player, Integer> temp = new LinkedHashMap<Player, Integer>();
        for (Map.Entry<Player, Integer> entry : list)
        {
            temp.put(entry.getKey(), entry.getValue());
        }
        return temp;
    }

}
