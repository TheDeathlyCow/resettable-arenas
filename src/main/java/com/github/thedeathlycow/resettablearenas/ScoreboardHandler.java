package com.github.thedeathlycow.resettablearenas;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class ScoreboardHandler {

    private final Scoreboard SCOREBOARD;

    public ScoreboardHandler(Scoreboard scoreboard) {
        this.SCOREBOARD = scoreboard;
    }

    public void initialiseObjective(String name, String type, String displayName) {
        try {
            SCOREBOARD.registerNewObjective(name, type, displayName);
        } catch (IllegalArgumentException ignored) { }
    }

    public int getScore(String objectiveName, String key) {
        Objective objective = SCOREBOARD.getObjective(objectiveName);
        return objective.getScore(key).getScore();
    }

    public void updateScoreboard(String objectiveName, String key, int value) {
        Objective objective = SCOREBOARD.getObjective(objectiveName);
        objective.getScore(key).setScore(value);
    }
}
