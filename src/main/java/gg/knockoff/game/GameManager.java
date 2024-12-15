package gg.knockoff.game;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.google.gson.JsonArray;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.block.BlockState;
import io.papermc.paper.entity.LookAnchor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

public class GameManager { //I honestly think this entire class could be optimised because of how long it is
    public List<PlayerData> playerDatas;
    public Teams teams;

    public static int SectionPlaceLocationX = 1000;
    public static int SectionPlaceLocationY = 0;
    public static int SectionPlaceLocationZ = 1000;
    public static int LastSectionPlaceLocationX = 1000;
    public static int LastSectionPlaceLocationY = 0;
    public static int LastSectionPlaceLocationZ = 1000;
    public ArrayList PlayerList = new ArrayList();
    public static String GameState = "game"; //can be "game" (game running), "end" (game ending)

    //Can be "solo" or "team"
    public static String GameType = "Solo";

    public static int Round = 0;
    public static int RoundCounter =0;

    public GameManager() {//Start of the game
        Bukkit.getServer().sendMessage(text("Starting Game! \n(Note: the server might lag slightly)"));
        GameState = "game";
        for (Entity e : Bukkit.getWorld("world").getEntities()) {
            if (e instanceof TextDisplay) {
                e.remove();
            }
        }
        if (Bukkit.getOnlinePlayers().size() > 13) {
            GameType = "duos";
        } else {
            GameType = "solo";
        }

        PlayerList.clear();
        teams = new Teams();
        KnockoffItem.SetupKnockoffItems();

        // Sets the target area to air to prevent previous game's sections to interfere with the current game
        // Could be optimised, Filling all this in 1 go and/or in larger spaces causes your server to most likely go out of memory or not respond for a good while
        // Plus this lags the server anyways
        Bukkit.getLogger().log(Level.INFO, "Making room for knockoff map, This may lag your server depending on how good it is");
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
        CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(1100, -30, 1100), BlockVector3.at(1000, 40, 1000));
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
            RandomPattern pat = new RandomPattern();
            BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());
            pat.add(air, 1);
            editSession.setBlocks((Region) selection, pat);
        }  catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
            e.printStackTrace();
        }

        MapManager.CopyRandomMapSection();
        MapManager.PlaceCurrentlySelectedSection();
        new BukkitRunnable() {
            @Override
            public void run() {
                playerDatas = new ArrayList<PlayerData>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    PlayerData p = new PlayerData(player);
                    playerDatas.add(p);
                }
            }
        }.runTaskLater(knockoff.getInstance(), 1);

        SetupFirstSpawns();

        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerList.add(p.getName());
            WorldBorder PlayerBorder = Bukkit.getServer().createWorldBorder();
            p.setWorldBorder(PlayerBorder);
            PlayerBorder.setCenter(p.getX() + 0.5, p.getZ() + 0.5);
            PlayerBorder.setSize(3);

            GiveTeamItems(p);
            p.setGameMode(GameMode.SURVIVAL);
            ScoreboardManager.SetPlayerScoreboard(p);
            Teams.SetPlayerDisplayNames(p);
            CustomPlayerNametags.CustomPlayerNametags(p);

            p.setSneaking(true);
            p.setSneaking(false);
        }
        TeamStatus.Init();
        GameManager.Round = 1;
        GameManager.RoundCounter = 30;

        new BukkitRunnable() {
            int timer = 0;
            @Override
            public void run() {
                switch (timer) {
                    case 7:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.showTitle(Title.title(text("Go!").color(NamedTextColor.GOLD), text(" "),
                                    Title.Times.times(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofSeconds(1))));
                            player.playSound(player, "crystalized:effect.countdown_end", 50, 1);
                            player.getWorldBorder().reset();
                        }
                        StartGameLoop();
                        cancel();
                        break;
                    case 6:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.showTitle(Title.title(text("Starting in:").color(NamedTextColor.GREEN), text("3 2 ").color(NamedTextColor.GRAY)
                                            .append(Component.text("1").color(NamedTextColor.RED))
                                    ,Title.Times.times(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofSeconds(1))));
                            player.playSound(player, "crystalized:effect.countdown", 50, 1);
                        }
                        break;
                    case 5:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.showTitle(Title.title(text("Starting in:").color(NamedTextColor.GREEN), text("3").color(NamedTextColor.GRAY)
                                            .append(Component.text(" 2").color(NamedTextColor.RED))
                                            .append(Component.text(" 1").color(NamedTextColor.GRAY))
                                    ,Title.Times.times(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofSeconds(1))));
                            player.playSound(player, "crystalized:effect.countdown", 50, 1);
                        }
                        break;
                    case 4:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.showTitle(Title.title(text("Starting in:").color(NamedTextColor.GREEN), text("3").color(NamedTextColor.RED)
                                            .append(Component.text(" 2 1").color(NamedTextColor.GRAY))
                                    ,Title.Times.times(Duration.ofMillis(0), Duration.ofSeconds(1), Duration.ofSeconds(1))));
                            player.playSound(player, "crystalized:effect.countdown", 50, 1);
                        }
                        break;
                }
                timer++;
            }
        }.runTaskTimer(knockoff.getInstance(), 1 ,20);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (knockoff.getInstance().GameManager == null) {cancel();}
                    if (knockoff.getInstance().GameManager != null) {
                        TabMenu.SendTabMenu(p);
                    }
                }
            }
        }.runTaskTimer(knockoff.getInstance(), 1 ,5);

    }

    private void StartGameLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (knockoff.getInstance().GameManager == null) {cancel();}
                if (knockoff.getInstance().DevMode) {
                    Round = 0;
                    RoundCounter = 0;
                    cancel();
                }

                GameManager.RoundCounter--;
                if (GameManager.RoundCounter == 30 && GameManager.GameState.equals("game")) {
                    SpawnRandomPowerup();
                }
                if (GameManager.RoundCounter == 0 && GameManager.GameState.equals("game")) {
                    GameManager.CloneNewMapSection();
                    RoundCounter = 60;
                    Round++;
                }
            }
        }.runTaskTimer(knockoff.getInstance(), 0 ,20);


        new BukkitRunnable() {
            @Override
            public void run() {
                if (knockoff.getInstance().DevMode) {
                    //This line provides debug info on the current map section
                    Bukkit.getServer().sendActionBar(Component.text("[Debugging] Section data " + knockoff.getInstance().mapdata.currentsection + ". X:" + knockoff.getInstance().mapdata.getCurrentXLength() + ". Y:" + knockoff.getInstance().mapdata.getCurrentYLength() + ". Z:" + knockoff.getInstance().mapdata.getCurrentZLength() + ". MX:" + knockoff.getInstance().mapdata.getCurrentMiddleXLength() + ". MY:" + knockoff.getInstance().mapdata.getCurrentMiddleYLength() + ". MZ:" + knockoff.getInstance().mapdata.getCurrentMiddleZLength()));

                    //This gives team info
                    //Bukkit.getServer().sendActionBar(Component.text("Team Stats: " + TeamStatus.TeamsList));

                }
                //Should stop this bukkitrunnable once the game ends
                if (knockoff.getInstance().GameManager == null) {cancel();}

                for (Player p : Bukkit.getOnlinePlayers()) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        player.unlistPlayer(p);
                    }

                    PlayerData pd = knockoff.getInstance().GameManager.getPlayerData(p);
                    if (!knockoff.getInstance().DevMode && !pd.isPlayerDead && GameState.equals("game")) {
                        p.getPlayer().sendActionBar(text("" + pd.getDamagepercentage() + "%"));
                    }
                    if (p.getLocation().getY() < -30 && p.getGameMode().equals(GameMode.SURVIVAL)) {//instantly kills the player when they get knocked into the void
                        Location loc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentMiddleXLength(), knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 10, knockoff.getInstance().mapdata.getCurrentMiddleZLength());
                        p.teleport(loc);
                        p.setHealth(0);

                    }
                }
                for (Entity e : Bukkit.getWorld("world").getEntities()) {
                    if (e instanceof Item) {
                        if (((Item) e).getItemStack().getType().equals(Material.COAL) || ((Item) e).getItemStack().getType().equals(Material.WIND_CHARGE)) {
                            //do nothing, material.coal and wind charges is powerups so we dont clear them
                        } else {
                            e.remove();
                        }
                    }
                }
            }
        }.runTaskTimer(knockoff.getInstance(), 0 ,1);
    }

    public static void StartEndGame(String WinningTeam) {
        GameState = "end";
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (GameManager.GameType.equals("solo")) {
                switch (WinningTeam) {
                    case "blue" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.blue.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "cyan" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.cyan.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "green" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.green.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "lemon" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.lemon.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "lime" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.lime.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "magenta" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.magenta.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "orange" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.orange.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "peach" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.peach.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "purple" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.purple.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "red" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.red.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "white" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.white.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                    case "yellow" -> {
                        Player LastPlayer = Bukkit.getPlayer(Teams.yellow.getFirst());
                        player.showTitle(Title.title(text("").append(LastPlayer.displayName()), translatable("crystalized.game.knockoff.win").color(NamedTextColor.YELLOW), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
                    }
                }
            } else {
                player.showTitle(Title.title(text("teamed win"), text("placeholder text"), Title.Times.times(Duration.ofMillis(250), Duration.ofSeconds(5), Duration.ofMillis(1000))));
            }
            player.playSound(player, "minecraft:ui.toast.challenge_complete", 50, 1); //TODO placeholder sound
        }
        new BukkitRunnable() {
            int timer = 0;

            @Override
            public void run() {
                switch (timer) {
                    case 8, 9, 10:
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.playSound(player, "minecraft:block.note_block.hat", SoundCategory.MASTER,50, 1); //TODO placeholder sound
                        }
                        break;
                    case 11:
                        ForceEndGame();
                        cancel();
                        break;
                }
                timer++;
            }
        }.runTaskTimer(knockoff.getInstance(), 20,20);
    }

    public static void ForceEndGame() {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/world \"world\"");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 " + SectionPlaceLocationX + "," + SectionPlaceLocationY + "," + SectionPlaceLocationZ);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos2 " + knockoff.getInstance().mapdata.getCurrentXLength() + "," + knockoff.getInstance().mapdata.getCurrentYLength() + "," + knockoff.getInstance().mapdata.getCurrentZLength());
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/set air");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 " + LastSectionPlaceLocationX + "," + LastSectionPlaceLocationY + "," + LastSectionPlaceLocationZ);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos2 " + (LastSectionPlaceLocationX + MapManager.LastXLength) + "," + (LastSectionPlaceLocationY+ MapManager.LastYLength) + "," + (LastSectionPlaceLocationZ + MapManager.LastZLength));
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/set air");
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.kick(Component.text("Game over, thanks for playing!").color(NamedTextColor.RED));
        }
        for (Entity e : Bukkit.getWorld("world").getEntities()) {
            if (e instanceof TextDisplay) {
                e.remove();
            }
        }

        SectionPlaceLocationX = 1000;
        SectionPlaceLocationY = 0;
        SectionPlaceLocationZ = 1000;
        knockoff.getInstance().GameManager.teams = null;
        knockoff.getInstance().GameManager = null;
    }

    public static void GiveTeamItems(Player player) {
        ItemStack item = new ItemStack(Material.AMETHYST_BLOCK, 64);
        ItemMeta im = item.getItemMeta();
        PlayerInventory inv = player.getInventory();

        //for debugging
        //Bukkit.getLogger().log(Level.INFO, "[GAMEMANAGER] Player " + player.getName() + "Is in Team " + Teams.GetPlayerTeam(player));

        if (Teams.GetPlayerTeam(player).equals("blue")) {
            im.setCustomModelData(1);
            inv.setChestplate(colorArmor(Color.fromRGB(0x0A42BB), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0x0A42BB), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0x0A42BB), new ItemStack(Material.LEATHER_BOOTS)));

        } else if (Teams.GetPlayerTeam(player).equals("cyan")) {
            im.setCustomModelData(2);
            inv.setChestplate(colorArmor(Color.fromRGB(0x157D91), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0x157D91), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0x157D91), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("green")) {
            im.setCustomModelData(3);
            inv.setChestplate(colorArmor(Color.fromRGB(0x0A971E), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0x0A971E), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0x0A971E), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("lemon")) {
            im.setCustomModelData(4);
            inv.setChestplate(colorArmor(Color.fromRGB(0xFFC500), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0xFFC500), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0xFFC500), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("lime")) {
            im.setCustomModelData(5);
            inv.setChestplate(colorArmor(Color.fromRGB(0x67E555), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0x67E555), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0x67E555), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("magenta")) {
            im.setCustomModelData(6);
            inv.setChestplate(colorArmor(Color.fromRGB(0xDA50E0), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0xDA50E0), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0xDA50E0), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("orange")) {
            im.setCustomModelData(7);
            inv.setChestplate(colorArmor(Color.fromRGB(0xFF7900), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0xFF7900), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0xFF7900), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("peach")) {
            im.setCustomModelData(8);
            inv.setChestplate(colorArmor(Color.fromRGB(0xFF8775), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0xFF8775), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0xFF8775), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("purple")) {
            im.setCustomModelData(9);
            inv.setChestplate(colorArmor(Color.fromRGB(0x7525DC), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0x7525DC), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0x7525DC), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("red")) {
            im.setCustomModelData(10);
            inv.setChestplate(colorArmor(Color.fromRGB(0xF74036), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0xF74036), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0xF74036), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("white")) {
            im.setCustomModelData(11);
            inv.setChestplate(colorArmor(Color.fromRGB(0xFFFFFF), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0xFFFFFF), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0xFFFFFF), new ItemStack(Material.LEATHER_BOOTS)));

        } else
        if (Teams.GetPlayerTeam(player).equals("yellow")) {
            im.setCustomModelData(12);
            inv.setChestplate(colorArmor(Color.fromRGB(0xFBE059), new ItemStack(Material.LEATHER_CHESTPLATE)));
            inv.setLeggings(colorArmor(Color.fromRGB(0xFBE059), new ItemStack(Material.LEATHER_LEGGINGS)));
            inv.setBoots(colorArmor(Color.fromRGB(0xFBE059), new ItemStack(Material.LEATHER_BOOTS)));

        }

        im.itemName(Component.translatable("crystalized.item.nexusblock.name"));
        List<Component> lore = new ArrayList<>();
        lore.add(Component.translatable("crystalized.item.nexusblock.desc").color(NamedTextColor.DARK_GRAY));
        lore.add(Component.translatable("crystalized.item.nexusblock.desc2").color(NamedTextColor.DARK_GRAY));
        im.lore(lore);
        item.setItemMeta(im);
        player.getInventory().addItem(item);
    }

    private static ItemStack colorArmor(Color c, ItemStack i) {
        LeatherArmorMeta lam = (LeatherArmorMeta) i.getItemMeta();
        lam.setColor(c);
        lam.setUnbreakable(true);
        i.setItemMeta(lam);
        return i;
    }

    public PlayerData getPlayerData(Player p) {
        for (PlayerData pd : playerDatas) {
            if (pd.player.equals(p.getName())) {
                return pd;
            }
        }
        Bukkit.getServer().sendMessage(text("error occured, a player didnt have associated data"));
        Bukkit.getLogger().warning("player name: " + p.getName());

        for (PlayerData pd : playerDatas) {
            Bukkit.getLogger().warning(pd.player);
        }

        return null;
    }

    @SuppressWarnings("deprication") //FAWE has deprecation notices from WorldEdit that's printed in console when compiled
    private static void SetupFirstSpawns() {
        //TODO idk how to change the direction of glazed terracotta blocks, so regular amethyst crystals are used instead
        if (!Teams.blue.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(SectionPlaceLocationX + 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 5),
                    BlockVector3.at(SectionPlaceLocationX + 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.BLUE_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.cyan.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 5),
                    BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.CYAN_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.green.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 5),
                    BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.GREEN_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.lemon.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(SectionPlaceLocationX + 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 5),
                    BlockVector3.at(SectionPlaceLocationX + 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.YELLOW_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.lime.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(SectionPlaceLocationX + 15, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 5),
                    BlockVector3.at(SectionPlaceLocationX + 17, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.LIME_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.magenta.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 15, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 5),
                    BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 17, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.MAGENTA_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.orange.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 15, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 5),
                    BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 17, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.ORANGE_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.peach.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(SectionPlaceLocationX + 15, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 5),
                    BlockVector3.at(SectionPlaceLocationX + 17, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 7));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.PINK_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.purple.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(SectionPlaceLocationX + 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 15),
                    BlockVector3.at(SectionPlaceLocationX + 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 17));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.PURPLE_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.red.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 15),
                    BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 17));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.RED_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.white.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 15),
                    BlockVector3.at(knockoff.getInstance().mapdata.getCurrentXLength() - 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), SectionPlaceLocationZ + 17));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.WHITE_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }
        if (!Teams.yellow.isEmpty()) {
            com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
            CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(SectionPlaceLocationX + 5, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 15),
                    BlockVector3.at(SectionPlaceLocationX + 7, knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentZLength() - 17));
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                RandomPattern pat = new RandomPattern();
                BlockState a = BukkitAdapter.adapt(Material.YELLOW_STAINED_GLASS.createBlockData());
                pat.add(a, 1);
                editSession.setBlocks((Region) selection, pat);
            }  catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                e.printStackTrace();
            }
        }


        for (Player p : Bukkit.getOnlinePlayers()) {
            if (Teams.GetPlayerTeam(p).equals("blue")) {
                Location blueloc = new Location(Bukkit.getWorld("world"), SectionPlaceLocationX + 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, SectionPlaceLocationZ + 6);
                p.teleport(blueloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("cyan")) {
                Location cyanloc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentXLength() - 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, knockoff.getInstance().mapdata.getCurrentZLength() - 6);
                p.teleport(cyanloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("green")) {
                Location greenloc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentXLength() - 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, SectionPlaceLocationZ + 6);
                p.teleport(greenloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("lemon")) {
                Location greenloc = new Location(Bukkit.getWorld("world"), SectionPlaceLocationX + 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, knockoff.getInstance().mapdata.getCurrentZLength() - 6);
                p.teleport(greenloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("lime")) { //Yes im aware this has blueloc as its variable, I copy pasted the first 4 lol
                Location blueloc = new Location(Bukkit.getWorld("world"), SectionPlaceLocationX + 16, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, SectionPlaceLocationZ + 6);
                p.teleport(blueloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("magenta")) {
                Location cyanloc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentXLength() - 16, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, knockoff.getInstance().mapdata.getCurrentZLength() - 6);
                p.teleport(cyanloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("orange")) {
                Location greenloc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentXLength() - 16, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, SectionPlaceLocationZ + 6);
                p.teleport(greenloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("peach")) {
                Location greenloc = new Location(Bukkit.getWorld("world"), SectionPlaceLocationX + 16, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, knockoff.getInstance().mapdata.getCurrentZLength() - 6);
                p.teleport(greenloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("purple")) {
                Location blueloc = new Location(Bukkit.getWorld("world"), SectionPlaceLocationX + 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, SectionPlaceLocationZ + 16);
                p.teleport(blueloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("red")) {
                Location cyanloc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentXLength() - 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, knockoff.getInstance().mapdata.getCurrentZLength() - 16);
                p.teleport(cyanloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("white")) {
                Location greenloc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentXLength() - 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, SectionPlaceLocationZ + 16);
                p.teleport(greenloc);
            } else
            if (Teams.GetPlayerTeam(p).equals("yellow")) {
                Location greenloc = new Location(Bukkit.getWorld("world"), SectionPlaceLocationX + 6, knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 3, knockoff.getInstance().mapdata.getCurrentZLength() - 16);
                p.teleport(greenloc);
            } else {
                Location loc = new Location(Bukkit.getWorld("world"), knockoff.getInstance().mapdata.getCurrentMiddleXLength(), knockoff.getInstance().mapdata.getCurrentMiddleYLength() + 10, knockoff.getInstance().mapdata.getCurrentMiddleZLength());
                p.teleport(loc);
            }
            p.lookAt(knockoff.getInstance().mapdata.getCurrentMiddleXLength(), knockoff.getInstance().mapdata.getCurrentMiddleYLength(), knockoff.getInstance().mapdata.getCurrentMiddleZLength(), LookAnchor.EYES);
        }
    }

    public static void CloneNewMapSection() {
        MapManager.CloneNewMapSection();
    }

    private static void SpawnRandomPowerup() {
        boolean IsValidSpot = false;
        Location blockloc = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        while (!IsValidSpot) {
            blockloc = new Location(Bukkit.getWorld("world"),
                    knockoff.getInstance().getRandomNumber(GameManager.SectionPlaceLocationX, knockoff.getInstance().mapdata.getCurrentXLength()) + 0.5,
                    knockoff.getInstance().getRandomNumber(GameManager.SectionPlaceLocationY, knockoff.getInstance().mapdata.getCurrentYLength()),
                    knockoff.getInstance().getRandomNumber(GameManager.SectionPlaceLocationZ, knockoff.getInstance().mapdata.getCurrentZLength()) + 0.5
            );
            if (blockloc.getBlock().getType().equals(Material.AIR)) {
                IsValidSpot = false;
            } else {
                IsValidSpot = true;
            }
        }
        DropPowerup.DropPowerup(new Location(Bukkit.getWorld("world"), blockloc.getBlockX(), blockloc.getBlockY() + 3, blockloc.getBlockZ()),
                KnockoffItem.ItemList.get(
                        knockoff.getInstance().getRandomNumber(0, KnockoffItem.ItemList.size())
                ).toString()
        );
        Bukkit.getServer().sendMessage(Component.text("---------------------------------\n\n[!] ")
                .append(Component.translatable("crystalized.game.knockoff.chat.powerup"))
                .append(Component.text("\n\n---------------------------------"))
        );
        Bukkit.getServer().sendMessage(Component.text("[DEBUG] Coords: X:" + blockloc.getBlockX() + " Y:" + blockloc.getBlockY() + " Z:" + blockloc.getBlockZ()));
    }
}

class MapManager {
    public static int LastXLength = 0;
    public static int LastYLength = 0;
    public static int LastZLength = 0;
    static String MoveDir = "";

    public static void CloneNewMapSection() {
        GameManager.LastSectionPlaceLocationX = GameManager.SectionPlaceLocationX;
        GameManager.LastSectionPlaceLocationY = GameManager.SectionPlaceLocationY;
        GameManager.LastSectionPlaceLocationZ = GameManager.SectionPlaceLocationZ;
        LastXLength = knockoff.getInstance().mapdata.CurrentXLength;
        LastYLength = knockoff.getInstance().mapdata.CurrentYLength;
        LastZLength = knockoff.getInstance().mapdata.CurrentZLength;
        Bukkit.getServer().sendMessage(Component.translatable("crystalized.game.knockoff.chat.movetosafety1").color(NamedTextColor.GOLD)
                .append(Component.translatable("crystalized.game.knockoff.chat.movetosafety2").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true))
        );
        CopyRandomMapSection();



        switch (knockoff.getInstance().getRandomNumber(1, 3)) {
            case 1, 2:
                MoveDir = "EAST";
                GameManager.SectionPlaceLocationX = GameManager.LastSectionPlaceLocationX + LastXLength;
                GameManager.SectionPlaceLocationZ = GameManager.LastSectionPlaceLocationZ;
                break;
            /*case 2: //TODO
                MoveDir = "SOUTH";
                break;*/
            case 3:
                MoveDir = "WEST";
                GameManager.SectionPlaceLocationX = GameManager.LastSectionPlaceLocationX - knockoff.getInstance().mapdata.CurrentXLength;
                GameManager.SectionPlaceLocationZ = GameManager.LastSectionPlaceLocationZ;
                break;
        }


        PlaceCurrentlySelectedSection();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(Title.title(text(""), Component.translatable("crystalized.game.knockoff.chat.movetosafety2").color(NamedTextColor.RED), Title.Times.times(Duration.ofMillis(100), Duration.ofSeconds(4), Duration.ofMillis(500))));
            //
        }

        //TODO Temporary sound effect. For Map Movement
        new BukkitRunnable() {
            int timer = 0;
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    switch (timer) {
                        case 1, 3:
                            player.playSound(player, "minecraft:block.note_block.chime", 50, 1);
                            break;
                        case 2:
                            player.playSound(player, "minecraft:block.note_block.chime", 50, 1);
                            player.playSound(player, "minecraft:block.conduit.ambient", 50, 1);
                            break;
                        case 4:
                            cancel();
                            break;
                    }
                }
                timer++;
            }
        }.runTaskTimer(knockoff.getInstance(), 0, 10);
        DecayMapSection();
    }
    public static void DecayMapSection() {
        //WorldEdit/FAWE API documentation is ass, gl understanding this


        //Turning map into Crystals
        new BukkitRunnable() {
            int XPos = 0;

            @Override
            public void run() {
                if (knockoff.getInstance().GameManager == null) {cancel();}
                switch (MoveDir) {
                    case "EAST" -> {
                        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
                        if ((GameManager.LastSectionPlaceLocationX + XPos) == (GameManager.LastSectionPlaceLocationX + LastXLength + 1)) {
                            cancel();
                        } else {
                            try (EditSession editSession = com.fastasyncworldedit.core.Fawe.instance().getWorldEdit().newEditSession((com.sk89q.worldedit.world.World) world)) {
                                Region region = new CuboidRegion(
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + XPos,
                                                GameManager.LastSectionPlaceLocationY,
                                                GameManager.LastSectionPlaceLocationZ
                                        ),
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + XPos,
                                                GameManager.LastSectionPlaceLocationY + LastYLength,
                                                GameManager.LastSectionPlaceLocationZ + LastZLength
                                        )
                                );
                                //Mask mask = new BlockMask(editSession.getExtent(), new BaseBlock(BlockTypes.AIR));
                                ExistingBlockMask mask = new ExistingBlockMask(editSession.getExtent());
                                RandomPattern pat = new RandomPattern();
                                BlockState a = BukkitAdapter.adapt(Material.AMETHYST_BLOCK.createBlockData());
                                pat.add(a, 1);
                                editSession.replaceBlocks(region, mask, pat);
                                editSession.flushQueue();
                            } catch (Exception e) {
                                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                                e.printStackTrace();
                            }
                            XPos++;
                        }
                    }
                    case "SOUTH" -> {

                    }
                    case "WEST" -> {
                        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
                        if ((GameManager.LastSectionPlaceLocationX + XPos) == (GameManager.LastSectionPlaceLocationX + LastXLength + 1)) {
                            cancel();
                        } else {
                            try (EditSession editSession = com.fastasyncworldedit.core.Fawe.instance().getWorldEdit().newEditSession((com.sk89q.worldedit.world.World) world)) {
                                Region region = new CuboidRegion(
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + LastXLength - XPos,
                                                GameManager.LastSectionPlaceLocationY,
                                                GameManager.LastSectionPlaceLocationZ
                                        ),
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + LastXLength - XPos,
                                                GameManager.LastSectionPlaceLocationY + LastYLength,
                                                GameManager.LastSectionPlaceLocationZ + LastZLength
                                        )
                                );
                                //Mask mask = new BlockMask(editSession.getExtent(), new BaseBlock(BlockTypes.AIR));
                                ExistingBlockMask mask = new ExistingBlockMask(editSession.getExtent());
                                RandomPattern pat = new RandomPattern();
                                BlockState a = BukkitAdapter.adapt(Material.AMETHYST_BLOCK.createBlockData());
                                pat.add(a, 1);
                                editSession.replaceBlocks(region, mask, pat);
                                editSession.flushQueue();
                            } catch (Exception e) {
                                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                                e.printStackTrace();
                            }
                            XPos++;
                        }
                    }
                }
            }
        }.runTaskTimer(knockoff.getInstance(), 0, 10);

        //Filling crystals with air, this has a delay compared to the previous BukkitRunnable
        //This is literally copy pasted code but with the material changed to AIR
        new BukkitRunnable() {
            int XPos = 0;

            @Override
            public void run() {
                if (knockoff.getInstance().GameManager == null) {cancel();}
                switch (MoveDir) {
                    case "EAST" -> {
                        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
                        if ((GameManager.LastSectionPlaceLocationX + XPos) == (GameManager.LastSectionPlaceLocationX + LastXLength + 1)) {
                            //TODO, Clear area
                            cancel();
                        } else {
                            try (EditSession editSession = com.fastasyncworldedit.core.Fawe.instance().getWorldEdit().newEditSession((com.sk89q.worldedit.world.World) world)) {
                                Region region = new CuboidRegion(
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + XPos,
                                                GameManager.LastSectionPlaceLocationY,
                                                GameManager.LastSectionPlaceLocationZ
                                        ),
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + XPos - 5,
                                                GameManager.LastSectionPlaceLocationY + LastYLength,
                                                GameManager.LastSectionPlaceLocationZ + LastZLength
                                        )
                                );
                                //Mask mask = new BlockMask(editSession.getExtent(), new BaseBlock(BlockTypes.AIR));
                                ExistingBlockMask mask = new ExistingBlockMask(editSession.getExtent());
                                RandomPattern pat = new RandomPattern();
                                BlockState a = BukkitAdapter.adapt(Material.AIR.createBlockData());
                                pat.add(a, 1);
                                editSession.replaceBlocks(region, mask, pat);
                                editSession.flushQueue();
                            } catch (Exception e) {
                                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                                e.printStackTrace();
                            }
                            XPos++;
                        }
                    }
                    case "SOUTH" -> {

                    }
                    case "WEST" -> {
                        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
                        if ((GameManager.LastSectionPlaceLocationX + XPos) == (GameManager.LastSectionPlaceLocationX + LastXLength + 1)) {
                            cancel();
                        } else {
                            try (EditSession editSession = com.fastasyncworldedit.core.Fawe.instance().getWorldEdit().newEditSession((com.sk89q.worldedit.world.World) world)) {
                                Region region = new CuboidRegion(
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + LastXLength - XPos,
                                                GameManager.LastSectionPlaceLocationY,
                                                GameManager.LastSectionPlaceLocationZ
                                        ),
                                        BlockVector3.at(
                                                GameManager.LastSectionPlaceLocationX + LastXLength - XPos + 5,
                                                GameManager.LastSectionPlaceLocationY + LastYLength,
                                                GameManager.LastSectionPlaceLocationZ + LastZLength
                                        )
                                );
                                //Mask mask = new BlockMask(editSession.getExtent(), new BaseBlock(BlockTypes.AIR));
                                ExistingBlockMask mask = new ExistingBlockMask(editSession.getExtent());
                                RandomPattern pat = new RandomPattern();
                                BlockState a = BukkitAdapter.adapt(Material.AIR.createBlockData());
                                pat.add(a, 1);
                                editSession.replaceBlocks(region, mask, pat);
                                editSession.flushQueue();
                            } catch (Exception e) {
                                Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
                                e.printStackTrace();
                            }
                            XPos++;
                        }
                    }
                }
            }
        }.runTaskTimer(knockoff.getInstance(), 60, 10);
    }

    public static void CopyRandomMapSection() {
        knockoff.getInstance().mapdata.getrandommapsection();
    }

    public static void PlaceCurrentlySelectedSection() {
        JsonArray data = knockoff.getInstance().mapdata.getCurrentsection();
        World world = Bukkit.getWorld("world");
        try (EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {
            CuboidRegion region = new CuboidRegion(BukkitAdapter.adapt(world), BlockVector3.at(data.get(1).getAsInt(), data.get(2).getAsInt(), data.get(3).getAsInt()), BlockVector3.at(data.get(4).getAsInt(), data.get(5).getAsInt(), data.get(6).getAsInt()));
            BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

            ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(
                    BukkitAdapter.adapt(world), region, clipboard, region.getMinimumPoint()
            );
            Operations.complete(forwardExtentCopy);

            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(GameManager.SectionPlaceLocationX, GameManager.SectionPlaceLocationY, GameManager.SectionPlaceLocationZ))
                    .build();
            Operations.complete(operation);
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
            e.printStackTrace();
        }
        if (!knockoff.getInstance().DevMode) {
            //Could be optimised, this needs to use FAWE's API, but we're using commands instead since idk how the api works for this
            String a = knockoff.getInstance().mapdata.getCurrentsection().get(7).getAsString().toLowerCase();
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/world \"world\"");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos1 " + GameManager.SectionPlaceLocationX + "," + GameManager.SectionPlaceLocationY + "," + GameManager.SectionPlaceLocationZ);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/pos2 " + knockoff.getInstance().mapdata.getCurrentXLength() + "," + knockoff.getInstance().mapdata.getCurrentYLength() + "," + knockoff.getInstance().mapdata.getCurrentZLength());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/replace " + a + " air");
        }
    }
}

class TabMenu {
    //static String StatsPlayerList = "";
    static Component StatsPlayerList = text("");

    public static void SendTabMenu(Player p) {
        StatsPlayerList = text("");

        //Header
        p.sendPlayerListHeader(
                text("\n")
                        .append(text("Crystalized: ").color(NamedTextColor.LIGHT_PURPLE).append(text("KnockOff (Work in Progress)").color(NamedTextColor.GOLD)))
                        .append(text("\n"))
        );

        //Footer
        // Could be optimised too
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData pd = knockoff.getInstance().GameManager.getPlayerData(player);
            if (pd.isOnline) {
                if (pd.isPlayerDead) {
                    if (pd.isEliminated) {
                        StatsPlayerList = text("")
                                .append(StatsPlayerList)
                                .append(text("\n [Eliminated] "))
                                .append(player.displayName())
                                .append(text(" \uE101 ")
                                .append(text(pd.getKills()))
                                .append(text(" \uE103 "))
                                .append(text(pd.getDeaths()))
                            );
                    } else {
                        StatsPlayerList = text("")
                                .append(StatsPlayerList)
                                .append(text("\n [Dead] "))
                                .append(player.displayName())
                                .append(text(" \uE101 ")
                                .append(text(pd.getKills()))
                                .append(text(" \uE103 "))
                                .append(text(pd.getDeaths()))
                            );
                    }
                } else {
                    StatsPlayerList = text("")
                            .append(StatsPlayerList)
                            .append(text("\n [Alive] "))
                            .append(player.displayName())
                            .append(text(" \uE101 ")
                            .append(text(pd.getKills()))
                            .append(text(" \uE103 "))
                            .append(text(pd.getDeaths()))
                    );
                }
            } else {
                StatsPlayerList = text("")
                        .append(StatsPlayerList)
                        .append(text("\n [Disconnected] "))
                        .append(player.displayName())
                        .append(text(" \uE101 ")
                        .append(text(pd.getKills()))
                        .append(text(" \uE103 "))
                        .append(text(pd.getDeaths()))
                    );
            }
        }


        p.sendPlayerListFooter(text("")
                .append(text("---------------------------------------------------").color(NamedTextColor.GRAY)
                .append(StatsPlayerList).color(NamedTextColor.WHITE)
                .append(text("\n---------------------------------------------------\n\n").color(NamedTextColor.GRAY))
                .append(text("Knockoff Version: " + knockoff.getInstance().getDescription().getVersion()).color(NamedTextColor.DARK_GRAY)))
        );
    }
}

class KnockoffProtocolLib {

    public static PacketAdapter make_allys_glow() {
        return new PacketAdapter(knockoff.getInstance(), PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                GameManager gc = knockoff.getInstance().GameManager;
                PacketContainer packet = event.getPacket();
                Player updated_player = get_player_by_entity_id(packet.getIntegers().read(0));
                if (gc == null
                        || updated_player == null
                        || Teams.GetPlayerTeam(updated_player) != Teams.GetPlayerTeam(event.getPlayer())) {
                    return;
                }
                event.setPacket(packet = packet.deepClone());
                List<WrappedDataValue> wrappedData = packet.getDataValueCollectionModifier().read(0);
                for (WrappedDataValue wdv : wrappedData) {
                    if (wdv.getIndex() == 0) {
                        byte b = (byte) wdv.getValue();
                        b |= 0b01000000;
                        wdv.setValue(b);
                    }
                }
            }
        };
    }

    private static Player get_player_by_entity_id(int id) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getEntityId() == id) {
                return player;
            }
        }
        return null;
    }
}