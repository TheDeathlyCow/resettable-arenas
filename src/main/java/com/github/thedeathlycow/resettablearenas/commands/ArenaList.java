package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ArenaList implements CommandExecutor {

    private final ResettableArenas plugin;

    public ArenaList() {
        this.plugin = (ResettableArenas) Bukkit.getPluginManager().getPlugin(ResettableArenas.NAME);
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        StringBuilder message = new StringBuilder();
        message.append(ChatColor.DARK_AQUA)
                .append("Arenas:\n")
                .append(ChatColor.AQUA);
        for (Arena arena : plugin.ARENA_REGISTRY.getArenas()) {
            message.append(" - ")
                    .append(arena.getName())
                    .append(" Load = ")
                    .append(arena.getLoadVersion())
                    .append(", Save = ")
                    .append(arena.getSaveVersion())
                    .append("\n");
        }

        message.append(ChatColor.DARK_AQUA)
                .append("Total Arenas: ")
                .append(plugin.ARENA_REGISTRY.getArenas().size());
        sender.sendMessage(message.toString());
        return true;
    }
}
