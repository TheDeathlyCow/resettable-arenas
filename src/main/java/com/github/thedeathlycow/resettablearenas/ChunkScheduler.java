package com.github.thedeathlycow.resettablearenas;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChunkScheduler extends BukkitRunnable {

    private final Set<ArenaChunk> chunks;
    private final Gson gson;
    private final String filename;
    private final ResettableArenas plugin;

    public ChunkScheduler(ResettableArenas plugin) {
        this.plugin = plugin;
        chunks = new HashSet<>();
        gson = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(ArenaChunk.class, new ArenaChunk.Serializer(plugin))
                .create();
        this.filename = plugin.getDataFolder().getAbsolutePath() + "/chunks.json";
    }

    public void load() {

        try (FileReader reader = new FileReader(filename)) {
            chunks.addAll(gson.fromJson(reader, new TypeToken<List<ArenaChunk>>() {
            }.getType()));
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

        try (FileWriter writer = new FileWriter(filename)) {
            String json = gson.toJson(chunks);
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addChunk(ArenaChunk chunk) {
        chunks.add(chunk);
    }

    @Override
    public void run() {
        chunks.forEach(ArenaChunk::tick);
    }
}
