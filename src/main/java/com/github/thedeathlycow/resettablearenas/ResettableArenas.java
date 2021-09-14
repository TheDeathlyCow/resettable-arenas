package com.github.thedeathlycow.resettablearenas;

import com.github.thedeathlycow.resettablearenas.commands.ArenaCommand;
import com.github.thedeathlycow.resettablearenas.database.Database;
import com.github.thedeathlycow.resettablearenas.listeners.WorldListener;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResettableArenas extends JavaPlugin implements Listener {

    public static final String NAME = "Resettable-Arenas";

    private static ResettableArenas instance;
    private Database database;

    public ResettableArenas() {

    }

    @Override
    public void onEnable() {
        instance = this;
        this.getDataFolder().mkdirs();
        this.getCommand("arena").setExecutor(new ArenaCommand(this));
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
