package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SubCommand {

    private final String identifier;
    private final List<Class<?>> argsTypes = new ArrayList<>();

    public SubCommand(String identifier, @Nullable Class<?>... argsTypes) {
        this.identifier = identifier;
        if (argsTypes != null) {
            this.argsTypes.addAll(Arrays.asList(argsTypes));
        }
    }

    public abstract boolean run(@NotNull CommandSender sender, List<Argument<?>> args);

    public boolean run(@NotNull CommandSender sender, Argument<?>... args) {
        return run(sender, Arrays.asList(args));
    }
}
