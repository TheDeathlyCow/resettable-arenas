package com.github.thedeathlycow.resettablearenas;

import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
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

    private final World world;

    private final transient List<ArenaChunk> CHUNKS;

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
    public Arena(String name, World world, ArenaBound from, ArenaBound to) throws IllegalArgumentException {
        if (Pattern.matches("^[a-zA-Z][a-zA-Z0-9_\\-+]{0,12}$", name)) {
            this.NAME = name;
            this.CHUNKS = new ArrayList<>();
            this.world = world;

            initialiseObjectives();

            ArenaDefiner definer = new ArenaDefiner(this, world, from, to);
            Bukkit.getScheduler()
                    .runTaskAsynchronously(ResettableArenas.getInstance(), definer);

        } else {
            throw new IllegalArgumentException("Illegal arena name '" + name + "'");
        }
    }

    void addChunk(ArenaChunk chunk) {
        chunk.setArena(this);
        CHUNKS.add(chunk);
    }

    //* Actor methods *//

    public void tick() {
        checkScoreboard();
        long start = System.currentTimeMillis();
        CHUNKS.forEach(ArenaChunk::tick);
        long elapsed = System.currentTimeMillis() - start;
        if (elapsed > 100) {
            System.out.printf("Arena tick took %.3f seconds!%n", elapsed / 1000f);
            System.out.println("Total chunks for arena: " + CHUNKS.size());
        }
    }

    /**
     * Sets the save version of the arena.
     *
     * @param saveVersion
     */
    public void save(int saveVersion) {
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
        updateScoreboard("sv." + getName(), "saveNum", saveVersion);
    }

    /**
     * Sets the load version of the arena.
     *
     * @param loadVersion
     */
    public void load(int loadVersion) {
        this.loadVersion = loadVersion;
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
        updateScoreboard("ld." + getName(), "loadNum", loadVersion);
    }

    //* Scoreboard stuff *//

    private void initialiseObjectives() {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        try {
            scoreboard.registerNewObjective("ld." + NAME, "dummy", "Load" + NAME);
        } catch (IllegalArgumentException ignored) {
        }
        try {
            scoreboard.registerNewObjective("sv." + NAME, "dummy", "Save" + NAME);
        } catch (IllegalArgumentException ignored) {
        }
        updateScoreboard("ld." + NAME, "$loadNum", loadVersion);
        updateScoreboard("sv." + NAME, "$saveNum", loadVersion);
    }

    public void checkScoreboard() {
        saveVersion = getScore("sv." + getName(), "$saveNum");
        loadVersion = getScore("ld." + getName(), "$loadNum");
    }

    private int getScore(String objectiveName, String key) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        return objective.getScore(key).getScore();
    }

    private void updateScoreboard(String objectiveName, String key, int value) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective objective = scoreboard.getObjective(objectiveName);
        objective.getScore(key).setScore(value);
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
    public String toString() {
        return String.format("%s: sv=%d, ld=%d", NAME, saveVersion, loadVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Arena arena = (Arena) o;
        return loadVersion == arena.loadVersion && saveVersion == arena.saveVersion && NAME.equals(arena.NAME) && world.equals(arena.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loadVersion, saveVersion, NAME, world);
    }

    // TODO: gen equals and hashcode

    public static class Deserializer implements JsonDeserializer<Arena> {

        public static final Gson GSON = new GsonBuilder()
                .disableHtmlEscaping()
                .registerTypeAdapter(ArenaBound.class, new ArenaBound())
                .registerTypeAdapter(Arena.class, new Deserializer())
                .create();

        /**
         * <pre>
         * {
         *      "name":"example",
         *      "world":"overworld",
         *      "from": {
         *          "x": 0,
         *          "z" 0
         *      },
         *      "to": {
         *          "x": 512,
         *      "z": 512
         *      }
         * }
         * </pre>
         *
         * @param json
         * @param typeOfT
         * @param context
         * @return
         * @throws JsonParseException
         */
        @Override
        public Arena deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String name = object.get("name").getAsString();
            ArenaBound from = GSON.fromJson(object.get("from"), ArenaBound.class);
            ArenaBound to = GSON.fromJson(object.get("to"), ArenaBound.class);

            String worldName = object.get("world").getAsString();
            World world = Bukkit.getWorld(worldName);

            Arena deserialized = new Arena(name, world, from, to);

            return deserialized;
        }
    }


}
