package com.github.thedeathlycow.resettablearenas.commands.arguments;

public class IntegerArg extends Argument<Integer> {
    /**
     * Attempts to parse an argument, and saves it if it was successful.
     *
     * @param arg The string arg to be parsed.
     * @throws ArgParseException Thrown if the argument could not be parsed.
     */
    public IntegerArg(String arg) throws ArgParseException {
        super(arg);
    }

    @Override
    public Integer parseArg(String arg) throws ArgParseException {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new ArgParseException(e.getMessage());
        }
    }
}
