package me.gipper1998.minigames.files;

/***
 * Message manager.
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.gipper1998.minigames.utils.FileBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MessageManager {
    private static MessageManager mm;
    private FileBuilder messages;

    // Constructor.
    public MessageManager()
    {
        messages = new FileBuilder("messages.yml");
    }

    // Grab instance.
    public static MessageManager getInstance()
    {
        if (mm == null) {
            mm = new MessageManager();
        }

        return mm;
    }

    // Reload messages config.
    public void reloadMessages()
    {
        messages.reloadConfig();
    }

    // Send player message.
    public void sendCustomPlayerMessage(String message, Player p)
    {
        if (!message.isEmpty())
        {
            // Replace prefix and send message, translating the color.
            message = replacePrefix(message);
            p.sendMessage(translateHEX(message));
        }
    }

    // Send player message from file, similar to previous method.
    public void sendMessage(String path, Player p)
    {
        String message = messages.getConfig().getString(path);

        if (!message.isEmpty())
        {
            message = replacePrefix(message);
            p.sendMessage(translateHEX(message));
        }
    }

    // Send message with string.
    public void sendStringMessage(String path, String text, Player p)
    {
        String message = messages.getConfig().getString(path);

        if (message.isEmpty())
        {
            return;
        }
        message = replacePrefix(message);
        message = message.replaceAll("<arenaname>", text);
        message = message.replaceAll("<word>", text);
        p.sendMessage(translateHEX(message));
    }

    // Send number message, replacing keywords.
    public void sendNumberMessage(String path, int num, Player p)
    {
        String message = messages.getConfig().getString(path);

        message = replacePrefix(message);
        message = message.replaceAll("<minimum>", Integer.toString(num));
        message = message.replaceAll("<maximum>", Integer.toString(num));
        message = message.replaceAll("<time>", Integer.toString(num));

        if (!message.isEmpty())
        {
            p.sendMessage(translateHEX(message));
        }
    }

    // Similar to above, but for buildit.
    public void sendNumberMessageBuildIt(String path, int num, List<Player> total, Player guessedCorreclty)
    {
        String message = messages.getConfig().getString(path);

        if (!message.isEmpty())
        {
            message = this.replacePrefix(message);
            message = message.replaceAll("<playername>", guessedCorreclty.getName());
            message = message.replaceAll("<points>", Integer.toString(num));

            for (Player p : total)
            {
                p.sendMessage(translateHEX(message));
            }

        }
    }

    // Send message with player name.
    public void sendPlayerNameMessage(String path, Player target, Player p)
    {
        String message = messages.getConfig().getString(path);

        if (!message.isEmpty())
        {
            message = replacePrefix(message);
            message = message.replaceAll("<playername>", target.getName());
            message = message.replaceAll("<builder>", target.getName());
            p.sendMessage(translateHEX(message));
        }
    }

    // Send message with arena name.
    public void sendArenaNameMessage(String path, String text, Player p)
    {
        String message = messages.getConfig().getString(path);

        if (!message.isEmpty())
        {
            message = replacePrefix(message);
            message = message.replaceAll("<arenaname>", text);
            p.sendMessage(translateHEX(message));
        }
    }

    // Send message with vault reward.
    public void sendVaultPlayerMessage(String path, Player p, int amount)
    {
        String message = messages.getConfig().getString(path);

        if (!message.isEmpty())
        {
            message = replacePrefix(message);
            message = message.replaceAll("<money>", Integer.toString(amount));
            p.sendMessage(translateHEX(message));
        }
    }

    // Send console message.
    public void sendConsoleMessage(String path)
    {
        String message = messages.getConfig().getString(path);
        message = replacePrefix(message);
        Bukkit.getConsoleSender().sendMessage(translateHEX(message));
    }

    // Custom console message.
    public void sendCustomConsoleMessage(String message)
    {
        Bukkit.getConsoleSender().sendMessage(translateHEX(message));
    }

    // Leaderboard string list message.
    public void sendLeaderboardStringList(Player p, String path, String wins, String losses, OfflinePlayer target)
    {
        List<String> messageList = messages.getConfig().getStringList(path);

        for (String message : messageList)
        {
            if (!message.isEmpty())
            {
                message = replacePrefix(message);
                message = message.replaceAll("<playername>", target.getName());
                message = message.replaceAll("<wins>", wins);
                message = message.replaceAll("<losses>", losses);
                p.sendMessage(translateHEX(message));
            }
        }

    }

    // Send message of stringlist.
    public void sendStringList(String path, Player p)
    {
        List<String> messageList = messages.getConfig().getStringList(path);

        for (String message : messageList)
        {
            if (!message.isEmpty())
            {
                message = replacePrefix(message);
                p.sendMessage(translateHEX(message));
            }
        }
    }

    // Get the signs string list.
    public List<String> getSignStringList(String path) {
        List<String> messageList = messages.getConfig().getStringList(path);
        List<String> sendMessages = new ArrayList();

        for (String message : messageList)
        {
            if (!message.isEmpty())
            {
                message = replacePrefix(message);
                sendMessages.add(translateHEX(message));
            }
        }
        return sendMessages;
    }

    // Get string from config.
    public String getString(String path)
    {
        String message = messages.getConfig().getString(path);
        if (message.isEmpty())
        {
            return "";
        }
        else
        {
            message = replacePrefix(message);
            return translateHEX(message);
        }
    }

    // Translate the color using hex.
    public String translateColor(String message)
    {
        return translateHEX(message);
    }

    // Set leaderboard message from file.
    public List<String> setPlayersInLeaderboard(String path, String intPath, List<String> players, int currentTime)
    {
        List<String> messageList = messages.getConfig().getStringList(path);

        if (messageList.isEmpty())
        {
            return null;
        }
        else
        {
            List<String> temp = new ArrayList();
            for (String line : temp)
            {
                if (line.contains("<playerlist>"))
                {
                    line = line.replaceAll("<playerlist>", "");

                    // Check size of what leaderboard should be.
                    int size = ConfigManager.getInstance().getInt(intPath);
                    if (size > players.size())
                    {
                        size = players.size();
                    }

                    for (int i = 0; i < size; ++i)
                    {
                        temp.add(translateHEX(line + players.get(i)));
                    }
                }
                else if (line.contains("<currenttime>"))
                {
                    line = line.replaceAll("<currenttime>", Integer.toString(currentTime));
                    temp.add(translateHEX(line));
                }
                else
                {
                    temp.add(translateHEX(line));
                }
            }

            return temp;
        }
    }

    // Translate hex/color into chat.
    private String translateHEX(String message)
    {
        Pattern HEX = Pattern.compile("#([A-Fa-f0-9]{6})");
        Matcher matcher = HEX.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 32);

        while(matcher.find())
        {
            String group = matcher.group(1);
            char c = group.charAt(0);
            matcher.appendReplacement(buffer, "§x§" + c + "§" + group.charAt(1) + "§" + group.charAt(2) + "§" + group.charAt(3) + "§" + group.charAt(4) + "§" + group.charAt(5));
        }

        return ChatColor.translateAlternateColorCodes('&', matcher.appendTail(buffer).toString());
    }

    // Message to send builder word.
    public void sendBuildWordMessage(String path, String word, Player p) {
        String message = messages.getConfig().getString(path);

        if (!message.isEmpty())
        {
            message = this.replacePrefix(message);
            message = message.replaceAll("<word>", word);
            p.sendMessage(this.translateHEX(message));
        }
    }

    // Replace prefix for everything.
    private String replacePrefix(String message)
    {
        if (message != null && !message.isEmpty())
        {
            message = message.replaceAll("<prefix>", this.getPrefix("prefix"));
            message = message.replaceAll("<spleef_prefix>", this.getPrefix("spleef.prefix"));
            message = message.replaceAll("<bowspleef_prefix>", this.getPrefix("bowspleef.prefix"));
            message = message.replaceAll("<tntrun_prefix>", this.getPrefix("tntrun.prefix"));
            message = message.replaceAll("<buildit_prefix>", this.getPrefix("buildit.prefix"));
            message = message.replaceAll("<buildit_chatprefix>", this.getPrefix("buildit.chat_prefix"));
            message = message.replaceAll("<blockparty_prefix>", this.getPrefix("blockparty.prefix"));
            return message;
        }
        else
        {
            return "";
        }
    }

    // Create in game message.
    public void inGameMessage(Player sender, Player p, String chatMessage)
    {
        String message = messages.getConfig().getString("buildit.in_game_chat");
        message = this.replacePrefix(message);
        message = message.replaceAll("<player>", sender.getName());
        message = this.translateHEX(message);
        message = message.replaceAll("<message>", chatMessage);
        p.sendMessage(message);
    }

    // Get prefix from config.
    private String getPrefix(String path)
    {
        return messages.getConfig().getString(path);
    }

    // Get the particular string list.
    public List<String> getStringList(String path)
    {
        return messages.getConfig().getStringList(path);
    }
}
