package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import com.sk89q.worldedit.antlr4.runtime.misc.Pair;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;

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
        Definer definer = new Definer(plugin, arena, executedIn, new Pair<>(fromX, fromZ), new Pair<>(toX, toZ));
        definer.runTaskLater(plugin, 5);
        defineScoreboard(arena);

        return true;
    }

    private void defineScoreboard(Arena arena) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        String load = "ld." + arena.getName();
        String save = "sv." + arena.getName();
        scoreboard.registerNewObjective(load, "dummy", load);
        scoreboard.registerNewObjective(save, "dummy", save);

        scoreboard.getObjective(load).getScore("loadNum").setScore(arena.getLoadVersion());
        scoreboard.getObjective(load).getScore("saveNum").setScore(arena.getSaveVersion());

    }
}
