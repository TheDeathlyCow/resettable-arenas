package com.github.thedeathlycow.resettablearenas.commands;

import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import com.github.thedeathlycow.resettablearenas.commands.arguments.ArgParseException;
import com.github.thedeathlycow.resettablearenas.commands.arguments.Argument;
import com.github.thedeathlycow.resettablearenas.database.Database;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class SubCommand {

    private final String identifier;
    private final Argument<?>[] args;

    protected final Database database;

    public SubCommand(String identifier, @Nullable Argument<?>... args) {
        this.identifier = identifier;
        database = ResettableArenas.getInstance().getDatabase();
        if (args == null) {
            args = new Argument[0];
        }
        this.args = args;
    }

    protected abstract boolean execute(@NotNull CommandSender sender, Argument<?>[] args);

    public boolean run(@NotNull CommandSender sender, List<String> args) {
        boolean isValidArgs = false;

        try {
            isValidArgs = parseArgs(args);
        } catch (ArgParseException e) {
            sender.sendMessage(ChatColor.RED + "Error: " + e);
        }

        if (isValidArgs) {
            return execute(sender, this.args);
        } else {
            sender.sendMessage(ChatColor.RED + "Error: Illegal arguments specified.");
            return false;
        }
    }

    public boolean run(@NotNull CommandSender sender, String... args) {
        return run(sender, Arrays.asList(args));
    }

    /**
     * Parse a list of args
     * @param argsStr
     * @return
     */
    private boolean parseArgs(List<String> argsStr) throws ArgParseException {

        if (!this.isValidArgs(argsStr)) {
            return false;
        }

        for (int i = 0; i < this.args.length; i++) {
            Argument<?> arg = this.args[i];
            arg.setValue(argsStr.get(i));
        }
        return true;
    }

    public boolean isValidArgs(List<String> argsStr) {
        if (argsStr.size() != this.args.length) {
            return false;
        }

        for (int i = 0; i < this.args.length; i++) {
            Argument<?> arg = this.args[i];
            try {
                arg.parseArg(argsStr.get(i));
            } catch (ArgParseException e) {
                System.out.println("Error parsing argument: " + e);
                return false;
            }
        }
        return true;
    }

    public String getIdentifier() {
        return identifier;
    }
}
