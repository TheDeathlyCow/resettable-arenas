package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
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
        if (isValidArgs(args)) {
            StringBuilder builder = new StringBuilder();
            builder.append(ChatColor.GREEN);

            List<Arena> arenas = database.getAllArenas();

            if (arenas.size() == 0) {
                builder.append("There are no arenas yet!");
            } else {
                builder.append("All Resettable Arenas:\n");
                arenas.forEach((a) -> builder.append(String.format(" - %s: sv=%d, ld=%d\n",
                        a.getName(), a.getSaveVersion(), a.getLoadVersion())));
                builder.append(String.format("Total: %d arenas", arenas.size()));
            }
            sender.sendMessage(builder.toString());
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Error: Illegal arguments specified.");
            return false;
        }
    }
}
