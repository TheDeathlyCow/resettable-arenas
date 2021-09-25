package com.github.thedeathlycow.resettablearenas;

import org.bukkit.Bukkit;

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
     * @param name  The name of the arena. The name must be at most 13 characters
     *              and match the regular expression ^[a-zA-Z][a-zA-Z0-9_\-+]{0,12}$
     * @param world World
     * @param from  The origin chunk of the arena.
     * @param to    The chunk on the opposite corner of the arena from `from`.
     * @throws IllegalArgumentException Thrown if the name is invalid.
     */
    public Arena(String name) throws IllegalArgumentException {
        if (Pattern.matches("^[a-zA-Z][a-zA-Z0-9_\\-+]{0,12}$", name)) {
            this.NAME = name;
            initialiseObjectives();
        } else {
            throw new IllegalArgumentException("Illegal arena name '" + name + "'");
        }
    }

    //* Scoreboard stuff *//

    private void initialiseObjectives() {
        ScoreboardHandler handler = new ScoreboardHandler(Bukkit.getScoreboardManager().getMainScoreboard());
        String loadObjective = "ld." + getName();
        String saveObjective = "sv." + getName();
        handler.initialiseObjective(loadObjective, "dummy", loadObjective);
        handler.initialiseObjective(saveObjective, "dummy", saveObjective);
        handler.updateScoreboard(loadObjective, "$loadNum", loadVersion);
        handler.updateScoreboard(saveObjective, "$saveNum", saveVersion);
    }

    public void checkScoreboard() {
        ScoreboardHandler handler = new ScoreboardHandler(Bukkit.getScoreboardManager().getMainScoreboard());
        saveVersion = handler.getScore("sv." + getName(), "$saveNum");
        loadVersion = handler.getScore("ld." + getName(), "$loadNum");
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

    public void setLoadVersion(int loadVersion) {
        this.loadVersion = loadVersion;
        ScoreboardHandler handler = new ScoreboardHandler(Bukkit.getScoreboardManager().getMainScoreboard());
        handler.updateScoreboard("ld." + getName(), "$loadNum", this.loadVersion);
    }

    public void setSaveVersion(int saveVersion) {
        this.saveVersion = saveVersion;
        ScoreboardHandler handler = new ScoreboardHandler(Bukkit.getScoreboardManager().getMainScoreboard());
        handler.updateScoreboard("sv." + getName(), "$saveNum", this.saveVersion);
    }

    public void save() {
        setSaveVersion(this.saveVersion + 1);
    }

    public void load() {
        setLoadVersion(this.loadVersion + 1);
    }

    @Override
    public String toString() {
        return String.format("Arena:{name=%s, sv=%d, ld=%d}", NAME, saveVersion, loadVersion);
    }

}
