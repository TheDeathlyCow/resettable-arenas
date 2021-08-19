package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import com.sk89q.worldedit.antlr4.runtime.misc.Pair;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

class Definer {

    private final ResettableArenas plugin;
    private final Arena arena;
    private final World world;
    private final Pair<Integer, Integer> from;
    private final Pair<Integer, Integer> to;
    private final List<BukkitRunnable> tasks;

    public Definer(ResettableArenas plugin, Arena arena, World world, Pair<Integer, Integer> from, Pair<Integer, Integer> to) {
        super();
        this.plugin = plugin;
        this.arena = arena;
        this.from = from;
        this.to = to;
        this.world = world;
        tasks = new ArrayList<>();
        defineTasks();
    }

    private void defineTasks() {
        final int dx = getDir(from.a, to.a);
        final int dz = getDir(from.b, to.b);
        for (int x = from.a; dx > 0 ? x <= to.a : x >= to.a; x += dx) {
            final int task = x;
            tasks.add(new BukkitRunnable() {
                @Override
                public void run() {
                    for (int z = from.b; dz > 0 ? z <= to.b : z >= to.b; z += dz) {
                        Chunk chunk = world.getChunkAt(task, z);
                        ArenaChunk arenaChunk = new ArenaChunk(plugin, arena, chunk);
                        plugin.CHUNK_SCHEDULER.addChunk(arenaChunk);
                        System.out.printf("Defined chunk %d, %d to be part of %s%n",
                                chunk.getX(), chunk.getZ(), arena.getName());
                    }
                }
            });
        }
    }

    public void run() {

        int numTasks = 0;
        for (BukkitRunnable task : tasks) {
            task.runTaskLater(plugin, ++numTasks);
        }
        plugin.reloadData();
    }

    private int getDir(int from, int to) {
        return Math.abs(to - from) / (to - from);
    }
}
