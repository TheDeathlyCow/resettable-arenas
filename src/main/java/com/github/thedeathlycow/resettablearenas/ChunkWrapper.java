package com.github.thedeathlycow.resettablearenas;

import org.bukkit.Chunk;
import org.bukkit.World;

import java.util.Objects;

public class ChunkWrapper {

    private int x;
    private int z;
    private World world;

    public ChunkWrapper(Chunk chunk) {
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.world = chunk.getWorld();
    }

    public Chunk getChunk() {
        return world.getChunkAt(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkWrapper that = (ChunkWrapper) o;
        return x == that.x && z == that.z && Objects.equals(world, that.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, z, world);
    }
}
