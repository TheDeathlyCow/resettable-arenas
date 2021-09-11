package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ArenaCommand implements CommandExecutor {

    private final ResettableArenas PLUGIN;

    public ArenaCommand(@NotNull ResettableArenas plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Error: Incorrect number of arguments specified!");
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            PLUGIN.ARENA_REGISTRY
                    .load();
            sender.sendMessage(ChatColor.GREEN +
                    String.format("Successfully reloaded %d arenas!",
                            PLUGIN.ARENA_REGISTRY.size()));
        } else if (args[0].equalsIgnoreCase("list")) {
            if (PLUGIN.ARENA_REGISTRY.size() == 0) {
                sender.sendMessage(ChatColor.GREEN + "There are no arenas defined yet!");
            } else {
                StringBuilder message = new StringBuilder();
                message.append(ChatColor.GREEN).append("Arenas:");
                for (Arena arena : PLUGIN.ARENA_REGISTRY.getArenas()) {
                    message.append("- ")
                            .append(arena.toString())
                            .append("\n");
                }
                message.append("There are ")
                        .append(PLUGIN.ARENA_REGISTRY.size())
                        .append(" arenas!");

                sender.sendMessage(message.toString());
            }
        } else {
            sender.sendMessage(ChatColor.RED + "Error: Invalid arguments!");
            return false;
        }

        return true;
    }
}
