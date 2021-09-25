package com.github.thedeathlycow.resettablearenas.commands.arguments;

public class IntegerArg extends Argument<Integer> {

    @Override
    public Integer parseArg(String arg) throws ArgParseException {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new ArgParseException(e.getMessage());
        }
    }
}
