package com.github.thedeathlycow.resettablearenas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ArenaRegistry {

    private final Set<Arena> arenas = new HashSet<>();
    private final String filename;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    public ArenaRegistry(ResettableArenas plugin) {
        this.filename = plugin.getDataFolder().getAbsolutePath() + "/arenas.json";
    }

    public void load() {
        arenas.clear();
        try (FileReader reader = new FileReader(filename)) {
            arenas.addAll(gson.fromJson(reader, new TypeToken<List<Arena>>(){}.getType()));
        } catch (IOException notFound) {
            try {
                File file = new File(filename);
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void save() {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        try (FileWriter writer = new FileWriter(filename)) {
            String json = gson.toJson(arenas);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addArena(Arena arena) {
//        for (Arena a : arenas) {
//            if (a.getName().equals(arena.getName())) {
//                return;
//            }
//        }
        arenas.add(arena);
    }

    @Nullable
    public Arena getArenaByName(String arenaName) {
        for (Arena arena : arenas) {
            if (arena.getName().equals(arenaName)) {
                return arena;
            }
        }
        return null;
    }

    public boolean deleteArena(Arena toDelete) {
        return arenas.remove(toDelete);
    }

    public Set<Arena> getArenas() {
        return Collections.unmodifiableSet(arenas);
    }
}
