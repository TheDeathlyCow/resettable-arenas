package com.github.thedeathlycow.resettablearenas;

import com.github.thedeathlycow.resettablearenas.commands.ArenaCommandExecutor;
import com.github.thedeathlycow.resettablearenas.database.Database;
import com.github.thedeathlycow.resettablearenas.database.SQLite;
import com.github.thedeathlycow.resettablearenas.listeners.WorldListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ResettableArenas extends JavaPlugin {

    public static final String NAME = "Resettable-Arenas";

    private static ResettableArenas instance;
    private Database database;

    public ResettableArenas() {
        instance = this;
        database = new SQLite();
        database.load();
    }

    @Override
    public void onEnable() {
        instance = this;
        this.getDataFolder().mkdirs();
        this.getCommand("arena").setExecutor(new ArenaCommandExecutor(this));
        this.getServer().getPluginManager().registerEvents(new WorldListener(), this);
//        reloadData();
    }

    @Override
    public void onDisable() {
    }

    public Database getDatabase() {
        return database;
    }

    public static ResettableArenas getInstance() {
        return instance;
    }
}
