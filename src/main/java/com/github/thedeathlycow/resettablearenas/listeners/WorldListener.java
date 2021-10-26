package com.github.thedeathlycow.resettablearenas.listeners;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.ChunkSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.world.ChunkLoadEvent;

public class WorldListener implements Listener {

    private final ResettableArenas PLUGIN;
    private static final String PLAYING_PREFIX = "playing_";

    public WorldListener() {
        PLUGIN = ResettableArenas.getInstance();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        for (String tag : player.getScoreboardTags()) {
            if (tag.startsWith(PLAYING_PREFIX)) {
                String arena = tag.substring(PLAYING_PREFIX.length());
                String leaveTag = "leave_" + arena;
                player.addScoreboardTag(leaveTag);
                break;
            }
        }
    }
}
