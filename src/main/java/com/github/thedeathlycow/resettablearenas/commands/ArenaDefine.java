package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
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
            e.printStackTrace();
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

    private boolean defineChunks(Arena arena, String[] args, Player player) {
        final int fromX = Integer.parseInt(args[2]);
        final int fromZ = Integer.parseInt(args[3]);
        final int toX = Integer.parseInt(args[4]);
        final int toZ = Integer.parseInt(args[5]);

        World executedIn = player.getLocation().getWorld();
        if (executedIn == null) {
            player.sendMessage(ChatColor.RED + "Error locating world!");
            return false;
        }

        final int dx = 16 * getDir(fromX, toX);
        final int dz = 16 * getDir(fromZ, toZ);
        for (int x = Math.min(fromX, toX); x < Math.max(fromX, toX); x += dx) {
            for (int z = Math.min(fromZ, toZ); z < Math.max(fromZ, toZ); z += dz) {
                Chunk chunk = executedIn.getChunkAt(x/16, z/16);
                ArenaChunk arenaChunk = new ArenaChunk(plugin, arena, chunk);
                plugin.CHUNK_SCHEDULER.addChunk(arenaChunk);
            }
        }

        return true;
    }


    private int getDir(int from, int to) {
        return Math.abs(to - from) / (to - from);
    }
}
