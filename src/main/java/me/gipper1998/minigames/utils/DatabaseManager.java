package me.gipper1998.minigames.utils;

/***
 * Database part, using sqlite for now.
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.gipper1998.minigames.Minigames;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class DatabaseManager {
    private static DatabaseManager db;
    private Connection conn;

    private List<String> games;
    private List<String> types;

    String path;

    // Constructor.
    public DatabaseManager()
    {
        this.path = Minigames.getInstance().getDataFolder().getAbsolutePath() + "/stats.db";

        // Make sure data folders exist
        if (!Minigames.getInstance().getDataFolder().exists())
        {
            Minigames.getInstance().getDataFolder().mkdirs();
        }

        try
        {
            this.conn = DriverManager.getConnection("jdbc:sqlite:" + path);
            this.games = this.types = new ArrayList<>();

            setGamesAndTypes();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    // Make instance.
    public static DatabaseManager getInstance()
    {
        if (db == null)
        {
            db = new DatabaseManager();
        }

        return db;
    }

    // Setting the lists.
    private void setGamesAndTypes()
    {
        games.add("spleef");
        games.add("bowspleef");
        games.add("tntrun");
        games.add("buildit");
        games.add("blockparty");

        types.add("wins");
        types.add("losses");
    }

    // Generate the table to store data.
    public void createScoreTable()
    {
        try
        {
            Statement stmt = conn.createStatement();
            stmt.execute("    CREATE TABLE IF NOT EXISTS minigame_scores (" +
                    "\n    uuid TEXT PRIMARY KEY," +
                    "\n    spleef_wins INTEGER NOT NULL DEFAULT 0," +
                    "\n    spleef_losses INTEGER NOT NULL DEFAULT 0," +
                    "\n    bowspleef_wins INTEGER NOT NULL DEFAULT 0," +
                    "\n    bowspleef_losses INTEGER NOT NULL DEFAULT 0," +
                    "\n    tntrun_wins INTEGER NOT NULL DEFAULT 0," +
                    "\n    tntrun_losses INTEGER NOT NULL DEFAULT 0," +
                    "\n    blockparty_wins INTEGER NOT NULL DEFAULT 0," +
                    "\n    blockparty_losses INTEGER NOT NULL DEFAULT 0)" +
                    "\n    buildit_first INTEGER NOT NULL DEFAULT 0," +
                    "\n    buildit_second INTEGER NOT NULL DEFAULT 0," +
                    "\n    buildit_third INTEGER NOT NULL DEFAULT 0," +
                    "\n");
            stmt.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    // Close the database.
    public void close() {
        try
        {
            if ((conn != null) && !conn.isClosed())
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    // Add the player to the database.
    public void addPlayer(UUID uuid)
    {

        // SQL code.
        String sql = "INSERT INTO minigame_scores (uuid) VALUES (?)";

        try
        {
            // Prepare the statement, set the string, and execute update.
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            preparedStatement.executeUpdate();

            // Close the prepared statement.
            preparedStatement.close();
        }

        // If something were a problem.
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    // Check if the player exists.
    public boolean playerExists(UUID uuid)
    {
        String sql = "SELECT * FROM minigame_scores WHERE uuid = (?)";

        try
        {

            // Prepare the statement and get result set.
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, uuid.toString());
            ResultSet resultSet = preparedStatement.executeQuery();

            // Close it and return if result found.
            preparedStatement.close();
            return resultSet.next();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    // Add a point to the game they won.
    public void addWin(UUID uuid, String game)
    {

        game = game.toLowerCase();

        // Check if player exists before continuing.
        if (playerExists(uuid))
        {
            addPlayer(uuid);
        }

        // Double check right game.
        if (games.contains(game))
        {
            String addToSQL = game + "_wins";
            String sql = "UPDATE minigame_scores SET " + addToSQL + " = " + addToSQL + " + 1 WHERE uuid = (?)";

            try
            {
                // Prepare to add the point.
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.executeUpdate();

                // Close.
                preparedStatement.close();

            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

        }
    }

    // Similar to above for more positioned placed games.
    public void addWin(UUID uuid, String game, String pos)
    {
        game = game.toLowerCase();

        if (playerExists(uuid))
        {
            addPlayer(uuid);
        }

        if (games.contains(game))
        {
            String addToSQL = game + "_" + pos;
            String sql = "UPDATE minigame_scores SET " + addToSQL + " = " + addToSQL + " + 1 WHERE uuid = (?)";

            try {
                PreparedStatement query = this.conn.prepareStatement(sql);

                try {
                    query.setString(1, uuid.toString());
                    query.executeUpdate();
                } catch (Throwable var10) {
                    if (query != null) {
                        try {
                            query.close();
                        } catch (Throwable var9) {
                            var10.addSuppressed(var9);
                        }
                    }

                    throw var10;
                }

                if (query != null) {
                    query.close();
                }
            } catch (SQLException var11) {
                SQLException e = var11;
                e.printStackTrace();
            }

        }
    }

    // Win point method, but for loss.
    public void addLoss(UUID uuid, String game)
    {
        game = game.toLowerCase();

        // Check if player exists before continuing.
        if (playerExists(uuid))
        {
            addPlayer(uuid);
        }

        // Double check right game.
        if (games.contains(game))
        {
            String addToSQL = game + "_losses";
            String sql = "UPDATE minigame_scores SET " + addToSQL + " = " + addToSQL + " + 1 WHERE uuid = (?)";

            try
            {
                // Prepare to add the point.
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.executeUpdate();

                // Close.
                preparedStatement.close();

            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }

        }
    }

    // Set the amount of wins for player.
    public void setWins(UUID uuid, String game, int amt)
    {
        game = game.toLowerCase();

        // Check if player exists before continuing.
        if (playerExists(uuid))
        {
            addPlayer(uuid);
        }

        // Double check right game.
        if (games.contains(game))
        {
            String addToSQL = game + "_wins";
            String sql = "UPDATE minigame_scores SET " + addToSQL + " = " + amt + " WHERE uuid = (?)";

            try
            {
                // Prepare to add the point.
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.executeUpdate();

                // Close.
                preparedStatement.close();

            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    // Above but for losses.
    public void setLosses(UUID uuid, String game, int amt)
    {
        game = game.toLowerCase();

        // Check if player exists before continuing.
        if (playerExists(uuid))
        {
            addPlayer(uuid);
        }

        // Double check right game.
        if (games.contains(game))
        {
            String addToSQL = game + "_losses";
            String sql = "UPDATE minigame_scores SET " + addToSQL + " = " + amt + " WHERE uuid = (?)";

            try
            {
                // Prepare to add the point.
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.executeUpdate();

                // Close.
                preparedStatement.close();

            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
    }

    public String getNamePos(String game, String type, int pos)
    {
        game = game.toLowerCase();
        type = type.toLowerCase();

        // Pre checks.
        if (!games.contains(game))
        {
            return null;
        }
        else if (!types.contains(type))
        {
            return null;
        }
        else
        {
            String addToSQL = game + "_" + type;
            String sql = "SELECT uuid FROM minigame_scores ORDER BY " + addToSQL + " DESC";

            try
            {
                // Prepare and get results.
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet set = preparedStatement.executeQuery();

                // Iterate through all results.
                int index = 1;
                while (set.next())
                {

                    // If found, grab, store, and return.
                    if (index == pos)
                    {
                        OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(set.getString("uuid")));
                        String str = p.getName();
                        preparedStatement.close();
                        return str;
                    }

                    index++;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    public String getScorePos(String game, String type, int pos)
    {
        game = game.toLowerCase();
        type = type.toLowerCase();

        // Pre checks.
        if (!games.contains(game))
        {
            return null;
        }
        else if (!types.contains(type))
        {
            return null;
        }
        else
        {
            String addToSQL = game + "_" + type;
            String sql = "SELECT uuid FROM minigame_scores ORDER BY " + addToSQL + " DESC";

            try
            {
                // Prepare and get results.
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ResultSet set = preparedStatement.executeQuery();

                // Iterate through all results.
                int index = 1;
                while (set.next())
                {

                    // If found, grab, store, and return.
                    if (index == pos)
                    {
                        int score = set.getInt(addToSQL);
                        String str = Integer.toString(score);
                        preparedStatement.close();
                        return str;
                    }

                    index++;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            return null;
        }
    }

    // Get stats from player.
    public int[] getStats(UUID uuid, String game) {
        game = game.toLowerCase();

        if (!games.contains(game))
        {
            return null;
        }
        else
        {
            String addToSQL = game + "_wins," + game + "_losses";
            String sql = "SELECT " + addToSQL + " FROM minigame_scores WHERE uuid = (?)";

            try
            {
                // Get the stats for player.
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, uuid.toString());
                ResultSet set = preparedStatement.executeQuery();

                // Check if found, otherwise return null.
                if (set.next())
                {
                    int[] stats = new int[2];

                    stats[0] = set.getInt(2);
                    stats[1] = set.getInt(1);

                    preparedStatement.close();
                    return stats;
                }
                else
                {
                    return null;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }
    }

    // Find player who may be offline.
    public OfflinePlayer findPlayer(String name)
    {
        String sql = "SELECT uuid FROM minigame_scores";

        try
        {
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet set = preparedStatement.executeQuery();

            // Iterate through each uuid till found.
            while(set.next())
            {
                String uuid = set.getString(1);
                OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
                if (p.getName().equalsIgnoreCase(name))
                {
                    preparedStatement.close();
                    return p;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
