package com.github.thedeathlycow.resettablearenas;

import org.bukkit.*;

import java.util.Objects;

public class ChunkWrapper {

    private Location location;
    private String worldName;
    private ChunkSnapshot snapshot;

    public ChunkWrapper(Chunk chunk) {
        this(chunk.getChunkSnapshot());
    }

    public ChunkWrapper(ChunkSnapshot chunk) {
        this.worldName = chunk.getWorldName();
        this.location = new Location(Bukkit.getWorld(worldName), chunk.getX() * 16, 0, chunk.getZ() * 16);
        this.snapshot = chunk;
    }

    public ChunkWrapper(String worldName, Location location) {
        this.worldName = worldName;
        this.location = location;
        snapshot = null;
    }

    public Chunk getChunk() {
        World world = Bukkit.getWorld(worldName);
        return world.getChunkAt(location);
    }

    public ChunkSnapshot getSnapshot() {
        if (snapshot == null) {
            snapshot = getChunk().getChunkSnapshot();
        }
        return snapshot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkWrapper that = (ChunkWrapper) o;
        return location.equals(that.location) && worldName.equals(that.worldName) && snapshot.equals(that.snapshot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, worldName, snapshot);
    }
}
