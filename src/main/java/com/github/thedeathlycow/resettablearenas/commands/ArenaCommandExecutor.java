package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ArenaCommandExecutor implements CommandExecutor {

    private final ResettableArenas PLUGIN;

    public ArenaCommandExecutor(@NotNull ResettableArenas plugin) {
        this.PLUGIN = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Error: No subcommand specified!");
            return false;
        }
        SubCommand subCommand = SubCommands.getCommand(args[0]);

        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "Error: Invalid command specified.");
            return false;
        }

        List<String> argsList = Arrays.asList(args).subList(1, args.length);
        return subCommand.run(sender, argsList);
    }
}
