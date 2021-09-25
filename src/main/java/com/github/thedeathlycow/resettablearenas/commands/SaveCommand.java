package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.commands.arguments.ArenaArg;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.SQLException;

public class SaveCommand extends SubCommand {

    public SaveCommand() {
        super("save", new ArenaArg());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, Argument<?>[] args) {
        Arena arena = (Arena)args[0].getValue();
        arena.save();
        try {
            database.updateArena(arena, "saveVer", arena.getSaveVersion());
        } catch (SQLException e) {
            sender.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + "Marked arena " + arena.getName() + " for saving!");
        return true;
    }
}
