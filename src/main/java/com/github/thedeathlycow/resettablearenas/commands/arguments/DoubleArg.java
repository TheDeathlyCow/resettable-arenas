package com.github.thedeathlycow.resettablearenas.commands.arguments;

public class DoubleArg extends Argument<Double> {

    @Override
    public Double parseArg(String arg) throws ArgParseException {
        try {
            return Double.parseDouble(arg);
        } catch (NumberFormatException e) {
            throw new ArgParseException(e.getMessage());
        }
    }
}
