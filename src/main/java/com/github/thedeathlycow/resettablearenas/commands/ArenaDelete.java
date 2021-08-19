package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ArenaDelete implements CommandExecutor {

    private final ResettableArenas plugin;

    public ArenaDelete() {
        this.plugin = (ResettableArenas) Bukkit.getPluginManager().getPlugin(ResettableArenas.NAME);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        String toDelete = args[1];

        Arena arena = plugin.ARENA_REGISTRY.getArenaByName(toDelete);

        if (arena == null) {
            sender.sendMessage(ChatColor.RED + "Error: Arena '" + toDelete + "' does not exist!");
            return false;
        }

        if (!plugin.ARENA_REGISTRY.deleteArena(arena)) {
            sender.sendMessage(ChatColor.RED + "Error: Unable to delete arena '" + toDelete + "'!");
            return false;
        }
        plugin.reloadData();
        sender.sendMessage(ChatColor.AQUA + "Successfully deleted arena '" + toDelete + "'!");
        return true;
    }
}
