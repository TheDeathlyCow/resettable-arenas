package com.github.thedeathlycow.resettablearenas.commands.arguments;

/**
 * Stores and parses an argument of a generic type.
 *
 * @param <E> The type of an argument.
 */
public abstract class Argument<E> {

    /**
     * The value of an argument.
     */
    protected E value;

    /**
     * Attempts to parse an argument, and saves it if it was successful.
     *
     * @param arg The string arg to be parsed.
     * @throws ArgParseException Thrown if the argument could not be parsed.
     */
    public Argument(String arg) throws ArgParseException {
        this.value = parseArg(arg);
    }

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
    public E getValue() {
        return value;
    }
}
