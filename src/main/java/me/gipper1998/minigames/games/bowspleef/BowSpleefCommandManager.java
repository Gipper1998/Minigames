package me.gipper1998.minigames.games.bowspleef;

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

public class BowSpleefCommandManager implements TabExecutor
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
                        MessageManager.getInstance().sendMessage("bowspleef.no_name_wizard", p);
                        return false;
                    }

                    // Set up the wizard.
                    else
                    {
                        String name = args[1].toUpperCase();
                        if ((BowSpleefArenaManager.getInstance().getArenaNames() != null) &&
                                (BowSpleefArenaManager.getInstance().getArenaNames().contains(name)))
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.wizard_arena_exists", p);
                            return false;
                        }
                        else if (BowSpleefInSetupWizard.getInstance().addPlayer(p, name))
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.in_wizard", p);
                            return true;
                        }
                        else
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.wizard_already_in", p);
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
                        MessageManager.getInstance().sendMessage("bowspleef.no_name_wizard", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if ((BowSpleefArenaManager.getInstance().getArenaNames()) != null &&
                                !(BowSpleefArenaManager.getInstance().getArenaNames().contains(name)))
                        {
                            MessageManager.getInstance().sendArenaNameMessage("bowspleef.arena_does_not_exist", name, p);
                            return false;
                        }
                        else
                        {
                            BowSpleefArena arena = BowSpleefArenaManager.getInstance().findArena(name);
                            BowSpleefGameManager game = BowSpleefArenaManager.getInstance().findGame(name);
                            if (game.getStatus() != BowSpleefGameStatus.STOP)
                            {
                                MessageManager.getInstance().sendMessage("bowspleef.arena_need_disable", p);
                                return false;
                            }
                            else if (BowSpleefInSetupWizard.getInstance().addEditPlayer(p, arena))
                            {
                                MessageManager.getInstance().sendMessage("bowspleef.in_wizard", p);
                                return true;
                            }
                            else
                            {
                                MessageManager.getInstance().sendMessage("bowspleef.wizard_already_in", p);
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
                        MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                        return false;
                    } else {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty()) {
                            MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                            return false;
                        } else if (BowSpleefArenaManager.getInstance().findArena(name) == null) {
                            MessageManager.getInstance().sendArenaNameMessage("bowspleef.arena_does_not_exist", name, p);
                            return false;
                        } else {
                            BowSpleefArenaManager.getInstance().deleteArena(name.toUpperCase());
                            MessageManager.getInstance().sendMessage("bowspleef.arena_deleted", p);
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
                        MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty())
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                            return false;
                        }
                        else if (BowSpleefArenaManager.getInstance().findArena(name) == null)
                        {
                            MessageManager.getInstance().sendArenaNameMessage("bowspleef.arena_does_not_exist", name, p);
                            return false;
                        }
                        else
                        {
                            BowSpleefArenaManager.getInstance().disableArena(p, BowSpleefArenaManager.getInstance().findArena(name));
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
                        MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty())
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                            return false;
                        }
                        else if (BowSpleefArenaManager.getInstance().findArena(name) == null)
                        {
                            MessageManager.getInstance().sendArenaNameMessage("bowspleef.arena_does_not_exist", name, p);
                            return false;
                        } else
                        {
                            BowSpleefArenaManager.getInstance().enableArena(p, BowSpleefArenaManager.getInstance().findArena(name));
                            return true;
                        }
                    }
                }

                // Have player join arena.
                else if (args[0].equalsIgnoreCase("join"))
                {
                    if (args.length < 2)
                    {
                        MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                        return false;
                    }
                    else
                    {
                        String name = args[1].toUpperCase();
                        if (name.isEmpty())
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.no_name", p);
                            return false;
                        }
                        else if (BowSpleefArenaManager.getInstance().findArena(name) == null)
                        {
                            MessageManager.getInstance().sendArenaNameMessage("bowspleef.arena_does_not_exist", name, p);
                            return false;
                        }
                        else
                        {
                            BowSpleefArenaManager.getInstance().findGame(name).addPlayer(p);
                            return true;
                        }
                    }
                }

                // Have player leave arena.
                else if (args[0].equalsIgnoreCase("leave"))
                {
                    BowSpleefGameManager bowspleefGameManager = BowSpleefArenaManager.getInstance().findPlayerInGame(p);
                    if (bowspleefGameManager != null)
                    {
                        bowspleefGameManager.removePlayer(p);
                        return true;
                    }
                    else
                    {
                        MessageManager.getInstance().sendMessage("bowspleef.player_not_in_game", p);
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
                            int[] stats = DatabaseManager.getInstance().getStats(p.getUniqueId(), "bowspleef");
                            if (stats == null)
                            {
                                MessageManager.getInstance().sendMessage("bowspleef.player_does_not_exist", p);
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
                                    MessageManager.getInstance().sendMessage("bowspleef.player_does_not_exist", p);
                                    return true;
                                }

                                int[] stats = DatabaseManager.getInstance().getStats(player.getUniqueId(), "bowspleef");
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
                            MessageManager.getInstance().sendMessage("bowspleef.no_player_entry", p);
                            return false;
                        }
                        else if (args.length < 3)
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.no_number_entry", p);
                            return false;
                        }
                        else if (!this.isNumeric(args[2]))
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.no_number_entry", p);
                            return false;
                        }
                        else
                        {
                            try
                            {
                                OfflinePlayer player = DatabaseManager.getInstance().findPlayer(args[1]);
                                if (player == null)
                                {
                                    MessageManager.getInstance().sendMessage("bowspleef.player_does_not_exist", p);
                                    return true;
                                } else
                                {
                                    DatabaseManager.getInstance().setWins(player.getUniqueId(), "bowspleef", Integer.parseInt(args[2]));
                                    MessageManager.getInstance().sendMessage("bowspleef.stat_set", p);
                                    return true;
                                }
                            }
                            catch (Exception var9)
                            {
                                MessageManager.getInstance().sendMessage("bowspleef.player_does_not_exist", p);
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
                            MessageManager.getInstance().sendMessage("bowspleef.no_player_entry", p);
                            return false;
                        }
                        else if (args.length < 3)
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.no_number_entry", p);
                            return false;
                        }
                        else if (!this.isNumeric(args[2]))
                        {
                            MessageManager.getInstance().sendMessage("bowspleef.no_number_entry", p);
                            return false;
                        }
                        else
                        {
                            try
                            {
                                OfflinePlayer player = DatabaseManager.getInstance().findPlayer(args[1]);
                                if (player == null)
                                {
                                    MessageManager.getInstance().sendMessage("bowspleef.player_does_not_exist", p);
                                    return true;
                                }
                                else
                                {
                                    DatabaseManager.getInstance().setLosses(player.getUniqueId(), "bowspleef", Integer.parseInt(args[2]));
                                    MessageManager.getInstance().sendMessage("bowspleef.stat_set", p);
                                    return true;
                                }
                            }
                            catch (Exception var10)
                            {
                                MessageManager.getInstance().sendMessage("bowspleef.player_does_not_exist", p);
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
                    return BowSpleefArenaManager.getInstance().getArenaNames();
                }

                if (args[0].equalsIgnoreCase("leave"))
                {
                    return null;
                }

                if (args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("enable") || args[0].equalsIgnoreCase("disable"))
                {
                    return BowSpleefArenaManager.getInstance().getArenaNames();
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
            MessageManager.getInstance().sendStringList("bowspleef.commands_page_admin", p);
        }
        else
        {
            MessageManager.getInstance().sendStringList("bowspleef.commands_page_player", p);
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
        MessageManager.getInstance().sendLeaderboardStringList(sender, "bowspleef.stats", Integer.toString(wins), Integer.toString(losses), target);
    }
}
