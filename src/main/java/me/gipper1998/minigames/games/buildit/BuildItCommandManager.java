package me.gipper1998.minigames.games.buildit;

/***
 * Command manager to run commands.
 */

import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.utils.DatabaseManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BuildItCommandManager implements TabExecutor
{
    // Main method.
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args)
    {
        // Check if the console is sending it, should only be runned by players.
        if (commandSender instanceof ConsoleCommandSender)
        {
            MessageManager.getInstance().sendConsoleMessage("no_console");
            return true;
        }

        else
        {
            // Save the player to make things easier.
            Player p = (Player) commandSender;

            // Show list of commands.
            if (args.length == 0)
            {
                viewCommands(p);
                return false;
            }
            else
            {

                // Create section.
                if (args[0].equalsIgnoreCase("create"))
                {

                    // Check perms.
                    if (!p.hasPermission("minigames.create"))
                    {
                        MessageManager.getInstance().sendMessage("no_perms", p);
                    }

                    // Find name.
                    if (args.length < 2)
                    {
                        MessageManager.getInstance().sendMessage("buildit.no_name_wizard", p);
                        return false;
                    }

                    // Set up the wizard.
                    else
                    {
                        String name = args[1].toUpperCase();
                        if ((BuildItArenaManager.getInstance().getArenaNames() != null) &&
                                (BuildItArenaManager.getInstance().getArenaNames().contains(name)))
                        {
                            MessageManager.getInstance().sendMessage("buildit.wizard_arena_exists", p);
                            return false;
                        }
                        else if (BuildItInSetupWizard.getInstance().addPlayer(p, name))
                        {
                            MessageManager.getInstance().sendMessage("buildit.in_wizard", p);
                            return true;
                        }
                        else
                        {
                            MessageManager.getInstance().sendMessage("buildit.wizard_already_in", p);
                            return false;
                        }
                    }
                }

                // Edit wizard for arena, basically similar with create.
                else if (args[0].equalsIgnoreCase("edit"))
                {
                    if (!p.hasPermission("minigames.create"))
                    {
                        MessageManager.getInstance().sendMessage("no_perms", p);
                    }

                    if (args.length < 2)
                    {
                        MessageManager.getInstance().sendMessage("buildit.no_name_wizard", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if ((BuildItArenaManager.getInstance().getArenaNames()) != null &&
                                !(BuildItArenaManager.getInstance().getArenaNames().contains(name)))
                        {
                            MessageManager.getInstance().sendArenaNameMessage("buildit.arena_does_not_exist", name, p);
                            return false;
                        }
                        else
                        {
                            BuildItArena arena = BuildItArenaManager.getInstance().findArena(name);
                            BuildItGameManager game = BuildItArenaManager.getInstance().findGame(name);
                            if (game.getStatus() != BuildItGameStatus.STOP)
                            {
                                MessageManager.getInstance().sendMessage("buildit.arena_need_disable", p);
                                return false;
                            }
                            else if (BuildItInSetupWizard.getInstance().addEditPlayer(p, arena))
                            {
                                MessageManager.getInstance().sendMessage("buildit.in_wizard", p);
                                return true;
                            }
                            else
                            {
                                MessageManager.getInstance().sendMessage("buildit.wizard_already_in", p);
                                return false;
                            }
                        }
                    }
                }

                else if (args[0].equalsIgnoreCase("delete")) {
                    if (!p.hasPermission("minigames.admin")) {
                        MessageManager.getInstance().sendMessage("no_perms", p);
                        return false;
                    } else if (args.length < 2) {
                        MessageManager.getInstance().sendMessage("buildit.no_name", p);
                        return false;
                    } else {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty()) {
                            MessageManager.getInstance().sendMessage("buildit.no_name", p);
                            return false;
                        } else if (BuildItArenaManager.getInstance().findArena(name) == null) {
                            MessageManager.getInstance().sendArenaNameMessage("buildit.arena_does_not_exist", name, p);
                            return false;
                        } else {
                            BuildItArenaManager.getInstance().deleteArena(name.toUpperCase());
                            MessageManager.getInstance().sendMessage("buildit.arena_deleted", p);
                            return true;
                        }
                    }
                }

                // Disable arena.
                else if (args[0].equalsIgnoreCase("disable"))
                {
                    if (!p.hasPermission("minigames.admin"))
                    {
                        MessageManager.getInstance().sendMessage("no_perms", p);
                        return false;
                    }
                    else if (args.length < 2)
                    {
                        MessageManager.getInstance().sendMessage("buildit.no_name", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty())
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_name", p);
                            return false;
                        }
                        else if (BuildItArenaManager.getInstance().findArena(name) == null)
                        {
                            MessageManager.getInstance().sendArenaNameMessage("buildit.arena_does_not_exist", name, p);
                            return false;
                        }
                        else
                        {
                            BuildItArenaManager.getInstance().disableArena(p, BuildItArenaManager.getInstance().findArena(name));
                            return true;
                        }
                    }
                }

                // Enable arena.
                else if (args[0].equalsIgnoreCase("enable"))
                {
                    if (!p.hasPermission("minigames.admin"))
                    {
                        MessageManager.getInstance().sendMessage("no_perms", p);
                        return false;
                    }
                    else if (args.length < 2)
                    {
                        MessageManager.getInstance().sendMessage("buildit.no_name", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty())
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_name", p);
                            return false;
                        }
                        else if (BuildItArenaManager.getInstance().findArena(name) == null)
                        {
                            MessageManager.getInstance().sendArenaNameMessage("buildit.arena_does_not_exist", name, p);
                            return false;
                        } else
                        {
                            BuildItArenaManager.getInstance().enableArena(p, BuildItArenaManager.getInstance().findArena(name));
                            return true;
                        }
                    }
                }

                // Have player join arena.
                else if (args[0].equalsIgnoreCase("join"))
                {
                    if (args.length < 2)
                    {
                        MessageManager.getInstance().sendMessage("buildit.no_name", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty())
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_name", p);
                            return false;
                        }
                        else if (BuildItArenaManager.getInstance().findArena(name) == null)
                        {
                            MessageManager.getInstance().sendArenaNameMessage("buildit.arena_does_not_exist", name, p);
                            return false;
                        }
                        else
                        {
                            BuildItArenaManager.getInstance().findGame(name).addPlayer(p);
                            return true;
                        }
                    }
                }

                // Have player leave arena.
                else if (args[0].equalsIgnoreCase("leave"))
                {
                    BuildItGameManager spleefGameManager = BuildItArenaManager.getInstance().findPlayerInGame(p);
                    if (spleefGameManager != null)
                    {
                        spleefGameManager.removePlayer(p);
                        return true;
                    }
                    else
                    {
                        MessageManager.getInstance().sendMessage("buildit.player_not_in_game", p);
                        return false;
                    }
                }

                // Mostly stats section.
                else
                {
                    if (args[0].equalsIgnoreCase("stats"))
                    {
                        if (args.length < 2)
                        {

                            // Set array stats to show.
                            int[] stats = DatabaseManager.getInstance().getStats(p.getUniqueId(), "buildit");
                            if (stats == null)
                            {
                                MessageManager.getInstance().sendMessage("buildit.player_does_not_exist", p);
                                return true;
                            }

                            statBoard(p, stats[1], stats[0], p);
                            return true;
                        }

                        // Make sure they can view other stats.
                        if ((p.hasPermission("minigames.otherstats")) ||( p.hasPermission("minigames.admin")))
                        {
                            try
                            {
                                OfflinePlayer player = DatabaseManager.getInstance().findPlayer(args[1]);
                                if (player == null)
                                {
                                    MessageManager.getInstance().sendMessage("buildit.player_does_not_exist", p);
                                    return true;
                                }

                                int[] stats = DatabaseManager.getInstance().getStats(player.getUniqueId(), "buildit");
                                statBoard(p, stats[1], stats[0], player);
                                return true;
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }

                    // Set the amount of wins.
                    if (args[0].equalsIgnoreCase("setWins"))
                    {
                        if (!p.hasPermission("minigames.admin"))
                        {
                            MessageManager.getInstance().sendMessage("no_perms", p);
                            return false;
                        }
                        else if (args.length < 2)
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_player_entry", p);
                            return false;
                        }
                        else if (args.length < 3)
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_number_entry", p);
                            return false;
                        }
                        else if (!this.isNumeric(args[2]))
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_number_entry", p);
                            return false;
                        }
                        else
                        {
                            try
                            {
                                OfflinePlayer player = DatabaseManager.getInstance().findPlayer(args[1]);
                                if (player == null)
                                {
                                    MessageManager.getInstance().sendMessage("buildit.player_does_not_exist", p);
                                    return true;
                                } else
                                {
                                    DatabaseManager.getInstance().setWins(player.getUniqueId(), "buildit", Integer.parseInt(args[2]));
                                    MessageManager.getInstance().sendMessage("buildit.stat_set", p);
                                    return true;
                                }
                            }
                            catch (Exception var9)
                            {
                                MessageManager.getInstance().sendMessage("buildit.player_does_not_exist", p);
                                return false;
                            }
                        }
                    }

                    // Set the amount of losses.
                    else if (args[0].equalsIgnoreCase("setLosses"))
                    {
                        if (!p.hasPermission("minigames.admin"))
                        {
                            MessageManager.getInstance().sendMessage("no_perms", p);
                            return false;
                        }
                        else if (args.length < 2)
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_player_entry", p);
                            return false;
                        }
                        else if (args.length < 3)
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_number_entry", p);
                            return false;
                        }
                        else if (!this.isNumeric(args[2]))
                        {
                            MessageManager.getInstance().sendMessage("buildit.no_number_entry", p);
                            return false;
                        }
                        else
                        {
                            try
                            {
                                OfflinePlayer player = DatabaseManager.getInstance().findPlayer(args[1]);
                                if (player == null)
                                {
                                    MessageManager.getInstance().sendMessage("buildit.player_does_not_exist", p);
                                    return true;
                                }
                                else
                                {
                                    DatabaseManager.getInstance().setLosses(player.getUniqueId(), "buildit", Integer.parseInt(args[2]));
                                    MessageManager.getInstance().sendMessage("buildit.stat_set", p);
                                    return true;
                                }
                            }
                            catch (Exception var10)
                            {
                                MessageManager.getInstance().sendMessage("buildit.player_does_not_exist", p);
                                return false;
                            }
                        }
                    }
                    else
                    {
                        return false;
                    }
                }
            }
        }
    }

    // Auto tab completion.
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args)
    {
        List<String> arguments = new ArrayList();
        if (args.length == 1)
        {
            if (commandSender.hasPermission("minigames.admin"))
            {
                arguments.add("create");
                arguments.add("edit");
                arguments.add("delete");
                arguments.add("enable");
                arguments.add("disable");
            }

            arguments.add("join");
            arguments.add("leave");
            arguments.add("stats");
            return arguments;

        }
        else
        {
            if (args.length == 2)
            {
                if (args[0].equalsIgnoreCase("create"))
                {
                    arguments.add("<type_name>");
                    return arguments;
                }

                if (args[0].equalsIgnoreCase("edit"))
                {
                    return BuildItArenaManager.getInstance().getArenaNames();
                }

                if (args[0].equalsIgnoreCase("leave"))
                {
                    return null;
                }

                if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable"))
                {
                    return BuildItArenaManager.getInstance().getArenaNames();
                }
            }

            if (args.length == 3 && (args[0].equalsIgnoreCase("setWins") || args[0].equalsIgnoreCase("setLosses")) && commandSender.hasPermission("minigames.admin"))
            {
                arguments.add("<type_number>");
                return arguments;
            } else
            {
                return null;
            }
        }
    }

    // View all the commands.
    private void viewCommands(Player p) {
        if (p.hasPermission("minigames.admin"))
        {
            MessageManager.getInstance().sendStringList("buildit.commands_page_admin", p);
        }
        else
        {
            MessageManager.getInstance().sendStringList("buildit.commands_page_player", p);
        }

    }

    // Check if it's numeric.
    private boolean isNumeric(String temp)
    {
        try
        {
            Double.parseDouble(temp);
            return true;
        }
        catch (NumberFormatException var3)
        {
            return false;
        }
    }

    // Stat board.
    private void statBoard(Player sender, int wins, int losses, OfflinePlayer target)
    {
        MessageManager.getInstance().sendLeaderboardStringList(sender, "buildit.stats", Integer.toString(wins), Integer.toString(losses), target);
    }
}
