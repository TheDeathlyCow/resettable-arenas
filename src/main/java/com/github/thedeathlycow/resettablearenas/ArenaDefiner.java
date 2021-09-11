package com.github.thedeathlycow.resettablearenas;

import org.bukkit.ChunkSnapshot;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

class ArenaDefiner implements Runnable {

    private final ResettableArenas plugin;
    private final Arena arena;
    private final World world;
    private final ArenaBound from;
    private final ArenaBound to;

    private final List<ChunkSnapshot> chunkSnapshots;

    public ArenaDefiner(Arena arena, World world, ArenaBound from, ArenaBound to) {
        this.plugin = ResettableArenas.getInstance();
        this.arena = arena;
        this.from = from;
        this.to = to;
        this.world = world;
        int size = Math.abs(from.getX() - to.getX()) * Math.abs(from.getZ() - to.getZ());
        chunkSnapshots = new ArrayList<>(size / ArenaChunk.CHUNK_SIZE);
        snapshotChunks();
    }

    private void snapshotChunks() {
        final int dx = getDir(from.getX(), to.getX());
        final int dz = getDir(from.getZ(), to.getZ());
        for (int x = from.getX(); dx > 0 ? x <= to.getX() : x >= to.getX(); x += dx) {
            for (int z = from.getZ(); dz > 0 ? z <= to.getZ() : z >= to.getZ(); z += dz) {
                chunkSnapshots.add(world.getChunkAt(x, z).getChunkSnapshot());
            }
        }
    }

    @Override
    public void run() {
        for (ChunkSnapshot chunk : chunkSnapshots) {
            ArenaChunk arenaChunk = new ArenaChunk(plugin, arena, chunk);
            arena.addChunk(arenaChunk);
        }
        chunkSnapshots.clear();
    }

    private static int getDir(int from, int to) {
        return Math.abs(to - from) / (to - from);
    }
}
