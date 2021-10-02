package com.github.thedeathlycow.resettablearenas;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardHandler {

    private final Scoreboard SCOREBOARD;

    /**
     * Creates a scoreboard handler for the main scoreboard.
     */
    public ScoreboardHandler() {
        this(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Creates a scoreboard handler on a specific scoreboard.
     *
     * @param scoreboard
     */
    public ScoreboardHandler(Scoreboard scoreboard) {
        this.SCOREBOARD = scoreboard;
    }

    public boolean isRegisteredObjective(String objective) {
        return SCOREBOARD.getObjective(objective) != null;
    }

    /**
     * Create a new objective on the scoreboard.
     *
     * @param name
     * @param type
     * @param displayName
     */
    public void initialiseObjective(String name, String type, String displayName) {
        try {
            SCOREBOARD.registerNewObjective(name, type, displayName);
        } catch (IllegalArgumentException ignored) {
        }
    }

    /**
     * Returns the score of a key in an objective.
     *
     * @param objectiveName
     * @param key
     * @return
     */
    public int getScore(String objectiveName, String key) {
        Objective objective = SCOREBOARD.getObjective(objectiveName);
        return objective.getScore(key).getScore();
    }

    /**
     * Sets the value in a key:value pair for an objective.
     *
     * @param objectiveName
     * @param key
     * @param value
     */
    public void updateScoreboard(String objectiveName, String key, int value) {
        Objective objective = SCOREBOARD.getObjective(objectiveName);
        objective.getScore(key).setScore(value);
    }
}
