package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import com.github.thedeathlycow.resettablearenas.database.Database;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SubCommand {

    private final String identifier;
    private final List<Class<?>> argsTypes = new ArrayList<>();

    protected final Database database;

    public SubCommand(String identifier, @Nullable Class<?>... argsTypes) {
        this.identifier = identifier;
        database = ResettableArenas.getInstance().getDatabase();
        if (argsTypes != null) {
            this.argsTypes.addAll(Arrays.asList(argsTypes));
        }
    }

    public abstract boolean run(@NotNull CommandSender sender, List<Argument<?>> args);

    public boolean run(@NotNull CommandSender sender, Argument<?>... args) {
        return run(sender, Arrays.asList(args));
    }

    public boolean isValidArgs(List<Argument<?>> args) {
        if (args.size() != argsTypes.size()) {
            return false;
        }
        for (int i = 0; i < args.size(); i++) {
            Class<?> givenClass = args.get(i).getValue().getClass();
            if (givenClass != argsTypes.get(i)) {
                return false;
            }
        }
        return true;
    }

    public String getIdentifier() {
        return identifier;
    }
}
