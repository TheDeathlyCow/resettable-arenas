package com.github.thedeathlycow.resettablearenas;

import com.github.thedeathlycow.resettablearenas.database.Database;
import org.bukkit.Bukkit;

import java.util.List;

public class ArenaTicker implements Runnable {

    private final ResettableArenas PLUGIN;
    private final Database DATABASE;

    public ArenaTicker() {
        this.PLUGIN = ResettableArenas.getInstance();
        this.DATABASE = PLUGIN.getDatabase();
    }

    /**
     * Runs periodically and asynchronously to check that all the
     * arena chunks are up-to-date.
     */
    @Override
    public void run() {
        List<Arena> arenas = DATABASE.getAllArenas(); // database query
        for (Arena arena : arenas) {
            List<ArenaChunk> chunks = DATABASE.getAllChunks(arena); // nother database query
            // non threadsafe calls to bukkit api - put back on main thread
            Bukkit.getScheduler().scheduleSyncDelayedTask(
                    PLUGIN, () -> {
                        arena.checkScoreboard();
                        chunks.forEach(ArenaChunk::tick);
                    }, 1 // small delay
            );
        }
    }
}
