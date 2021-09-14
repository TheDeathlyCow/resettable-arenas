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

        // TODO : replace with database

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Error: No subcommand specified!");
            return false;
        }

        if (args[0].equalsIgnoreCase("reload")) {

        } else if (args[0].equalsIgnoreCase("list")) {

        } else {
            sender.sendMessage(ChatColor.RED + "Error: Invalid arguments!");
            return false;
        }

        return true;
    }
}
