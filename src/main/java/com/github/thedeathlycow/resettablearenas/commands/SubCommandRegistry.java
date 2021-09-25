package com.github.thedeathlycow.resettablearenas.commands;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SubCommandRegistry {

    private static final List<SubCommand> COMMANDS = new ArrayList<>();

    public static final SubCommand GET;
    public static final SubCommand LIST;
    public static final SubCommand CREATE;
    public static final SubCommand ADD_CHUNK;

    static {
        GET = register(new GetArena());
        LIST = register(new ListArenas());
        CREATE = register(new CreateArena());
        ADD_CHUNK = register(new AddArenaChunk());
    }

    @Nullable
    public static SubCommand getCommand(String identifier) {
        for (SubCommand command : COMMANDS) {
            if (command.getIdentifier().equalsIgnoreCase(identifier)) {
                return command;
            }
        }
        return null;
    }

    private static SubCommand register(SubCommand command) {
        COMMANDS.add(command);
        return command;
    }
}
