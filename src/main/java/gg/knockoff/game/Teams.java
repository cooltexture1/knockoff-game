package gg.knockoff.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.logging.Level;

public class Teams {

    public static List<String> blue = new ArrayList<>();
    public static List<String> cyan = new ArrayList<>();
    public static List<String> green = new ArrayList<>();
    public static List<String> lemon = new ArrayList<>();
    public static List<String> lime = new ArrayList<>();
    public static List<String> magenta = new ArrayList<>();
    public static List<String> orange = new ArrayList<>();
    public static List<String> peach = new ArrayList<>();
    public static List<String> purple = new ArrayList<>();
    public static List<String> red = new ArrayList<>();
    public static List<String> white = new ArrayList<>();
    public static List<String> yellow = new ArrayList<>();

    public static final TextColor TEAM_BLUE = TextColor.color(0x0A42BB);
    public static final TextColor TEAM_CYAN = TextColor.color(0x157D91);
    public static final TextColor TEAM_GREEN = TextColor.color(0x0A971E);
    public static final TextColor TEAM_LEMON = TextColor.color(0xFFC500);
    public static final TextColor TEAM_LIME = TextColor.color(0x67E555);
    public static final TextColor TEAM_MAGENTA = TextColor.color(0xDA50E0);
    public static final TextColor TEAM_ORANGE = TextColor.color(0xFF7900);
    public static final TextColor TEAM_PEACH = TextColor.color(0xFF8775);
    public static final TextColor TEAM_PURPLE = TextColor.color(0x7525DC);
    public static final TextColor TEAM_RED = TextColor.color(0xF74036);
    public static final TextColor TEAM_WHITE = TextColor.color(0xFFFFFF);
    public static final TextColor TEAM_YELLOW = TextColor.color(0xFBE059);

    public Teams() {
        List<String> playerlist = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            playerlist.add(p.getName());
        }
        Collections.shuffle(playerlist);

        if (playerlist.size() > 13) {
            Bukkit.getLogger().log(Level.INFO, "Sorting Players into teams (duos)...");
        } else {
            Bukkit.getLogger().log(Level.INFO, "Sorting Players into teams (solo)...");
        }

        if (Bukkit.getOnlinePlayers().isEmpty()) {
            Bukkit.getServer().sendMessage(Component.text("\nStarting the game requires a player to be online. Please login to the server and try again.\n"));
            return;
        } else {
            new BukkitRunnable() {
                @Override
                public void run() { //Clear every team when the game ends and everyone is kicked
                    if (knockoff.getInstance().GameManager == null) {
                        if (!blue.isEmpty()) {blue.clear();}
                        if (!cyan.isEmpty()) {cyan.clear();}
                        if (!green.isEmpty()) {green.clear();}
                        if (!lemon.isEmpty()) {lemon.clear();}
                        if (!lime.isEmpty()) {lime.clear();}
                        if (!magenta.isEmpty()) {magenta.clear();}
                        if (!orange.isEmpty()) {orange.clear();}
                        if (!peach.isEmpty()) {peach.clear();}
                        if (!purple.isEmpty()) {purple.clear();}
                        if (!red.isEmpty()) {red.clear();}
                        if (!white.isEmpty()) {white.clear();}
                        if (!yellow.isEmpty()) {yellow.clear();}
                        cancel();
                    }
                }
            }.runTaskTimer(knockoff.getInstance(), 20 ,1);

            if (playerlist.size() > 0) {
                if (blue.isEmpty()) {
                    blue.add(playerlist.get(0));
                    if (playerlist.size() > 12) {
                        blue.add(playerlist.get(12));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + blue + " in Team Blue");
                }
            }else {
                Bukkit.getLogger().log(Level.SEVERE, "Tried to add a player to team Blue but the player list is 0. Please report this as you shouldn't be able to get this error");
            }

            if (playerlist.size() > 1) { //If the player list is 2 or greater
                if (cyan.isEmpty()) {
                    blue.add(playerlist.get(1));
                    if (playerlist.size() > 13) {
                        cyan.add(playerlist.get(13));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + cyan + " in Team Cyan");
                }
            }else {
                Bukkit.getLogger().log(Level.WARNING, "No player(s) available for Cyan team (FYI: Recommend getting an alt account or someone else to join. 2 or more players is recommended)");
            }

            if (playerlist.size() > 2) { //If the player list is 3 or greater
                if (green.isEmpty()) {
                    green.add(playerlist.get(2));
                    if (playerlist.size() > 14) {
                        green.add(playerlist.get(14));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + green + " in Team Green");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Lemon team");
            }

            if (playerlist.size() > 3) { //If the player list is 4 or greater
                if (lemon.isEmpty()) {
                    lemon.add(playerlist.get(3));
                    if (playerlist.size() > 15) {
                        lemon.add(playerlist.get(15));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + lemon + " in Team Lemon");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Lemon team");
            }

            if (playerlist.size() > 4) { //If the player list is 5 or greater
                if (lime.isEmpty()) {
                    lime.add(playerlist.get(4));
                    if (playerlist.size() > 16) {
                        lime.add(playerlist.get(16));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + lime + " in Team Lime");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Lemon team");
            }

            if (playerlist.size() > 5) { //If the player list is 6 or greater
                if (magenta.isEmpty()) {
                    magenta.add(playerlist.get(5));
                    if (playerlist.size() > 17) {
                        magenta.add(playerlist.get(17));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + magenta + " in Team Magenta");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Lemon team");
            }

            if (playerlist.size() > 6) { //If the player list is 7 or greater
                if (orange.isEmpty()) {
                    orange.add(playerlist.get(6));
                    if (playerlist.size() > 18) {
                        orange.add(playerlist.get(18));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + orange + " in Team Orange");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Orange team");
            }

            if (playerlist.size() > 7) { //If the player list is 8 or greater
                if (peach.isEmpty()) {
                    peach.add(playerlist.get(7));
                    if (playerlist.size() > 19) {
                        peach.add(playerlist.get(19));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + peach + " in Team Peach");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Peach team");
            }

            if (playerlist.size() > 8) { //If the player list is 9 or greater
                if (purple.isEmpty()) {
                    purple.add(playerlist.get(8));
                    if (playerlist.size() > 20) {
                        purple.add(playerlist.get(20));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + purple + " in Team Purple");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Purple team");
            }

            if (playerlist.size() > 9) { //If the player list is 10 or greater
                if (red.isEmpty()) {
                    red.add(playerlist.get(9));
                    if (playerlist.size() > 21) {
                        red.add(playerlist.get(21));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + red + " in Team Red");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Red team");
            }

            if (playerlist.size() > 10) { //If the player list is 11 or greater
                if (white.isEmpty()) {
                    white.add(playerlist.get(10));
                    if (playerlist.size() > 22) {
                        white.add(playerlist.get(22));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + white + " in Team White");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for White team");
            }

            if (playerlist.size() > 11) { //If the player list is 12
                if (yellow.isEmpty()) {
                    yellow.add(playerlist.get(11));
                    if (playerlist.size() > 23) {
                        yellow.add(playerlist.get(23));
                    }
                    Bukkit.getLogger().log(Level.INFO, "Player(s) " + yellow + " in Team Yellow");
                }
            }else {
                Bukkit.getLogger().log(Level.INFO, "No player(s) available for Yellow team");
            }
        }
    }

    public static String GetPlayerTeam(Player player) {
        //Bukkit.getLogger().log(Level.INFO, "Figuring out " + player.getName() + "'s Team...");
        if (blue.contains(player.getName())) {
            return "blue";
        } else if (cyan.contains(player.getName())) {
            return "cyan";
        } else if (green.contains(player.getName())) {
            return "green";
        } else if (lemon.contains(player.getName())) {
            return "lemon";
        } else if (lime.contains(player.getName())) {
            return "lime";
        } else if (magenta.contains(player.getName())) {
            return "magenta";
        } else if (orange.contains(player.getName())) {
            return "orange";
        } else if (peach.contains(player.getName())) {
            return "peach";
        } else if (purple.contains(player.getName())) {
            return "purple";
        } else if (red.contains(player.getName())) {
            return "red";
        } else if (white.contains(player.getName())) {
            return "white";
        } else if (yellow.contains(player.getName())) {
            return "yellow";
        } else {
            return null;
        }
    }

    public static void SetPlayerDisplayNames(Player player) {
        if (blue.contains(player.getName())) {
            player.displayName(Component.text("\uE120 ").append(Component.text(player.getName()).color(TEAM_BLUE)));
        } else if (cyan.contains(player.getName())) {
            player.displayName(Component.text("\uE121 ").append(Component.text(player.getName()).color(TEAM_CYAN)));
        } else if (green.contains(player.getName())) {
            player.displayName(Component.text("\uE122 ").append(Component.text(player.getName()).color(TEAM_GREEN)));
        } else if (lemon.contains(player.getName())) {
            player.displayName(Component.text("\uE128 ").append(Component.text(player.getName()).color(TEAM_LEMON)));
        } else if (lime.contains(player.getName())) {
            player.displayName(Component.text("\uE123 ").append(Component.text(player.getName()).color(TEAM_LIME)));
        } else if (magenta.contains(player.getName())) {
            player.displayName(Component.text("\uE124 ").append(Component.text(player.getName()).color(TEAM_MAGENTA)));
        } else if (orange.contains(player.getName())) {
            player.displayName(Component.text("\uE129 ").append(Component.text(player.getName()).color(TEAM_ORANGE)));
        } else if (peach.contains(player.getName())) {
            player.displayName(Component.text("\uE12A ").append(Component.text(player.getName()).color(TEAM_PEACH)));
        } else if (purple.contains(player.getName())) {
            player.displayName(Component.text("\uE12B ").append(Component.text(player.getName()).color(TEAM_PURPLE)));
        } else if (red.contains(player.getName())) {
            player.displayName(Component.text("\uE125 ").append(Component.text(player.getName()).color(TEAM_RED)));
        } else if (white.contains(player.getName())) {
            player.displayName(Component.text("\uE126 ").append(Component.text(player.getName()).color(TEAM_WHITE)));
        } else if (yellow.contains(player.getName())) {
            player.displayName(Component.text("\uE127 ").append(Component.text(player.getName()).color(TEAM_YELLOW)));
        } else {
            player.displayName(Component.text("[Unknown Team]").append(Component.text(player.getName())));
        }
    }
}

class CustomPlayerNametags{

    public static void CustomPlayerNametags(Player player) {

        //I make 2 display entities facing the opposite way because normally display entities are only visible from 1 side
        Location ploc = new Location(player.getWorld(), player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        TextDisplay displayfront = ploc.getWorld().spawn(ploc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.CENTER);
        });
        TextDisplay displayback = ploc.getWorld().spawn(ploc, TextDisplay.class, entity -> {
            entity.setBillboard(Display.Billboard.CENTER);
        });

        new BukkitRunnable() {
            @Override
            public void run() {
                if (knockoff.getInstance().GameManager == null) {
                    displayfront.remove();
                    displayback.remove();
                    cancel();
                } else {
                    PlayerData pd = knockoff.getInstance().GameManager.getPlayerData(player);
                    if (pd.isPlayerDead) {
                        displayfront.text(Component.text(""));
                        displayback.text(Component.text(""));
                    } else {
                        displayfront.text(Component.text("")
                                .append(player.displayName())
                                .append(Component.text("\nKB: "))
                                .append(Component.text(pd.getDamagepercentage()))
                                .append(Component.text("% | L: "))
                                .append(Component.text(pd.getLives()))
                        );
                        displayback.text(Component.text("")
                                .append(player.displayName())
                                .append(Component.text("\nKB: "))
                                .append(Component.text(pd.getDamagepercentage()))
                                .append(Component.text("% | L: "))
                                .append(Component.text(pd.getLives()))
                        );
                    }

                    Location ploc = new Location(player.getWorld(), player.getX(), player.getY() + 2.5, player.getZ(), player.getYaw(), player.getPitch());
                    displayfront.teleport(ploc);
                    Location ploc2 = new Location(player.getWorld(), player.getX(), player.getY() + 2.5, player.getZ(), player.getYaw() + 180, player.getPitch());
                    displayback.teleport(ploc2);
                }
            }
        }.runTaskTimer(knockoff.getInstance(), 20, 1);
    }
}