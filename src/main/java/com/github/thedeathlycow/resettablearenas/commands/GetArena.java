package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class GetArena extends SubCommand {

    public GetArena(String identifier, @Nullable Class<?>... argsTypes) {
        super("get", Arena.class);
    }

    @Override
    public boolean run(@NotNull CommandSender sender, List<Argument<?>> args) {
        if (isValidArgs(args)) {
            Arena arena = (Arena)args.get(0).getValue();
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Error: Illegal arguments specified.");
            return false;
        }
    }
}
