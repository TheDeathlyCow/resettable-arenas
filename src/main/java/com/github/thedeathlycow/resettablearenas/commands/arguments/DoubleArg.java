package com.github.thedeathlycow.resettablearenas.commands.arguments;

public class DoubleArg extends Argument<Double> {
    /**
     * Attempts to parse an argument, and saves it if it was successful.
     *
     * @param arg The string arg to be parsed.
     * @throws ArgParseException Thrown if the argument could not be parsed.
     */
    public DoubleArg(String arg) throws ArgParseException {
        super(arg);
    }

    @Override
    public Double parseArg(String arg) throws ArgParseException {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new ArgParseException(e.getMessage());
        }
    }
}
