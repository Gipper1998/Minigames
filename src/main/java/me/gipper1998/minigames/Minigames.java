package me.gipper1998.minigames;

/***
 * Minigames plugin.
 * @author gipper1998
 */

import java.util.ArrayList;
import java.util.List;
import me.gipper1998.minigames.files.ConfigManager;
import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.games.bowspleef.BowSpleefArenaManager;
import me.gipper1998.minigames.games.bowspleef.BowSpleefCommandManager;
import me.gipper1998.minigames.games.bowspleef.BowSpleefInSetupWizard;
import me.gipper1998.minigames.games.bowspleef.BowSpleefSignManager;
import me.gipper1998.minigames.games.buildit.BuildItArenaManager;
import me.gipper1998.minigames.games.buildit.BuildItCommandManager;
import me.gipper1998.minigames.games.buildit.BuildItInSetupWizard;
import me.gipper1998.minigames.games.buildit.BuildItSignManager;
import me.gipper1998.minigames.games.spleef.SpleefArenaManager;
import me.gipper1998.minigames.games.spleef.SpleefCommandManager;
import me.gipper1998.minigames.games.spleef.SpleefInSetupWizard;
import me.gipper1998.minigames.games.spleef.SpleefSignManager;
import me.gipper1998.minigames.softdepends.PAPIManager;
import me.gipper1998.minigames.softdepends.VaultManager;
import me.gipper1998.minigames.utils.DatabaseManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Minigames extends JavaPlugin implements TabExecutor
{

    // Static to have every class grab plugin data.
    private static Minigames main;

    // Get instance.
    public static Minigames getInstance()
    {
        return main;
    }

    // Enable the plugin (using it as constructor).
    public void onEnable()
    {
        // Make plugin equal to this.
        main = this;

        // Set up the files.
        DatabaseManager.getInstance().createScoreTable();
        ConfigManager.getInstance().reloadConfig();
        MessageManager.getInstance().reloadMessages();

        // Make minigames be a command.
        this.getCommand("minigames").setExecutor(this);

        // Register vault.
        if ((this.getServer().getPluginManager().getPlugin("Vault") != null) && VaultManager.getInstance().registerVault())
        {
            MessageManager.getInstance().sendConsoleMessage("vault_enable");
        }

        // Register PAPI.
        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null)
        {
            PAPIManager.getInstance().register();
            MessageManager.getInstance().sendConsoleMessage("papi_enable");
        }

        // Register WorldEdit.
        if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
        }

        // Start the games up.
        this.startSpleefGames();
        this.startBowSpleefGames();
        this.startBuildItGames();
    }

    // Disable the plugin (using it ad deconstructor).
    public void onDisable()
    {
        try
        {
            DatabaseManager.getInstance().close();
            endSpleefGames();
            endBowSpleefGames();
            endBuildItGames();
        }
        catch (Exception e)
        {
            MessageManager.getInstance().sendCustomConsoleMessage("Somehow error");
        }

    }

    // On command method for minigames.
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {

        // Reload plugin.
        if (args[0].equalsIgnoreCase("reload"))
        {

            // Check if player.
            if (sender instanceof ConsoleCommandSender) {
                ConfigManager.getInstance().reloadConfig();
                SpleefArenaManager.getInstance().reloadArenas();
                BowSpleefArenaManager.getInstance().reloadArenas();
                BuildItArenaManager.getInstance().reloadArenas();
                return true;
            }
            else
            {
                if (sender instanceof Player)
                {
                    Player p = (Player)sender;
                    if (p.hasPermission("minigames.admin"))
                    {
                        ConfigManager.getInstance().reloadConfig();
                        SpleefArenaManager.getInstance().reloadArenas();
                        BowSpleefArenaManager.getInstance().reloadArenas();
                        BuildItArenaManager.getInstance().reloadArenas();
                    }
                    else
                    {
                        MessageManager.getInstance().sendMessage("no-perms", p);
                    }
                }

                return true;
            }
        }
        else
        {
            return args[0].equalsIgnoreCase("info");
        }
    }

    // Tab list.
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args)
    {
        List<String> arguments = new ArrayList();
        if (args.length == 1)
        {
            if (commandSender.hasPermission("minigames.admin"))
            {
                arguments.add("reload");
            }

            arguments.add("info");
            return arguments;
        }
        else
        {
            return null;
        }
    }

    // A lot of starting and ending games.

    private void startSpleefGames()
    {
        SpleefArenaManager.getInstance().loadArenas();
        SpleefSignManager.getInstance().startUpdater();
        SpleefInSetupWizard.getInstance().removeEverybody();
        this.getCommand("spleef").setExecutor(new SpleefCommandManager());
    }

    private void startBowSpleefGames()
    {
        BowSpleefArenaManager.getInstance().loadArenas();
        BowSpleefSignManager.getInstance().startUpdater();
        BowSpleefInSetupWizard.getInstance().removeEverybody();
        this.getCommand("bowspleef").setExecutor(new BowSpleefCommandManager());
    }

    private void startBuildItGames()
    {
        BuildItArenaManager.getInstance().loadArenas();
        BuildItSignManager.getInstance().startUpdater();
        BuildItInSetupWizard.getInstance().removeEverybody();
        this.getCommand("buildit").setExecutor(new BuildItCommandManager());
    }

    private void endSpleefGames()
    {
        SpleefArenaManager.getInstance().shutGamesDown();
        SpleefInSetupWizard.getInstance().removeEverybody();
    }

    private void endBowSpleefGames()
    {
        BowSpleefArenaManager.getInstance().shutGamesDown();
        BowSpleefInSetupWizard.getInstance().removeEverybody();
    }

    private void endBuildItGames()
    {
        BuildItArenaManager.getInstance().shutGamesDown();
        BuildItInSetupWizard.getInstance().removeEverybody();
    }
}
