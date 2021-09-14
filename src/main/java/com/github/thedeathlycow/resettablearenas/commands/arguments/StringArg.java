package com.github.thedeathlycow.resettablearenas.commands.arguments;

public class StringArg extends Argument<String> {
    /**
     * Attempts to parse an argument, and saves it if it was successful.
     *
     * @param arg The string arg to be parsed.
     * @throws ArgParseException Thrown if the argument could not be parsed.
     */
    public StringArg(String arg) throws ArgParseException {
        super(arg);
    }

    @Override
    public String parseArg(String arg) {
        return arg;
    }
}
