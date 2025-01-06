package me.gipper1998.minigames.games.spleef;

/***
 * Where the game begins, a lot of commands.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lombok.Getter;
import lombok.Setter;
import me.gipper1998.minigames.Minigames;
import me.gipper1998.minigames.files.ConfigManager;
import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.softdepends.VaultManager;
import me.gipper1998.minigames.utils.DatabaseManager;
import me.gipper1998.minigames.utils.FireworkBuilder;
import me.gipper1998.minigames.utils.ItemBuilder;
import me.gipper1998.minigames.utils.ItemStoreManager;
import me.gipper1998.minigames.utils.PotionBuilder;
import me.gipper1998.minigames.utils.ScoreboardBuilder;
import me.gipper1998.minigames.utils.TNTBuilder;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class SpleefGameManager extends BukkitRunnable implements Listener
{

    @Getter
    private SpleefArena arena;

    private Random rand;
    private int waitTime = 10;

    @Getter
    private int gameTime = 120;
    private int startDelay = 5;
    private int winnerDelay = 5;
    private int currentTime = 0;

    @Setter @Getter
    private SpleefGameStatus status;

    private Player winner;
    private String EXIT_ITEM;
    private String GOLD_SPADE_ITEM;
    private String DIAMOND_SPADE_ITEM;
    private String SNOWBALL_ITEM;
    private String TNT;
    private int tntTime;
    private boolean tntEnabled;
    private HashMap<Player, ItemStoreManager> playersStuff;

    @Getter
    private List<Player> totalPlayers;

    @Getter
    private List<Player> playersInGame;

    private List<Location> blocksBroken;

    @Getter
    private List<Player> spectators;
    private List<Integer> events;
    private PotionBuilder invisiblePotion;
    private ScoreboardBuilder scoreboard;
    private boolean scoreCreated;
    private boolean reloadGameData;

    // Main constructor.
    public SpleefGameManager(SpleefArena arena)
    {
        // Set all variables.
        this.status = SpleefGameStatus.WAIT;
        this.winner = null;
        this.EXIT_ITEM = "";
        this.GOLD_SPADE_ITEM = "";
        this.DIAMOND_SPADE_ITEM = "";
        this.SNOWBALL_ITEM = "";
        this.TNT = "";
        this.tntTime = 0;
        this.tntEnabled = false;
        this.playersStuff = new HashMap();
        this.totalPlayers = new ArrayList();
        this.playersInGame = new ArrayList();
        this.blocksBroken = new ArrayList();
        this.spectators = new ArrayList();
        this.events = new ArrayList();
        this.scoreCreated = false;
        this.reloadGameData = false;
        this.arena = arena;
        this.waitTime = ConfigManager.getInstance().getInt("spleef.waiting_time");
        this.gameTime = ConfigManager.getInstance().getInt("spleef.total_game_time");
        this.EXIT_ITEM = MessageManager.getInstance().getString("spleef.leave_item");
        this.GOLD_SPADE_ITEM = MessageManager.getInstance().getString("spleef.gold_shovel");
        this.DIAMOND_SPADE_ITEM = MessageManager.getInstance().getString("spleef.diamond_shovel");
        this.SNOWBALL_ITEM = MessageManager.getInstance().getString("spleef.snowball");
        this.TNT = "TNT_SPLEEF";
        this.rand = new Random();
        this.invisiblePotion = new PotionBuilder("invis", (this.startDelay + 1) * 20, 5);
        this.currentTime = this.waitTime;

        // Load and run.
        loadEvents();
        this.runTaskTimer(Minigames.getInstance(), 20L, 20L);
        Minigames.getInstance().getServer().getPluginManager().registerEvents(this, Minigames.getInstance());

    }

    // Config refresh.
    public void configRefreshed()
    {
        this.reloadGameData = true;
    }

    // Bukkit runnable section.
    public void run()
    {

        // XP bar.
        if (xpBarEnabled())
        {
            for(Player p : totalPlayers)
            {
                float xp = 0.0F;
                switch (this.status)
                {
                    case WAIT:
                        xp = (float)this.currentTime / (float)this.waitTime;
                        p.setLevel(this.currentTime);
                        p.setExp(xp);
                        break;
                    case DELAY_START:
                        xp = (float)this.currentTime / (float)this.startDelay;
                        p.setLevel(this.currentTime);
                        p.setExp(xp);
                        break;
                    case GAME:
                        xp = (float)this.currentTime / (float)this.gameTime;
                        p.setLevel(this.currentTime);
                        p.setExp(xp);
                }
            }
        }

        // Scoreboard section.
        if (this.scoreboardEnabled()) {
            if (!this.scoreCreated)
            {
                this.scoreboard = new ScoreboardBuilder("spleef-" + this.arena.getName());
                this.scoreCreated = true;
            }

            this.updateScoreboard();
        }

        // TNT.
        if (this.tntEnabled)
        {
            if (this.tntTime <= 0)
            {
                this.tntEnabled = false;
            }

            for(Player p : totalPlayers)
            {
                if (this.rand.nextBoolean())
                {
                    TNTBuilder.getInstance().create(p.getLocation(), this.TNT);
                }
            }

            --this.tntTime;
        }

        // Switch for functions.
        switch (this.status)
        {
            case WAIT:
                waitTask();
                break;
            case DELAY_START:
                delayStart();
                break;
            case GAME:
                playGame();
                break;
            case WINNER:
                winnerShowOff();
                break;
        }

    }

    // Waiting for game to start.
    private void waitTask()
    {

        // Check if minimum requirement is met.
        if (playersInGame.size() >= arena.getMin())
        {

            // Countdown for game.
            if (currentTime % 10 == 0 || currentTime <= 5)
            {
                for (Player p : playersInGame) {
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1.0F);
                    MessageManager.getInstance().sendNumberMessage("spleef.starting_game", this.currentTime, p);
                }
            }

            // Decrement.
            --currentTime;

            // If zero, start.
            if (currentTime <= 0) {

                for (Player p : playersInGame)
                {
                    p.teleport(this.arena.getArena());
                    invisiblePotion.addPlayer(p);
                }

                currentTime = startDelay;
                status = SpleefGameStatus.DELAY_START;
            }
        }

        // If a player leaves or minimum not met.
        else
        {
            if (currentTime != waitTime)
            {

                for (Player p : playersInGame)
                {
                    MessageManager.getInstance().sendMessage("spleef.arena_not_enough_players", p);
                }
            }

            // Reload data.
            if (this.reloadGameData) {
                removeEverybody();
                waitTime = ConfigManager.getInstance().getInt("spleef.waiting_time");
                gameTime = ConfigManager.getInstance().getInt("spleef.total_game_time");
                EXIT_ITEM = MessageManager.getInstance().getString("spleef.leave_item");
                GOLD_SPADE_ITEM = MessageManager.getInstance().getString("spleef.gold_shovel");
                DIAMOND_SPADE_ITEM = MessageManager.getInstance().getString("spleef.diamond_shovel");
                SNOWBALL_ITEM = MessageManager.getInstance().getString("spleef.snowball");
                reloadGameData = false;
            }

            // Reset current time to wait.
            currentTime = waitTime;
        }

    }

    // Delayed start to the game.
    private void delayStart()
    {
        // Decrement current time.
        --currentTime;

        // When zero, start.
        if (currentTime <= 0)
        {
            for (Player p : playersInGame)
            {
                giveItems(p);
                p.showPlayer(Minigames.getInstance(), p);
                p.playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0F, 1.0F);
                MessageManager.getInstance().sendMessage("spleef.game_start", p);
                p.setGameMode(GameMode.SURVIVAL);
            }

            currentTime = this.gameTime;
            status = SpleefGameStatus.GAME;
        }

    }

    // Actual game.
    private void playGame() {

        // If only one remains.
        if (playersInGame.size() == 1)
        {
            winner = playersInGame.get(0);
            currentTime = this.winnerDelay;
            status = SpleefGameStatus.WINNER;

            for (Player p : totalPlayers)
            {
                MessageManager.getInstance().sendPlayerNameMessage("spleef.player_winner", this.winner, p);
            }
        }

        // Otherwise play on.
        else
        {

            // Run random gimmicks and other things.
            checkTime();
            --currentTime;

            // If zero, end the game.
            if (this.currentTime <= 0)
            {
                for (Player p : playersInGame)
                {
                    MessageManager.getInstance().sendMessage("spleef.arena_no_winner", p);
                }

                removeEverybody();
                resetArena();
                currentTime = this.waitTime;
                status = SpleefGameStatus.WAIT;
            }

        }
    }

    // Showcase the winner.
    private void winnerShowOff()
    {
        if (currentTime % 2 != 0)
        {
            new FireworkBuilder(this.arena.getArena(), 1, "aqua", 2, 5);
        }

        --currentTime;
        if (currentTime <= 0)
        {
            winnerRewards();
            addLosses();
            winner = null;
            removeEverybody();
            resetArena();
            currentTime = waitTime;
            status = SpleefGameStatus.WAIT;
        }
    }

    // Reset the arena.
    private void resetArena()
    {

        // Get all the blocks broken back into place.
        blocksBroken.forEach((location) -> {
            location.getBlock().setType(Material.SNOW_BLOCK);
        });

        blocksBroken.clear();
        if (reloadGameData)
        {
            waitTime = ConfigManager.getInstance().getInt("spleef.waiting_time");
            gameTime = ConfigManager.getInstance().getInt("spleef.total_game_time");
            EXIT_ITEM = MessageManager.getInstance().getString("spleef.leave_item");
            GOLD_SPADE_ITEM = MessageManager.getInstance().getString("spleef.gold_shovel");
            DIAMOND_SPADE_ITEM = MessageManager.getInstance().getString("spleef.diamond_shovel");
            SNOWBALL_ITEM = MessageManager.getInstance().getString("spleef.snowball");
           reloadGameData = false;
        }

    }

    // Update the scoreboard with new information.
    private void updateScoreboard()
    {
        scoreboard.setTitle(MessageManager.getInstance().getString("spleef.leaderboard_title"));
        ArrayList active = new ArrayList();
        if ((this.status == SpleefGameStatus.GAME) || (this.status == SpleefGameStatus.DELAY_START))
        {
            for (Player p : totalPlayers)
            {
                active.add(p.getName());
            }

            scoreboard.setLines(MessageManager.getInstance().setPlayersInLeaderboard("spleef.leaderboard_game", "spleef.names_on_board_max", active, currentTime));
        }

        if (status == SpleefGameStatus.WAIT)
        {
            for (Player p : totalPlayers)
            {
                active.add(p.getName());
            }

            scoreboard.setLines(MessageManager.getInstance().setPlayersInLeaderboard("spleef.leaderboard_wait", "spleef.names_on_board_max", active, currentTime));
        }

    }

    // Give items to player
    private void giveItems(Player p)
    {
        if (p.hasPermission("minigames.spleef.diamond"))
        {
            p.getInventory().setItem(0, (new ItemBuilder(Material.DIAMOND_SHOVEL, this.DIAMOND_SPADE_ITEM)).getIs());
        }
        else
        {
            p.getInventory().setItem(0, (new ItemBuilder(Material.GOLDEN_SHOVEL, this.GOLD_SPADE_ITEM)).getIs());
        }

        if (ConfigManager.getInstance().getBoolean("spleef.give_snowballs_on_start.enable"))
        {
            if (p.hasPermission("minigames.spleef.diamond"))
            {
                p.getInventory().setItem(1, (new ItemBuilder(Material.SNOWBALL, this.SNOWBALL_ITEM, ConfigManager.getInstance().getInt("spleef.give_snowballs_on_start.diamond_shovel_amount"))).getIs());
            }
            else
            {
                p.getInventory().setItem(1, (new ItemBuilder(Material.SNOWBALL, this.SNOWBALL_ITEM, ConfigManager.getInstance().getInt("spleef.give_snowballs_on_start.gold_shovel_amount"))).getIs());
            }
        }

    }

    // Both clicks event set in a bool to make it easier.
    private boolean bothClicks(PlayerInteractEvent event)
    {
        return event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK;
    }

    // Check xp bar.
    private boolean xpBarEnabled()
    {
        return ConfigManager.getInstance().getBoolean("spleef.exp_time_enable");
    }

    // Check scoreboard enabled.
    private boolean scoreboardEnabled()
    {
        return ConfigManager.getInstance().getBoolean("spleef.scoreboard_enable");
    }

    // Give rewards from vault and anything additional.
    private void winnerRewards()
    {

        // Vault money.
        VaultManager.getInstance().deposit("spleef.vault_message", winner, ConfigManager.getInstance().getInt("spleef.vault_reward"));

        // Rewards via commands.
        List<String> rewardCommands = ConfigManager.getInstance().getStringList("spleef.commands");
        ConsoleCommandSender console = Minigames.getInstance().getServer().getConsoleSender();
        DatabaseManager.getInstance().addWin(winner.getUniqueId(), "spleef");
        Iterator var9 = rewardCommands.iterator();
        if (!rewardCommands.isEmpty()) {
            while(var9.hasNext()) {
                String command = (String)var9.next();
                command = command.replace("<player>", this.winner.getName());
                ServerCommandEvent commandEvent = new ServerCommandEvent(console, command);
                Minigames.getInstance().getServer().getPluginManager().callEvent(commandEvent);
                Minigames.getInstance().getServer().getScheduler().callSyncMethod(Minigames.getInstance(), () -> {
                    return Minigames.getInstance().getServer().dispatchCommand(commandEvent.getSender(), commandEvent.getCommand());
                });
            }
        }

    }

    // Add losses.
    private void addLosses()
    {
        for (Player p : totalPlayers)
        {
            if (!p.equals(winner))
            {
                DatabaseManager.getInstance().addLoss(p.getUniqueId(), "spleef");
            }
        }

    }

    // Add player to game.
    public void addPlayer(Player p)
    {

        // If they are already there.
        if (this.totalPlayers.contains(p))
        {
            MessageManager.getInstance().sendMessage("spleef.player_already_joined", p);
        }
        else
        {
            if (this.status == SpleefGameStatus.WAIT)
            {

                // Check if they are in another arena.
                if (!SpleefArenaManager.getInstance().isInAnotherArena(p))
                {
                    MessageManager.getInstance().sendMessage("spleef.player_in_another_arena", p);
                    return;
                }

                // Check if max.
                if (this.playersInGame.size() >= this.arena.getMax())
                {
                    MessageManager.getInstance().sendMessage("spleef.arena_full", p);
                    return;
                }

                // Add player.
                for (Player player : totalPlayers)
                {
                    p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                    MessageManager.getInstance().sendPlayerNameMessage("spleef.player_join", p, player);
                }

                // Remove potion effects.
                for (PotionEffect effect : p.getActivePotionEffects())
                {
                    p.removePotionEffect(effect.getType());
                }

                // Check if scoreboard enabled and add them.
                if (this.scoreboardEnabled())
                {
                    scoreboard.addPlayer(p);
                }

                MessageManager.getInstance().sendArenaNameMessage("spleef.player_success_join", this.arena.getName(), p);
                playersInGame.add(p);
                totalPlayers.add(p);
                playersStuff.put(p, new ItemStoreManager(p));
                p.teleport(arena.getLobby());
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 1.0F);
                p.setGameMode(GameMode.ADVENTURE);
                p.getInventory().setItem(8, (new ItemBuilder(ConfigManager.getInstance().getBlock("spleef.in_lobby.leave"), this.EXIT_ITEM)).getIs());
                p.updateInventory();
            }
            else if (this.status == SpleefGameStatus.STOP)
            {
                MessageManager.getInstance().sendMessage("spleef.arena_is_disabled", p);
            }
            else
            {
                MessageManager.getInstance().sendMessage("spleef.arena_in-game", p);
            }

        }
    }

    // Remove player, taking them back to where they were.
    public void removePlayer(Player p)
    {
        ItemStoreManager ism = playersStuff.get(p);
        ism.giveBackItems();
        this.playersStuff.remove(p);
        p.teleport(this.arena.getExit());
        this.scoreboard.removePlayer(p);

        if (this.totalPlayers.contains(p))
        {

            // Check which status the arena is.
            if (this.status == SpleefGameStatus.WAIT)
            {
                this.playersInGame.remove(p);
                this.totalPlayers.remove(p);

                for (Player player : playersInGame)
                {
                    MessageManager.getInstance().sendPlayerNameMessage("spleef.player_quit", p, player);
                }

                MessageManager.getInstance().sendArenaNameMessage("spleef.player_success_quit", this.arena.getName(), p);
            }
            else
            {
                MessageManager.getInstance().sendArenaNameMessage("spleef.player_success_quit", this.arena.getName(), p);
                if (this.spectators.contains(p))
                {
                    this.spectators.remove(p);
                }

                if (this.playersInGame.contains(p))
                {
                    this.playersInGame.remove(p);
                }

                this.totalPlayers.remove(p);

                for (PotionEffect effect : p.getActivePotionEffects())
                {
                    p.removePotionEffect(effect.getType());
                }

                if (this.playersInGame.size() == 1)
                {
                    this.winner = this.playersInGame.get(0);
                    this.currentTime = this.winnerDelay;
                    this.status = SpleefGameStatus.WINNER;
                }
            }
        }
        else
        {
            MessageManager.getInstance().sendMessage("spleef.player_not_in_game", p);
        }

    }

    // Remove everyone from the game.
    public void removeEverybody()
    {

        for (Player p : totalPlayers)
        {
            ItemStoreManager ism = playersStuff.get(p);
            ism.giveBackItems();
            p.teleport(this.arena.getExit());
            this.scoreboard.removePlayer(p);
        }

        this.playersInGame.clear();
        this.spectators.clear();
        this.totalPlayers.clear();
        this.playersStuff.clear();
    }

    // Load events from config.
    public void loadEvents()
    {
        if (ConfigManager.getInstance().getBoolean("spleef.enable_time_events"))
        {
            for (String key : ConfigManager.getInstance().getConfigurationSection("spleef.time_events"))
            {
                try
                {
                    events.add(Integer.parseInt(key));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    // Check time for events.
    // Mostly checking if enabled, add to players.
    // And do some crazy things depending on the config.yml file.
    private void checkTime() {

        if (events.contains(currentTime))
        {
            String path = "spleef.time_events." + currentTime + ".";
            if (ConfigManager.getInstance().contains(path + "snowballs"))
            {
                for (Player p : playersInGame)
                {
                    if (ConfigManager.getInstance().contains(path + "random"))
                    {
                        if (ConfigManager.getInstance().getBoolean(path + "random"))
                        {
                            if (rand.nextBoolean())
                            {
                                p.getInventory().addItem(new ItemBuilder(Material.SNOWBALL, SNOWBALL_ITEM, ConfigManager.getInstance().getInt((path + "snowballs"))).getIs());
                                p.updateInventory();
                            }
                        }
                        else
                        {
                            p.getInventory().addItem(new ItemBuilder(Material.SNOWBALL, SNOWBALL_ITEM, ConfigManager.getInstance().getInt((path + "snowballs"))).getIs());
                            p.updateInventory();
                        }
                    }
                    else
                    {
                        p.getInventory().addItem(new ItemBuilder(Material.SNOWBALL, SNOWBALL_ITEM, ConfigManager.getInstance().getInt((path + "snowballs"))).getIs());
                        p.updateInventory();
                    }
                }
            }
            if (ConfigManager.getInstance().contains(path + "tntfall"))
            {
                tntEnabled = true;
                tntTime = ConfigManager.getInstance().getInt(path + "tntfall");
            }
            if (ConfigManager.getInstance().contains(path + "message"))
            {
                for (Player p : playersInGame)
                {
                    MessageManager.getInstance().sendCustomPlayerMessage(ConfigManager.getInstance().getString(path + "message"), p);
                }
            }
            if (ConfigManager.getInstance().contains(path + "speed"))
            {
                PotionBuilder potion = new PotionBuilder("fast", ConfigManager.getInstance().getInt(path + "speed") * 20, ConfigManager.getInstance().getInt(path + "speed_amp"));
                if (ConfigManager.getInstance().contains(path + "random"))
                {
                    if (ConfigManager.getInstance().getBoolean(path + "random"))
                    {
                        for (Player p : playersInGame)
                        {
                            int random = rand.nextInt(100);
                            if (random < (ConfigManager.getInstance().getInt(path + "random")))
                            {
                                potion.addPlayer(p);
                            }
                        }
                    }
                    else
                    {
                        for (Player p : playersInGame)
                        {
                            potion.addPlayer(p);
                        }
                    }
                }
            }

            if (ConfigManager.getInstance().contains(path + "slow"))
            {
                PotionBuilder potion = new PotionBuilder("slow", ConfigManager.getInstance().getInt(path + "slow") * 20, ConfigManager.getInstance().getInt(path + "slow_amp"));
                if (ConfigManager.getInstance().contains(path + "random"))
                {
                    for (Player p : playersInGame)
                    {
                        int random = rand.nextInt(100);
                        if (random < (ConfigManager.getInstance().getInt(path + "random")))
                        {
                            potion.addPlayer(p);
                        }
                    }
                }
                else
                {
                    for (Player p : playersInGame)
                    {
                        potion.addPlayer(p);
                    }
                }
            }

            if (ConfigManager.getInstance().contains(path + "jump"))
            {
                PotionBuilder potion = new PotionBuilder("jump", ConfigManager.getInstance().getInt(path + "jump") * 20, ConfigManager.getInstance().getInt(path + "jump_amp"));
                if (ConfigManager.getInstance().contains(path + "random"))
                {
                    if (ConfigManager.getInstance().getBoolean(path + "random"))
                    {
                        for (Player p : playersInGame)
                        {
                            int random = rand.nextInt(100);
                            if (random < (ConfigManager.getInstance().getInt(path + "random")))
                            {
                                potion.addPlayer(p);
                            }
                        }
                    }
                    else
                    {
                        for (Player p : playersInGame)
                        {
                            potion.addPlayer(p);
                        }
                    }
                }
            }
        }
    }

    // If tnt were to explode.
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (event.getEntity().getType() == EntityType.TNT)
        {
            // Check the name, if equal, destroy only snow blocks.
            if (event.getEntity().getCustomName().equals(TNT))
            {
                Iterator<Block> inter = event.blockList().iterator();
                while (inter.hasNext())
                {
                    Block b = inter.next();
                    if (b.getType() != Material.SNOW_BLOCK)
                    {
                        inter.remove();
                    } else
                    {
                        blocksBroken.add(b.getLocation());
                        b.setType(Material.AIR);
                    }
                }
            }
        }
    }

    // Sign clicking.
    @EventHandler
    public void onPlayerInteractExit(PlayerInteractEvent event)
    {
        if (playersInGame.contains(event.getPlayer()))
        {
            if (bothClicks(event) && event.getMaterial() == Material.BARRIER)
            {
                removePlayer(event.getPlayer());
                event.setCancelled(true);
                return;
            }
        }
    }

    // Check the blocks broken are valid for players.
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        Block b = event.getBlock();

        // Making sure it's the right block.
        if (playersInGame.contains(event.getPlayer()) && b.getType() == Material.SNOW_BLOCK)
        {
            if (status == SpleefGameStatus.GAME)
            {
                event.setDropItems(false);
                blocksBroken.add(b.getLocation());
                b.setType(Material.AIR);
            }
            else
            {
                event.setCancelled(true);
            }
        }
    }

    // Launching projectiles from game.
    @EventHandler
    public void onProjectileLaunch(ProjectileHitEvent event)
    {
        Player p = (Player) event.getEntity().getShooter();
        if (playersInGame.contains(p) && status == SpleefGameStatus.GAME)
        {
            // Making sure it's the right block.
            if (event.getHitBlock() != null && event.getHitBlock().getType() == Material.SNOW_BLOCK)
            {
                blocksBroken.add(event.getHitBlock().getLocation());
                event.getHitBlock().setType(Material.AIR);
            }
        }
    }

    // Keep food level high.
    @EventHandler
    public void foodLevel(FoodLevelChangeEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player p = (Player) event.getEntity();
            if (playersInGame.contains(p))
            {
                event.setCancelled(true);
            }
        }
    }

    // Prevent item drops.
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if (playersInGame.contains(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    // Prevent item pickup.
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event)
    {
        if (playersInGame.contains(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    // Prevent crafting.
    @EventHandler
    public void onCraftEvent(CraftItemEvent event)
    {
        if (event.getWhoClicked() instanceof Player)
        {
            Player p = (Player) event.getWhoClicked();
            if (playersInGame.contains(p))
            {
                event.setCancelled(true);
            }
        }
    }

    // Prevent flying unless spectator.
    @EventHandler
    public void onPlayerFly(PlayerToggleFlightEvent event)
    {
        if (playersInGame.contains(event.getPlayer()))
        {
            event.setCancelled(true);
        }
        if (spectators.contains(event.getPlayer()))
        {

            // Making sure they don't go too far.
            double distance = event.getPlayer().getEyeLocation().distance(arena.getSpectate());
            if (distance > 100)
            {
                event.getPlayer().teleport(arena.getSpectate());
            }
        }
    }

    // Valid player command.
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        if (playersInGame.contains(event.getPlayer()) || spectators.contains(event.getPlayer()))
        {
            String command = event.getMessage();
            if (!(command.equalsIgnoreCase("/spleef leave")))
            {
                event.setCancelled(true);
            }
        }
    }

    // When player leaves.
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (playersInGame.contains(event.getPlayer()) || spectators.contains(event.getPlayer()))
        {
            removePlayer(event.getPlayer());
            DatabaseManager.getInstance().addLoss(event.getPlayer().getUniqueId(), "spleef");
        }
    }

    // Basically if player fell in water or lava, configure things.
    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event)
    {
        if (playersInGame.contains(event.getPlayer()))
        {
            if (status == SpleefGameStatus.GAME)
            {
                Player p = event.getPlayer();
                if ((p.getLocation().getBlock().getType() == Material.WATER) || (p.getLocation().getBlock().getType() == Material.LAVA))
                {
                    for (Player player : totalPlayers)
                    {
                        MessageManager.getInstance().sendPlayerNameMessage("spleef.player_died", p, player);
                    }
                    playersInGame.remove(p);
                    spectators.add(p);
                    p.teleport(arena.getSpectate());
                    p.setGameMode(GameMode.SPECTATOR);

                    for (PotionEffect effect : p.getActivePotionEffects())
                    {
                        p.removePotionEffect(effect.getType());
                    }

                    if (playersInGame.size() == 1)
                    {
                        winner = playersInGame.get(0);
                        currentTime = winnerDelay;
                        status = SpleefGameStatus.WINNER;
                        return;
                    }
                }
            }
            if (status == SpleefGameStatus.DELAY_START)
            {
                if ((event.getPlayer().getLocation().getBlock().getType() == Material.WATER )|| (event.getPlayer().getLocation().getBlock().getType() == Material.LAVA))
                {
                    event.getPlayer().teleport(arena.getArena());
                }
            }
            if (status == SpleefGameStatus.WINNER)
            {
                if ((event.getPlayer().getLocation().getBlock().getType() == Material.WATER) || (event.getPlayer().getLocation().getBlock().getType() == Material.LAVA))
                {
                    event.getPlayer().teleport(arena.getArena());
                }
            }
            if (status == SpleefGameStatus.WAIT)
            {
                if ((event.getPlayer().getLocation().getBlock().getType() == Material.WATER) || (event.getPlayer().getLocation().getBlock().getType() == Material.LAVA))
                {
                    event.getPlayer().teleport(arena.getLobby());
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player p = (Player) event.getEntity();
            if (playersInGame.contains(p))
            {
                event.setCancelled(true);
            }
        }
    }
}
