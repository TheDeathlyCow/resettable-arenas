package com.github.thedeathlycow.resettablearenas;

import com.github.thedeathlycow.resettablearenas.commands.ArenaCommand;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class ResettableArenas extends JavaPlugin implements Listener {

    public final ArenaRegistry ARENA_REGISTRY;

    public static final String NAME = "Resettable-Arenas";

    public ResettableArenas() {
        ARENA_REGISTRY = new ArenaRegistry(this);
    }

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        this.getConfig().addDefault("chunk-size", 16);
        this.getCommand("arena").setExecutor(new ArenaCommand(this));
        reloadData();
        Ticker ticker = new Ticker(this);
        ticker.runTaskTimer(this, 1L, 20L);
    }

    @Override
    public void onDisable() {
    }

    public void reloadData() {
        this.ARENA_REGISTRY.load();
    }

    public static ResettableArenas getInstance() {
        return (ResettableArenas)Bukkit.getPluginManager()
                .getPlugin(NAME);
    }
}
