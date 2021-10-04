package com.github.thedeathlycow.resettablearenas;

import com.github.thedeathlycow.resettablearenas.commands.ArenaCommandExecutor;
import com.github.thedeathlycow.resettablearenas.database.Database;
import com.github.thedeathlycow.resettablearenas.database.SQLite;
import com.github.thedeathlycow.resettablearenas.listeners.WorldListener;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

public class ResettableArenas extends JavaPlugin {

    public static final String NAME = "Resettable-Arenas";

    private static ResettableArenas instance;
    private static Player dummy;
    private static com.sk89q.worldedit.entity.Player dummyWE;
    private Database database;
    private BukkitTask checker;

    public ResettableArenas() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        database = new SQLite();
        database.load();
        this.getCommand("arena").setExecutor(new ArenaCommandExecutor(this));
        this.getServer().getPluginManager().registerEvents(new WorldListener(), this);

        ArenaTicker ticker = new ArenaTicker();
        checker = Bukkit.getScheduler().runTaskTimerAsynchronously(this, ticker, 5, 20);
    }

    @Override
    public void onDisable() {
        checker.cancel();
        database.onDisable();
    }

    public Database getDatabase() {
        return database;
    }

    public static ResettableArenas getInstance() {
        return instance;
    }
}
