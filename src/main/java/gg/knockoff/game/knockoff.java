package gg.knockoff.game;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.function.pattern.RandomPattern;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.block.BlockState;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;

public final class knockoff extends JavaPlugin {

    public final MapData mapdata = new MapData();
    public boolean is_force_starting = false;
    public GameManager GameManager;

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        this.getServer().getPluginManager().registerEvents(new CrystalBlocks(), this);

        Bukkit.getWorld("world").setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 20);

        DebugCommands dc = new DebugCommands();
        this.getCommand("force_start").setExecutor(dc);
        new BukkitRunnable() {

            @Override
            public void run() {
                if (GameManager != null) {
                    return;
                }

                if (is_force_starting) {
                    if (knockoff.getInstance().getServer().getOnlinePlayers().size() > 24) {//24 is the max player limit for now
                        Bukkit.getServer().sendMessage(Component.text("Too many players to start a game (hardcoded limit is 24). Please kick players off or limit your player count in server.properties."));
                        return;
                    } else {
                        GameManager = new GameManager();
                        is_force_starting = false;
                        return;
                    }
                }
            }
        }.runTaskTimer(knockoff.getInstance(), 1, 20);

        // Sets the target area to air to prevent previous game's sections to interfere with the current game
        // Could be optimised, Filling all this in 1 go and/or in larger spaces causes your server to most likely go out of memory or not respond for a good while
        // Plus this lags the server anyways
        getLogger().log(Level.INFO, "Making room for knockoff map, This may lag your server depending on how good it is");
        com.sk89q.worldedit.world.World world = BukkitAdapter.adapt(Bukkit.getWorld("world"));
        CuboidRegion selection = new CuboidRegion(world, BlockVector3.at(1000100, -30, 1000100), BlockVector3.at(1000000, 40, 1000000));
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
            RandomPattern pat = new RandomPattern();
            BlockState air = BukkitAdapter.adapt(Material.AIR.createBlockData());
            pat.add(air, 1);
            editSession.setBlocks(selection, pat);
        }  catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "[GAMEMANAGER] Exception occured within the worldedit API:");
            e.printStackTrace();
        }

        getLogger().log(Level.INFO, "KnockOff Plugin Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO, "Knockoff Plugin Disabling. If this is a reload, We highly recommend restarting instead");
    }

    public static knockoff getInstance() {
        return getPlugin(knockoff.class);
    }
}
