package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import com.github.thedeathlycow.resettablearenas.commands.arguments.ArenaArg;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import com.github.thedeathlycow.resettablearenas.commands.arguments.IntegerArg;
import com.github.thedeathlycow.resettablearenas.commands.arguments.StringArg;
import com.github.thedeathlycow.resettablearenas.database.Database;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AddArenaChunks extends SubCommand {
    public AddArenaChunks() {
        super("addChunks",
                new ArenaArg(), // arena
                new StringArg(), // world name
                new IntegerArg(), // from
                new IntegerArg(),
                new IntegerArg(), // to
                new IntegerArg());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, Argument<?>[] args, int numArgs) {
        Arena arena = (Arena) args[0].getValue();

        String worldName = (String) args[1].getValue();
        int fromX = (Integer) args[2].getValue();
        int fromZ = (Integer) args[3].getValue();

        int toX = (Integer) args[4].getValue();
        int toZ = (Integer) args[5].getValue();

        World world = Bukkit.getWorld(worldName);

        List<ArenaChunk> chunks = new ArrayList<>();
        int dx = getDir(fromX, toX);
        int dz = getDir(fromZ, toZ);

        int chunksAdded = 0;
        final long start = System.currentTimeMillis();
        for (int x = fromX; dx > 0 ? x <= toX : x >= toX; x += dx) {
            for (int z = fromZ; dz > 0 ? z <= toZ : z >= toZ; z += dz) {
                Location curr = new Location(world, 16 * x, 0, 16 * z);
                ChunkSnapshot chunk = world.getChunkAt(curr).getChunkSnapshot();
                ArenaChunk arenaChunk = new ArenaChunk(arena, chunk);
                arenaChunk.setLoadVersion(arena.getLoadVersion());
                arenaChunk.setSaveVersion(arena.getSaveVersion());
                chunks.add(arenaChunk);
                chunksAdded++;
            }
        }
        final int total = chunksAdded;
        Bukkit.getScheduler().scheduleAsyncDelayedTask(ResettableArenas.getInstance(),
                () -> {
                    Database db = ResettableArenas.getInstance().getDatabase();
                    for (ArenaChunk c : chunks) {
                        db.addArenaChunk(c);
                    }
                    long time = System.currentTimeMillis() - start;
                    String message = String.format("Successfully added %s chunks (took %.3f ms)!", total, time / 1000.0f);
                    sender.sendMessage(ChatColor.GREEN + message);
                }, 1);
        return true;
    }

    private int getDir(int from, int to) {
        return Math.abs(to - from) / (to - from);
    }
}
