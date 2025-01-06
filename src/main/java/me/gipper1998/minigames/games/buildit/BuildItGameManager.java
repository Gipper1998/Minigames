package me.gipper1998.minigames.games.buildit;

/***
 * Build It game manager, has a lot more going on with
 * additional files needed for game to run.
 * Little different from Spleef and BowSpleef.
 */

import lombok.Getter;
import lombok.Setter;
import me.gipper1998.minigames.Minigames;
import me.gipper1998.minigames.files.ConfigManager;
import me.gipper1998.minigames.files.MessageManager;
import me.gipper1998.minigames.utils.ItemBuilder;
import me.gipper1998.minigames.utils.ItemStoreManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import java.net.http.WebSocket;
import java.util.*;

public class BuildItGameManager extends BukkitRunnable implements WebSocket.Listener
{

    @Getter
    private BuildItArena arena;

    @Getter @Setter
    private BuildItGameStatus status;

    private Player builder;
    private List<Location> blocksPlaced;
    private List<String> bannedBlocks;
    private List<Player> builderOrder;

    @Getter
    private List<Player> totalPlayers;

    private List<Player> spectators;
    private List<Player> gotWordCorrectly;
    private List<Player> voteBuilderCheat;

    private HashMap<Player, ItemStoreManager> playersStuff;

    @Getter
    private int currentTime;

    private int waitTime;
    private int buildTime;
    private int cutBuildTime;
    private int showWordToPlayersTime;
    private int showWordToBuilderTime;
    private int winnerShowOffTime;

    private int firstPoint;
    private int regularPoint;
    private int builderCountIndex;
    private int rounds;
    private String buildWord;

    private boolean cutTime;
    private ItemStack leaveItem;
    private BuildItScoreboardManager scoreboard;

    // Constructor to fill everything.
    public BuildItGameManager(BuildItArena arena)
    {
        this.arena = arena;
        this.leaveItem = new ItemBuilder(ConfigManager.getInstance().getBlock("buildit.in_lobby.leave"), MessageManager.getInstance().getString("buildit.leave_item")).getIs();
        this.scoreboard = new BuildItScoreboardManager(arena.getName(), this);
        this.waitTime = ConfigManager.getInstance().getInt("buildit.waiting_time");
        this.buildTime = ConfigManager.getInstance().getInt("buildit.build_time");
        this.cutBuildTime = ConfigManager.getInstance().getInt("buildit.cut_build_time");
        this.showWordToBuilderTime = ConfigManager.getInstance().getInt("buildit.show_word_to_builder");
        this.showWordToPlayersTime = ConfigManager.getInstance().getInt("buildit.show_everyone_word_after_build");
        this.bannedBlocks = ConfigManager.getInstance().getStringList("buildit.banned_blocks");
        this.rounds = ConfigManager.getInstance().getInt("buildit.game_rounds");
        this.firstPoint = ConfigManager.getInstance().getInt("buildit.first_player");
        this.regularPoint = ConfigManager.getInstance().getInt("buildit.regular_points");
        this.blocksPlaced = new ArrayList<>();
        this.bannedBlocks = new ArrayList<>();
        this.builderOrder = new ArrayList<>();
        this.totalPlayers = new ArrayList<>();
        this.spectators = new ArrayList<>();
        this.gotWordCorrectly = new ArrayList<>();
        this.voteBuilderCheat = new ArrayList<>();
        this.playersStuff = new HashMap<>();
        this.currentTime = 30;
        this.builderCountIndex = 0;
        this.cutTime = false;
        this.runTaskTimer(Minigames.getInstance(), 0L, 20L);
    }

    // Actual game.
    @Override
    public void run(){
        //scoreboard.updateScoreboard();
        xpTime();
        switch(status){
            case WAIT: {
                waitArea();
                break;
            }
            case GAME_PICK_WORD: {
                pickWord();
                break;
            }
            case GAME_BUILD_WORD: {
                buildWord();
                break;
            }
            case GAME_DISPLAY_WORD: {
                displayWord();
                break;
            }
            case WINNER: {
                winners();
                break;
            }
        }
    }

    // Waiting for game to start.
    private void waitArea()
    {
        // Checking if min is met and if time is 0.
        if (totalPlayers.size() >= arena.getMinimum())
        {
            if ((currentTime % 10 == 0 || (currentTime <= 5)))
            {
                for (Player p : totalPlayers)
                {
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    MessageManager.getInstance().sendNumberMessage("buildit.starting_game", currentTime, p);
                }
            }
            --currentTime;

            // Once zero, start the game.
            if (currentTime <= 0)
            {
                sortOrder();
                spectators.clear();
                status = BuildItGameStatus.GAME_PICK_WORD;
                currentTime = showWordToBuilderTime;
                return;
            }
        }
        else
        {
            currentTime = waitTime;
        }
    }

    // Get the new builder.
    private void getNewBuilder()
    {

        // If starting for the first time.
        if (builder == null)
        {
            builder = builderOrder.get(builderCountIndex);
            for (Player p : totalPlayers)
            {
                if (!p.equals(builder))
                {
                    spectators.add(p);
                }
            }
        }
        else
        {

            // Get new builder and save old one.
            Player prevBuilder = builder;
            spectators.add(prevBuilder);
            builder = builderOrder.get(builderCountIndex);
            spectators.remove(builder);

            // Previous builder setup.
            prevBuilder.getInventory().clear();
            prevBuilder.getInventory().setItem(8, leaveItem);
            prevBuilder.updateInventory();
            prevBuilder.setGameMode(GameMode.ADVENTURE);
            prevBuilder.teleport(arena.getSpectators());
        }

        // Let everyone know who is building.
        for (Player p : totalPlayers)
        {
            MessageManager.getInstance().sendPlayerNameMessage("buildit.now_building", builder, p);
        }

        // Set the builder up for success.
        builder.setGameMode(GameMode.CREATIVE);
        builder.teleport(arena.getBuilderSpot());
        builder.getInventory();
        builder.updateInventory();
    }

    // Pick the builder word.
    private void pickWord()
    {

        // Setup
        if (currentTime == showWordToBuilderTime)
        {

            // Get new builder, clear cheats, and show word to them.
            getNewBuilder();
            voteBuilderCheat.clear();
            buildWord = BuildItWordManager.getInstance().getRandomWord();
            MessageManager.getInstance().sendStringMessage("buildit.builder_show_word", buildWord, builder);
        }

        // When countdown is less than 3.
        if (currentTime <= 3)
        {
            for (Player p : totalPlayers)
            {
                MessageManager.getInstance().sendNumberMessage("buildit.starting_build_time", currentTime, p);
                p.playSound(p.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
        }

        // Decrement and check if zero.
        --currentTime;
        if (currentTime <= 0)
        {
            status = BuildItGameStatus.GAME_BUILD_WORD;
            currentTime = buildTime;
            return;
        }
    }

    // Actual round.
    private void buildWord()
    {

        // Start the round.
        if (currentTime == buildTime)
        {
            for (Player p : totalPlayers)
            {
                p.playSound(p.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            }
            MessageManager.getInstance().sendMessage("buildit.build_start", builder);
        }

        // If someone got the word right, cut time.
        if ((!gotWordCorrectly.isEmpty()) && (currentTime > 15))
        {
            if (!cutTime)
            {
                cutTime = true;
                if (cutBuildTime != 0)
                {
                    currentTime = cutBuildTime + 1;
                }
            }
        }

        // When 10 increments or fewer than 5, play sounds and message to screen.
        if ((currentTime % 10 == 0) || (currentTime <= 5))
        {
            for (Player p : totalPlayers)
            {
                p.playSound(p.getLocation(),Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                MessageManager.getInstance().sendNumberMessage("buildit.build_time_left", currentTime, p);
            }
        }

        // Decrement time, and check if its 0 or if both sizes are the same.
        currentTime--;
        if ((currentTime <= 0) || (gotWordCorrectly.size() == spectators.size()))
        {
            cutTime = false;
            status = BuildItGameStatus.GAME_DISPLAY_WORD;
            currentTime = showWordToPlayersTime;
            return;
        }
    }

    // Display word when round concludes.
    private void displayWord()
    {

        // Show the word.
        if (currentTime == showWordToPlayersTime)
        {
            if (builder != null)
            {
                for (Player p : totalPlayers)
                {
                    // If everyone got it, don't bother showing word.
                    if (gotWordCorrectly.size() == spectators.size())
                    {
                        MessageManager.getInstance().sendMessage("buildit.everyone_got_it", p);
                    }
                    else
                    {
                        MessageManager.getInstance().sendStringMessage("buildit.show_word", buildWord, p);
                    }
                }
            }
            // If builder left.
            else
            {
                for (Player p : totalPlayers)
                {
                    MessageManager.getInstance().sendStringMessage("buildit.builder_left", buildWord, p);
                }
            }
        }

        --currentTime;
        if (currentTime <= 0)
        {
            for (Player p : totalPlayers)
            {
                p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
            }

            // Check for next builder.
            if (checkNextBuilder())
            {
                resetArena();
                status = BuildItGameStatus.GAME_PICK_WORD;
                currentTime = showWordToBuilderTime;
            }
            else
            {
                resetArena();
                status = BuildItGameStatus.WINNER;
                currentTime = winnerShowOffTime;
            }
            return;
        }
    }

    // Winner showcase.
    private void winners()
    {

    }

    // Sort the builder order.
    private void sortOrder()
    {
        Collections.shuffle(totalPlayers);
        builderOrder.clear();
        builderCountIndex = 0;
        for (int i = 0; i < rounds; i++)
        {
            for (Player p : totalPlayers)
            {
                builderOrder.add(p);
            }
        }
    }

    // Double check if round is nearly over.
    public boolean checkNextBuilder()
    {
        ++builderCountIndex;
        if (builderCountIndex >= builderOrder.size())
        {
            return false;
        }

        return true;
    }

    // Show xp in separate function.
    private void xpTime()
    {
        if (!ConfigManager.getInstance().getBoolean("buildit.exp_time_enable"))
        {
            return;
        }

        for (Player p : totalPlayers)
        {
            float xp = 0.0f;
            switch (status)
            {
                case WAIT:
                    xp = (float) currentTime / waitTime;
                    p.setLevel(currentTime);
                    p.setExp(xp);
                    break;

                case GAME_PICK_WORD:
                    xp = (float) currentTime / showWordToBuilderTime;
                    p.setLevel(currentTime);
                    p.setExp(xp);
                    break;

                case GAME_BUILD_WORD:
                    xp = (float) currentTime / buildTime;
                    p.setLevel(currentTime);
                    p.setExp(xp);
                    break;

                case GAME_DISPLAY_WORD:
                    xp = (float) currentTime / showWordToPlayersTime;
                    p.setLevel(currentTime);
                    p.setExp(xp);
                    break;
            }
        }
    }

    // If player got word correctly.
    private void setGotWordCorrectly(Player p)
    {
        p.playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);

        // If first, give extra.
        if (gotWordCorrectly.isEmpty() && (firstPoint != 0))
        {
            scoreboard.addPoint(p, firstPoint);
            MessageManager.getInstance().sendNumberMessage("buildit.got_word_correct", firstPoint, p);
        }
        else
        {
            scoreboard.addPoint(p, regularPoint);
            MessageManager.getInstance().sendNumberMessage("buildit.got_word_correct", regularPoint, p);
        }

        gotWordCorrectly.add(p);
    }

    // Reset arena back to default.
    private void resetArena()
    {
        blocksPlaced.forEach(location -> location.getBlock().setType(Material.AIR));
        blocksPlaced.clear();
    }

    // Add player to the game.
    public void addPlayer(Player p )
    {

        // If already in game.
        if (totalPlayers.contains(p))
        {
            MessageManager.getInstance().sendMessage("buildit.player_already_joined", p);
            return;
        }

        // If it's in wait stage.
        if (status == BuildItGameStatus.WAIT)
        {
            // Check if they are in another arena.
            if (!BuildItArenaManager.getInstance().isInAnotherArena(p))
            {
                MessageManager.getInstance().sendMessage("buildit.player_in_another_arena", p);
                return;
            }

            // If arena is maxed.
            if (totalPlayers.size() >= arena.getMaximum())
            {
                MessageManager.getInstance().sendMessage("buildit.arena_full", p);
                return;
            }

            // Announce to other players.
            for (Player player : totalPlayers)
            {
                player.playSound(p.getLocation(),Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                MessageManager.getInstance().sendPlayerNameMessage("buildit.player_join", p, player);
            }

            // Clear effects.
            for (PotionEffect effect : p.getActivePotionEffects())
            {
                p.removePotionEffect(effect.getType());
            }

            MessageManager.getInstance().sendStringMessage("buildit.player_success_join", arena.getName(), p);
            totalPlayers.add(p);
            playersStuff.put(p, new ItemStoreManager(p));
            p.teleport(arena.getLobby());
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            p.setGameMode(GameMode.ADVENTURE);
            p.getInventory().setItem(8, leaveItem);
            p.updateInventory();
            scoreboard.addPlayer(p);
        }
        else if (status == BuildItGameStatus.STOP)
        {
            MessageManager.getInstance().sendMessage("buildit.arena_is_disabled", p);
        }
        else
        {
            MessageManager.getInstance().sendMessage("buildit.arena_in-game", p);
        }
    }

    // Remove player from game.
    public void removePlayer(Player p)
    {
        ItemStoreManager ism = playersStuff.get(p);
        ism.giveBackItems();
        playersStuff.remove(p);
        scoreboard.removePlayer(p);
        if (totalPlayers.contains(p))
        {

            // If waiting.
            if (status == BuildItGameStatus.WAIT)
            {
                totalPlayers.remove(p);
                for (Player player : totalPlayers)
                {
                    MessageManager.getInstance().sendPlayerNameMessage("buildit.player_quit", p, player);
                }
                MessageManager.getInstance().sendStringMessage("buildit.player_success_quit", arena.getName(), p);
            }

            // Otherwise if game is running, get rid of them.
            else
            {
                MessageManager.getInstance().sendStringMessage("buildit.player_success_quit", arena.getName(), p);
                if (spectators.contains(p))
                {
                    spectators.remove(p);
                }
                if (builder.equals(p))
                {
                    builder = null;
                }
                for (int i = 0; i < rounds; i++)
                {
                    if (builderOrder.contains(p))
                    {
                        builderOrder.remove(p);
                    }
                }

                totalPlayers.remove(p);

                // If small enough, winner declared.
                if (totalPlayers.size() < arena.getMinimum())
                {
                    currentTime = winnerShowOffTime;
                    status = BuildItGameStatus.WINNER;
                    return;
                }
            }
        }
        else
        {
            MessageManager.getInstance().sendMessage("buildit.player_not_in_game", p);
        }
    }

    // Remove everyone.
    public void removeEverybody()
    {
        for (Player p : totalPlayers)
        {
            ItemStoreManager ism = playersStuff.get(p);
            ism.giveBackItems();
            scoreboard.removePlayer(p);
        }
        spectators.clear();
        totalPlayers.clear();
        playersStuff.clear();
    }

    // If they may be cheating.
    public void addCheatPoint(Player p)
    {
        if (voteBuilderCheat.contains(p))
        {
            return;
        }
        voteBuilderCheat.add(p);
        for (Player player : totalPlayers)
        {
            MessageManager.getInstance().sendMessage("builder_cheat_notify", player);
        }

        // If all players say the builder's cheating
        if (voteBuilderCheat.size() == spectators.size())
        {
            cutTime = false;
            status = BuildItGameStatus.GAME_PICK_WORD;
            currentTime = showWordToBuilderTime;
            for (Player player : totalPlayers)
            {
                MessageManager.getInstance().sendMessage("builder_kicked", player);
            }
        }
    }

    // If both clicks.
    private boolean bothClicks(PlayerInteractEvent event)
    {
        return (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK);
    }

    // If they are clicking the leave item.
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            ItemStack item = event.getItem();
            if (item != null)
            {
                if (item.equals(leaveItem) && bothClicks(event))
                {
                    event.setCancelled(true);
                    removePlayer(event.getPlayer());
                }
            }
        }
    }

    // Block breaking.
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            if (event.getPlayer().equals(builder) && status == BuildItGameStatus.GAME_BUILD_WORD)
            {
                // Double check if the block they are breaking is the one they placed.
                if (!blocksPlaced.contains(event.getBlock().getLocation()))
                {
                    event.setCancelled(true);
                }
                else
                {
                    blocksPlaced.remove(event.getBlock().getLocation());
                }
            }
            else {
                event.setCancelled(true);
            }
        }
    }

    // Block placing.
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            if (event.getPlayer().equals(builder) && (status == BuildItGameStatus.GAME_BUILD_WORD))
            {
                // Check if block placed is a banned block.
                for (String string : bannedBlocks)
                {
                    if (event.getBlock().getType().equals(Material.valueOf(string.toUpperCase())))
                    {
                        MessageManager.getInstance().sendMessage("buildit.cannot_place_block", builder);
                        event.setCancelled(true);
                    }
                }
                blocksPlaced.add(event.getBlock().getLocation());
            }
            else
            {
                event.setCancelled(true);
            }
        }
    }

    // When player leaves
    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            String command = event.getMessage();
            if (!(command.equalsIgnoreCase("/buildit leave")))
            {
                event.setCancelled(true);
            }
        }
    }

    // On item drop.
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    // Prevent item pickup.
    @EventHandler
    public void onItemPickup(PlayerPickupItemEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
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
            if (totalPlayers.contains(p))
            {
                event.setCancelled(true);
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
            if (totalPlayers.contains(p))
            {
                event.setCancelled(true);
            }
        }
    }

    // Prevent flying unless they are the builder.
    @EventHandler
    public void onPlayerFly(PlayerToggleFlightEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            if (spectators.contains(event.getPlayer()))
            {
                event.setCancelled(true);
            }
            else
            {
                double distance = event.getPlayer().getEyeLocation().distance(arena.getBuilderSpot());
                if (distance > 100)
                {
                    event.getPlayer().teleport(arena.getBuilderSpot());
                }
            }
        }
    }

    // Prevent pvp.
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Player)
        {
            Player p = (Player) event.getEntity();
            if (totalPlayers.contains(p))
            {
                event.setCancelled(true);
            }
        }
    }

    // When player leaves.
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            removePlayer(event.getPlayer());
        }
    }


    // Player chat.
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event)
    {
        if (totalPlayers.contains(event.getPlayer()))
        {
            event.setCancelled(true);
            Player sender = event.getPlayer();
            String message = event.getMessage();
            if (status == BuildItGameStatus.GAME_BUILD_WORD) {

                if (spectators.contains(event.getPlayer()))
                {
                    if (message.equalsIgnoreCase(buildWord))
                    {
                        if (!gotWordCorrectly.contains(sender))
                        {
                            setGotWordCorrectly(sender);
                        }
                        else
                        {
                            MessageManager.getInstance().sendMessage("buildit.already_got_word",sender);
                        }
                    }
                    else
                    {
                        for (Player p : totalPlayers)
                        {
                            MessageManager.getInstance().inGameMessage(sender, p, message);
                        }
                    }
                }
                else {
                    MessageManager.getInstance().sendMessage("buildit.builder_prevent_chat", sender);
                }
            }
            else
            {
                for (Player p : totalPlayers)
                {
                    MessageManager.getInstance().inGameMessage(sender, p, message);
                }
            }
        }
    }
}
