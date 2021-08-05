package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ArenaCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Error: This command can only be executed by a player!");
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendHelp(player);
            return true;
        }

        SubCommands subCommand;
        try {
            subCommand = SubCommands.valueOf(args[0].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(ChatColor.RED + "Error: Illegal command argument: " + args[0]);
            return false;
        }
        return subCommand.executor.onCommand(player, command, label, args);
    }

    private enum SubCommands {
        DEFINE(new ArenaDefine()),
        SAVE(new ArenaSave()),
        LOAD(new ArenaLoad());

        private final CommandExecutor executor;

        SubCommands(CommandExecutor executor) {
            this.executor = executor;
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "=== Help for ResettableArenas ===");
        sender.sendMessage(ChatColor.RED + " /arena help - Brings up this message.");
        sender.sendMessage(ChatColor.RED + " /arena define <name: string> <from: x z> <to: x z> - Defines an arena with the specified name between two (x, z) coordinates.");
        sender.sendMessage(ChatColor.RED + " /arena save <arena_name: string> - Saves the current state chunks of the arena when they are loaded.");
        sender.sendMessage(ChatColor.RED + " /arena load <arena_name: string> - Loads the chunks from memory to their last saved version.");
    }
}
