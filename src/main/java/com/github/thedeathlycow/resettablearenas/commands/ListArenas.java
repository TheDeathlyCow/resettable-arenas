package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ListArenas extends SubCommand {
    public ListArenas() {
        super("list", null);
    }

    @Override
    public boolean run(@NotNull CommandSender sender, List<Argument<?>> args) {
        if (args.size() == 0) {

            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Error: Incorrect number of arguments specified.");
            return false;
        }
    }
}
