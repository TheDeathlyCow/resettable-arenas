package com.github.thedeathlycow.resettablearenas;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Ticker extends BukkitRunnable {

    private final ResettableArenas PLUGIN;

    public Ticker(ResettableArenas plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public void run() {
        PLUGIN.ARENA_REGISTRY.getArenas()
                .forEach(Arena::checkScoreboard);

        PLUGIN.CHUNK_SCHEDULER.getChunks()
                .forEach(ArenaChunk::tick);
    }
}
