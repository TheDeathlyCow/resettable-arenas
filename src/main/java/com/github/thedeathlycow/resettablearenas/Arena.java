package com.github.thedeathlycow.resettablearenas;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Handles basic data for Arenas including their name,
 * load version, and save version.
 *
 * @author TheDeathlyCow
 */
public class Arena {

    /**
     * The current 'load version' of this arena. Whenever this is updated,
     * any ArenaChunks listening to this arena will load their saved schematic
     * files.
     */
    private int loadVersion = 0;
    /**
     * The current 'save version' of this arena. Whenever this is updated,
     * any ArenaChunks listening to this arena will save their chunks to a
     * schematic file.
     */
    private int saveVersion = 0;
    /**
     * The name of this arena. Must be unique for all arenas.
     */
    private final String NAME;

    /**
     * Creates an arena with a name.
     *
     * @param name The name of the arena. The name must be at most 13 characters
     *             and match the regular expression ^[a-zA-Z0-9_\-+]{1,13}$
     * @throws IllegalArgumentException Thrown if the name is invalid.
     */
    public Arena(String name) throws IllegalArgumentException {
        if (Pattern.matches("^[a-zA-Z0-9_\\-+]{1,13}$", name)) {
            this.NAME = name;
        } else {
            throw new IllegalArgumentException("Illegal arena name '" + name + "'");
        }

    }

    //* Actor methods *//

    /**
     * Sets the load version of the arena.
     *
     * @param loadVersion
     */
    public void setLoadVersion(int loadVersion) {
        this.loadVersion = loadVersion;
    }

    /**
     * Sets the save version of the arena.
     *
     * @param saveVersion
     */
    public void setSaveVersion(int saveVersion) {
        this.saveVersion = saveVersion;
    }

    /**
     * Increments the save value of the arena, instructing chunks to
     * begin saving their current state.
     * <p>
     * IMPORTANT: Only chunks that are loaded will be saved. If the chunk
     * is not loaded, it will not be saved.
     */
    public void save() {
        saveVersion++;
    }

    /**
     * Increments the load value of the arena, instructing chunks to
     * begin resetting to their current state.
     * <p>
     * IMPORTANT: Only chunks that are loaded will be reset. If the chunk
     * is not loaded, it will not be reset.
     */
    public void load() {
        loadVersion++;
    }

    //* Getters *//

    /**
     * @return The name of the arena.
     */
    public String getName() {
        return NAME;
    }

    /**
     * @return The load version of the arena.
     */
    public int getLoadVersion() {
        return loadVersion;
    }

    /**
     * @return The save version of the arena.
     */
    public int getSaveVersion() {
        return saveVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return NAME.equals(arena.NAME);
    }

    @Override
    public int hashCode() {
        return Objects.hash(NAME);
    }
}
