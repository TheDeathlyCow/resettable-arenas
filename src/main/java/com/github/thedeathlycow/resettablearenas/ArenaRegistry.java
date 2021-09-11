package com.github.thedeathlycow.resettablearenas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Chunk;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class ArenaRegistry {

    private final Map<String, Arena> arenas = new HashMap<>();
    private final String filename;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Arena.class, new Arena.Deserializer())
            .disableHtmlEscaping()
            .create();

    public ArenaRegistry(ResettableArenas plugin) {
        this.filename = plugin.getDataFolder().getAbsolutePath() + "/arenas.json";
    }

    public void load() {
        arenas.clear();
        try (FileReader reader = new FileReader(filename)) {
            List<Arena> arenasList = gson.fromJson(reader, new TypeToken<List<Arena>>(){}.getType());
            arenasList.forEach((arena -> arenas.put(arena.getName(), arena)));
        } catch (IOException notFound) {
            try {
                File file = new File(filename);
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int size() {
        return arenas.size();
    }

    public void addArena(Arena arena) {
        arenas.put(arena.getName(), arena);
    }

    @Nullable
    public Arena getArenaByName(String arenaName) {
        return arenas.get(arenaName);
    }

    public boolean deleteArena(Arena toDelete) {
        return arenas.remove(toDelete.getName(), toDelete);
    }

    public Collection<Arena> getArenas() {
        return arenas.values();
    }

    public static List<ArenaChunk> getChunks(Arena arena, Chunk from, Chunk to) {
        return new ArrayList<>();
    }
}
