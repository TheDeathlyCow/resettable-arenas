package com.github.thedeathlycow.resettablearenas.commands.arguments;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import com.github.thedeathlycow.resettablearenas.database.Database;

public class ArenaArg extends Argument<Arena> {

    private final Database database;

    /**
     * Attempts to parse an argument, and saves it if it was successful.
     *
     * @param arg The string arg to be parsed.
     * @throws ArgParseException Thrown if the argument could not be parsed.
     */
    public ArenaArg(String arg) throws ArgParseException {
        super(arg);
        database = ResettableArenas.getInstance().getDatabase();
    }

    @Override
    public Arena parseArg(String arg) throws ArgParseException {
        Arena arena = database.getArena(arg);
        if (arena != null) {
            return arena;
        } else {
            throw new ArgParseException("Arena '" + arg + "' does not exist!");
        }
    }
}
