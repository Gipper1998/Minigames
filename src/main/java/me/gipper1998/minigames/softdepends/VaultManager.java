package me.gipper1998.minigames.softdepends;

/***
 * Vault for giving in game currency.
 */

import me.gipper1998.minigames.Minigames;
import me.gipper1998.minigames.files.MessageManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultManager
{
    private static VaultManager vm;
    private Economy economy;

    // Get instance.
    public static VaultManager getInstance()
    {
        if (vm == null)
        {
            vm = new VaultManager();
        }

        return vm;
    }

    // Check if economy is there.
    public boolean registerVault()
    {
        RegisteredServiceProvider<Economy> rsp = Minigames.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null)
        {
            return false;
        } else
        {
            economy = rsp.getProvider();
            return economy != null;
        }
    }

    // Deposit money and send message.
    public void deposit(String path, Player p, int money)
    {
        if (economy != null)
        {
            if (money == 0)
            {
                return;
            }

            economy.depositPlayer(p, money);
            MessageManager.getInstance().sendVaultPlayerMessage(path, p, money);
        }

    }
}
