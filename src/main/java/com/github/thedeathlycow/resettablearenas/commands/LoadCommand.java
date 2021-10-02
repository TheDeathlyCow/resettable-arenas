package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.commands.arguments.ArenaArg;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;

public class LoadCommand extends SubCommand {
    public LoadCommand() {
        super("load", new ArenaArg());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, Argument<?>[] args) {
        Arena arena = (Arena) args[0].getValue();
        arena.load();
        try {
            database.updateArena(arena, "loadVer", arena.getLoadVersion());
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "Marked arena " + arena.getName() + " for loading!");
        return true;
    }
}
