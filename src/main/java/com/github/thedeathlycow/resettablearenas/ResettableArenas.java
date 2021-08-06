package com.github.thedeathlycow.resettablearenas;

import com.github.thedeathlycow.resettablearenas.commands.ArenaCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class ResettableArenas extends JavaPlugin implements Listener {

    public final ArenaRegistry ARENA_REGISTRY;
    public final ChunkScheduler CHUNK_SCHEDULER;

    public static final String NAME = "Resettable-Arenas";

    public ResettableArenas() {
        ARENA_REGISTRY = new ArenaRegistry(this);
        CHUNK_SCHEDULER = new ChunkScheduler(this);
    }

    @Override
    public void onEnable() {
        this.getDataFolder().mkdirs();
        this.getCommand("arena").setExecutor(new ArenaCommand());
        ARENA_REGISTRY.load();
        CHUNK_SCHEDULER.load();
        Ticker ticker = new Ticker(this);
        ticker.runTaskTimer(this, 1L, 20L);
    }

    @Override
    public void onDisable() {
        ARENA_REGISTRY.save();
        CHUNK_SCHEDULER.save();
    }
}
