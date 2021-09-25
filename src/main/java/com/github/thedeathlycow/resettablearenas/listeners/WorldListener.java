package com.github.thedeathlycow.resettablearenas.listeners;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.ChunkSnapshot;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {

    private final ResettableArenas PLUGIN;

    public WorldListener() {
        PLUGIN = ResettableArenas.getInstance();
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
//        ChunkSnapshot chunk = event.getChunk().getChunkSnapshot();
//
//        ArenaChunk arenaChunk = PLUGIN.getDatabase().getArenaChunk(chunk);
//
//        if (arenaChunk != null) {
//            arenaChunk.tick(PLUGIN.getDatabase());
//        }
    }
}
