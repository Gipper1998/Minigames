package me.gipper1998.minigames.games.buildit;

/***
 * Actual wizard area that sends data back to the
 * in setup wizard section.
 */

import lombok.Getter;
import me.gipper1998.minigames.files.MessageManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BuildItSetupWizard {
    private BuildItArenaSetupTemplate template;

    private List<String> messagesPrompts = new ArrayList<>();

    private boolean editing;

    @Getter
    private Player p;

    private String prevArenaName;

    // Constructors for both editing and new arena.
    public BuildItSetupWizard(Player p, String name)
    {
        this.p = p;
        this.template = new BuildItArenaSetupTemplate(name);
        this.editing = false;
        addPrompts();
        MessageManager.getInstance().sendStringList("buildit.wizard_cmds", p);
        p.setGameMode(GameMode.CREATIVE);
    }

    public BuildItSetupWizard(Player p, BuildItArena arena)
    {
        this.p = p;
        this.template = new BuildItArenaSetupTemplate(arena);
        this.editing = true;
        this.prevArenaName = this.template.getName();
        addPrompts();
        MessageManager.getInstance().sendStringList("buildit.wizard_cmds", p);
        p.setGameMode(GameMode.CREATIVE);
    }

    // Check from chat for setup.
    public boolean fromChat(String message)
    {
        if (message.equalsIgnoreCase(messagesPrompts.get(0)))
        {
            template.setBuilderSpot(p.getLocation());
            MessageManager.getInstance().sendMessage("buildit.wizard_builder_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(1)))
        {
            template.setLobby(p.getLocation());
            MessageManager.getInstance().sendMessage("buildit.wizard_lobby_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(2)))
        {
            template.setExit(p.getLocation());
            MessageManager.getInstance().sendMessage("buildit.wizard_exit_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(3)))
        {
            template.setSpectate(p.getLocation());
            MessageManager.getInstance().sendMessage("buildit.wizard_spectator_spawn_set", p);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(7)))
        {
            MessageManager.getInstance().sendMessage("buildit.exit_wizard", p);
            exitWizard(false);
            return true;
        }
        else if (message.equalsIgnoreCase(messagesPrompts.get(8)))
        {
            // Check if arena's done.
            if (isComplete())
            {
                MessageManager.getInstance().sendMessage("buildit.wizard_arena_saved", p);
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
            if (BuildItArenaManager.getInstance().findArena(message) == null)
            {
                template.setName(message);
                MessageManager.getInstance().sendMessage("buildit.wizard_new_name_set", this.p);
                return true;
            }
            MessageManager.getInstance().sendMessage("buildit.wizard_name_cannot_set", this.p);
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
                    MessageManager.getInstance().sendNumberMessage("buildit.wizard_minimum_set", min, p);
                    return true;
                }
                MessageManager.getInstance().sendMessage("buildit.wizard_min_not_accepted", p);
                return true;
            }
            MessageManager.getInstance().sendMessage("buildit.wizard_no_number", p);
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
                    MessageManager.getInstance().sendNumberMessage("buildit.wizard_maximum_set", max, p);
                    return true;
                }
                MessageManager.getInstance().sendMessage("buildit.wizard_max_not_accepted", p);
                return true;
            }
            MessageManager.getInstance().sendMessage("buildit.wizard_no_number", p);
            return true;
        }
        return false;
    }

    // Add the prompts to the message list.
    private void addPrompts()
    {
        messagesPrompts.add("set_builder");
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
                template.setLobby(template.getSpectate());
            }
            if (editing)
            {
                BuildItArenaManager.getInstance().deleteArena(prevArenaName);
                BuildItArenaManager.getInstance().createArena(template);
                prevArenaName = "";
            }
            else
            {
                BuildItArenaManager.getInstance().createArena(template);
            }
        }
        BuildItInSetupWizard.getInstance().removePlayer(p);
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
        if (template.getBuilderSpot() == null)
        {
            MessageManager.getInstance().sendMessage("buildit.wizard_builder_not_set", p);
            return false;
        }
        if (template.getSpectate() == null)
        {
            MessageManager.getInstance().sendMessage("buildit.wizard_spectator_not_set", p);
            return false;
        }
        if (template.getExit() == null)
        {
            MessageManager.getInstance().sendMessage("buildit.wizard_exit_not_set", p);
            return false;
        }
        if (template.getMinimum() == 0)
        {
            MessageManager.getInstance().sendMessage("buildit.wizard_minimum_not_set", p);
            return false;
        }
        if (template.getMaximum() == 0)
        {
            MessageManager.getInstance().sendMessage("buildit.wizard_maximum_not_set", p);
            return false;
        }
        return true;
    }
}
