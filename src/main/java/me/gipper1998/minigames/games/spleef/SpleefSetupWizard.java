package me.gipper1998.minigames.games.spleef;

/***
 * Actual wizard area that sends data back to the
 * in setup wizard section.
 */

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import me.gipper1998.minigames.files.MessageManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SpleefSetupWizard {
    private SpleefArenaSetupTemplate template;

    private List<String> messagesPrompts = new ArrayList<>();

    private boolean editing;

    @Getter
    private Player p;

    private String prevArenaName;

    // Constructors for both editing and new arena.
    public SpleefSetupWizard(Player p, String name)
    {
        this.p = p;
        this.template = new SpleefArenaSetupTemplate(name);
        this.editing = false;
        addPrompts();
        MessageManager.getInstance().sendStringList("spleef.wizard_cmds", p);
        p.setGameMode(GameMode.CREATIVE);
    }

    public SpleefSetupWizard(Player p, SpleefArena arena)
    {
        this.p = p;
        this.template = new SpleefArenaSetupTemplate(arena);
        this.editing = true;
        this.prevArenaName = this.template.getName();
        addPrompts();
        MessageManager.getInstance().sendStringList("spleef.wizard_cmds", p);
        p.setGameMode(GameMode.CREATIVE);
    }

    // Check from chat for setup.
    public boolean fromChat(String message)
    {
        if (message.equalsIgnoreCase(messagesPrompts.get(0)))
        {
            template.setArena(p.getLocation());
            MessageManager.getInstance().sendMessage("spleef.wizard_arena_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(1)))
        {
            template.setLobby(p.getLocation());
            MessageManager.getInstance().sendMessage("spleef.wizard_lobby_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(2)))
        {
            template.setExit(p.getLocation());
            MessageManager.getInstance().sendMessage("spleef.wizard_exit_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(3)))
        {
            template.setSpectate(p.getLocation());
            MessageManager.getInstance().sendMessage("spleef.wizard_spectator_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(7)))
        {
            MessageManager.getInstance().sendMessage("spleef.exit_wizard", p);
            exitWizard(false);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(8)))
        {
            // Check if arena's done.
            if (isComplete())
            {
                MessageManager.getInstance().sendMessage("spleef.wizard_arena_saved", p);
                exitWizard(true);
            }
            return true;
        }

        // Customize section.
        String nameCommand = messagesPrompts.get(4);
        String minCommand = messagesPrompts.get(5);
        String maxCommand = messagesPrompts.get(6);
        boolean getName = false;
        boolean getMax = false;
        boolean getMin = false;
        String[] s = message.split(" ");

        // Set to true.
        for (String temp : s)
        {
            if (temp.compareTo(minCommand) == 0)
            {
                getMin = true;
                break;
            }
            if (temp.compareTo(maxCommand) == 0)
            {
                getMax = true;
                break;
            }
            if (temp.compareTo(nameCommand) == 0)
            {
                getName = true;
                break;
            }
        }

        // Setting it.
        if (getName)
        {
            message = message.replaceAll("set_name", "");
            message = message.replaceAll("\\s+", "");
            if (SpleefArenaManager.getInstance().findArena(message) == null)
            {
                template.setName(message);
                MessageManager.getInstance().sendMessage("spleef.wizard_new_name_set", this.p);
                return true;
            }
            MessageManager.getInstance().sendMessage("spleef.wizard_name_cannot_set", this.p);
            return true;
        }
        if (getMin)
        {
            String numberOnly = message.replaceAll("[^0-9]", "");
            numberOnly = numberOnly.replaceAll(" +", " ");
            numberOnly = numberOnly.replaceAll("\\s", " ");
            if (isNumeric(numberOnly))
            {
                int min = Integer.parseInt(numberOnly);
                if ((min <= template.getMaximum()) || (template.getMaximum() == 0))
                {
                    template.setMinimum(min);
                    MessageManager.getInstance().sendNumberMessage("spleef.wizard_minimum_set", min, p);
                    return true;
                }
                MessageManager.getInstance().sendMessage("spleef.wizard_min_not_accepted", p);
                return true;
            }
            MessageManager.getInstance().sendMessage("spleef.wizard_no_number", p);
            return true;
        }
        if (getMax)
        {
            String numberOnly = message.replaceAll("[^0-9]", "");
            numberOnly = numberOnly.replaceAll(" +", " ");
            numberOnly = numberOnly.replaceAll("\\s", " ");
            if (isNumeric(numberOnly))
            {
                int max = Integer.parseInt(numberOnly);
                if (max >= template.getMinimum() || template.getMinimum() == 0)
                {
                    template.setMaximum(max);
                    MessageManager.getInstance().sendNumberMessage("spleef.wizard_maximum_set", max, p);
                    return true;
                }
                MessageManager.getInstance().sendMessage("spleef.wizard_max_not_accepted", p);
                return true;
            }
            MessageManager.getInstance().sendMessage("spleef.wizard_no_number", p);
            return true;
        }
        return false;
    }

    // Add the prompts to the message list.
    private void addPrompts()
    {
        messagesPrompts.add("set_spawn");
        messagesPrompts.add("set_lobby");
        messagesPrompts.add("set_exit");
        messagesPrompts.add("set_spectate");
        messagesPrompts.add("set_name");
        messagesPrompts.add("set_min");
        messagesPrompts.add("set_max");
        messagesPrompts.add("exit_wizard");
        messagesPrompts.add("complete_wizard");
    }

    // Exit the wizard.
    public void exitWizard(boolean finished)
    {
        p.getInventory().clear();
        p.updateInventory();
        if (finished)
        {
            if (template.getLobby() == null)
            {
                template.setLobby(template.getArena());
            }
            if (editing)
            {
                SpleefArenaManager.getInstance().deleteArena(prevArenaName);
                SpleefArenaManager.getInstance().createArena(template);
                prevArenaName = "";
            }
            else
            {
                SpleefArenaManager.getInstance().createArena(template);
            }
        }
        SpleefInSetupWizard.getInstance().removePlayer(p);
    }

    // Check numeric.
    private boolean isNumeric(String temp)
    {
        try
        {
            Double.parseDouble(temp);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    // Complete function to check everything.
    private boolean isComplete()
    {
        if (template.getArena() == null)
        {
            MessageManager.getInstance().sendMessage("spleef.wizard_arena_not_set", p);
            return false;
        }
        if (template.getSpectate() == null)
        {
            MessageManager.getInstance().sendMessage("spleef.wizard_spectator_not_set", p);
            return false;
        }
        if (template.getExit() == null)
        {
            MessageManager.getInstance().sendMessage("spleef.wizard_exit_not_set", p);
            return false;
        }
        if (template.getMinimum() == 0)
        {
            MessageManager.getInstance().sendMessage("spleef.wizard_minimum_not_set", p);
            return false;
        }
        if (template.getMaximum() == 0)
        {
            MessageManager.getInstance().sendMessage("spleef.wizard_maximum_not_set", p);
            return false;
        }
        return true;
    }
}
