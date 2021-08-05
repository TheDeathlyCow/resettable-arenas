package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import com.sk89q.worldedit.math.Vector2;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaDefine implements CommandExecutor {

    private final ResettableArenas plugin;

    public ArenaDefine() {
        this.plugin = (ResettableArenas) Bukkit.getPluginManager().getPlugin(ResettableArenas.NAME);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        Arena arena;
        String name;
        try {
            name = args[1];
            arena = new Arena(name);
        } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            player.sendMessage(ChatColor.RED + "Error executing command: " + e.getMessage());
            return false;
        }
        plugin.ARENA_REGISTRY.addArena(arena);

        try {
            if (defineChunks(arena, args, player)) {
                player.sendMessage(ChatColor.AQUA + "Successfully created arena '" + name + "'!");
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Error: Illegal command arguments.");
            return false;
        }

        return true;
    }

    public boolean defineChunks(Arena arena, String[] args, Player player) {
        final int fromX = Integer.parseInt(args[2]) / 16;
        final int fromZ = Integer.parseInt(args[3]) / 16;
        final int toX = Integer.parseInt(args[4]) / 16;
        final int toZ = Integer.parseInt(args[5]) / 16;

        World executedIn = player.getLocation().getWorld();
        if (executedIn == null) {
            player.sendMessage(ChatColor.RED + "Error locating world!");
            return false;
        }

        final int dx = getDir(fromX, toX);
        final int dz = getDir(fromZ, toZ);
        for (int x = fromX ; dx > 0 ? x <= toX : x >= toX; x += dx) {
            for (int z = fromZ; dz > 0 ? z <= toZ : z >= toZ; z += dz) {
                Chunk chunk = executedIn.getChunkAt(x, z);
                ArenaChunk arenaChunk = new ArenaChunk(plugin, arena, chunk);
                plugin.CHUNK_SCHEDULER.addChunk(arenaChunk);
                System.out.printf("Defined chunk %d %d to be in arena %s%n", x, z, arena.getName());
            }
        }

        return true;
    }

    private int getDir(int from, int to) {
        return Math.abs(to - from) / (to - from);
    }
}
