package com.github.thedeathlycow.resettablearenas.commands;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubCommands {

    private static final Map<String, SubCommand> COMMANDS = new HashMap<>();

    public static final SubCommand GET;
    public static final SubCommand LIST;
    public static final SubCommand CREATE;
    public static final SubCommand ADD_CHUNK;
    public static final SubCommand ADD_CHUNKS;
    public static final SubCommand SAVE;
    public static final SubCommand LOAD;

    static {
        GET = register(new GetArena());
        LIST = register(new ListArenas());
        CREATE = register(new CreateArena());
        ADD_CHUNK = register(new AddArenaChunk());
        ADD_CHUNKS = register(new AddArenaChunks());
        SAVE = register(new SaveCommand());
        LOAD = register(new LoadCommand());
    }

    @Nullable
    public static SubCommand getCommand(String identifier) {

        return COMMANDS.get(identifier.toLowerCase());
    }

    private static SubCommand register(SubCommand command) {
        String key = command.getIdentifier().toLowerCase();
        if (COMMANDS.containsKey(key)) {
            throw new IllegalStateException("Subcommand '" + key + "' already defined!");
        }
        COMMANDS.put(key, command);
        return command;
    }
}
