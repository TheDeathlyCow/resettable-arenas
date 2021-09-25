package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import com.github.thedeathlycow.resettablearenas.commands.arguments.StringArg;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;

public class CreateArena extends SubCommand {

    public CreateArena() {
        super("create", new StringArg());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, Argument<?>[] args) {
        String arenaName = (String) args[0].getValue();
        Arena arena = new Arena(arenaName);
        try {
            database.addArena(arena);
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Error adding arena: " + e);
            return false;
        }
        sender.sendMessage(ChatColor.GREEN + "Successfully created arena" + arena.getName());
        return true;
    }
}
