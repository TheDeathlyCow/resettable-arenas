package com.github.thedeathlycow.resettablearenas.commands.arguments;

import org.jetbrains.annotations.Nullable;

/**
 * Stores and parses an argument of a generic type.
 *
 * @param <E> The type of an argument.
 */
public abstract class Argument<E> {

    /**
     * The value of an argument.
     */
    private E value;

    /**
     * Attempts to parse an argument, and returns it if it was successful.
     *
     * @param arg The string arg to be parsed.
     * @return The value of the argument, if it was successfully parsed.
     * @throws ArgParseException Thrown if the argument could not be parsed.
     */
    public abstract E parseArg(String arg) throws ArgParseException;

    /**
     * Returns the value of this argument.
     *
     * @return The value of this argument.
     */
    @Nullable
    public E getValue() {
        return value;
    }

    /**
     * Attempts to parse the argument, and if successful, sets the parsed
     * value to the value of this argument.
     *
     * @param arg String value of arg.
     * @throws ArgParseException Thrown if arg could not be parsed.
     */
    public void setValue(String arg) throws ArgParseException {
        this.value = parseArg(arg);
    }
}
