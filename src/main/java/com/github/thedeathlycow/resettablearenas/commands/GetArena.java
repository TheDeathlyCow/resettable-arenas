package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.commands.arguments.ArenaArg;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class GetArena extends SubCommand {

    public GetArena() {
        super("get", new ArenaArg());
    }

    @Override
    protected boolean execute(@NotNull CommandSender sender, Argument<?>[] args, int numArgs) {
        Arena arena = (Arena)args[0].getValue();
        sender.sendMessage(ChatColor.GREEN + arena.toString());
        return true;
    }
}
