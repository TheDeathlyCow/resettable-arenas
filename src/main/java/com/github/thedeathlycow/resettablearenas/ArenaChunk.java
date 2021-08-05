package com.github.thedeathlycow.resettablearenas;

import com.google.gson.*;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Objects;

public class ArenaChunk {

    private final Chunk CHUNK;
    private final File SCHEMATIC;
    private final ResettableArenas PLUGIN;
    private Arena arena;
    private int loadVersion = 0;
    private int saveVersion = 0;
    private transient boolean setLoadThisSession = false;
    private transient boolean setSaveThisSession = false;

    public ArenaChunk(ResettableArenas plugin, Chunk chunk) {
        this(plugin, null, chunk);
    }

    public ArenaChunk(ResettableArenas plugin, Arena arena, Chunk chunk) {
        this.CHUNK = chunk;
        this.arena = arena;
        this.PLUGIN = plugin;
        String schematicFile = String.format("/schematics/ArenaChunk.%d.%d.schem", chunk.getX(), chunk.getZ());
        this.SCHEMATIC = new File(plugin.getDataFolder().getAbsolutePath() + schematicFile);
        SCHEMATIC.getParentFile().mkdirs();
//        try {
//            SCHEMATIC.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    public void setArena(@Nullable Arena arena) {
        this.arena = arena;
    }

    public void tick() {
        if (arena != null && CHUNK.isLoaded()) {
            if (this.saveVersion != arena.getSaveVersion()) {
                this.save();
            }
            if (this.loadVersion != arena.getLoadVersion()) {
                this.load();
            }
        } else {
            System.out.println("Failed to load or save chunk " + this.toString());
            System.out.println(arena);
            System.out.println(CHUNK.isLoaded());
        }
    }

    private void save() {
        final BlockVector3 min = BlockVector3.at(CHUNK.getX()*16, 0, CHUNK.getZ()*16);
        final BlockVector3 max = BlockVector3.at(CHUNK.getX()*16 + 16, 255, CHUNK.getZ()*16 + 16);

        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(this.CHUNK.getWorld());
        CuboidRegion region = new CuboidRegion(adaptedWorld, min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        EditSession session = WorldEdit.getInstance().newEditSessionBuilder().world(adaptedWorld).build();
        ForwardExtentCopy copy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
        copy.setCopyingEntities(true);
        try {
            Operations.complete(copy);
        } catch (WorldEditException e) {
            e.printStackTrace();
            sendErrorMessage("Error " + e + " while copying " + this.toString());
        }
        session.close();

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(SCHEMATIC))) {
            writer.write(clipboard);
        } catch (IOException e) {
            e.printStackTrace();
            sendErrorMessage("Error saving schematic: " + e + " for " + this.toString());
        }

        PLUGIN.CHUNK_SCHEDULER.addChunk(this);
        this.saveVersion = arena.getSaveVersion();
    }

    private void load() {
        this.loadVersion = arena.getLoadVersion(); // immediately update load version, so no spam if error occurs
        // load clipboard
        Clipboard clipboard;
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(this.SCHEMATIC);
        try (ClipboardReader reader = clipboardFormat.getReader(new FileInputStream(this.SCHEMATIC))) {
            clipboard = reader.read();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            sendErrorMessage("Error loading schematic file: " + this.SCHEMATIC.getAbsolutePath()
                    + "in chunk " + this.toString());
            return;
        }

        // remove entities
        clearChunkEntities();
        // paste clipboard
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(this.CHUNK.getWorld());
        EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder().world(adaptedWorld).build();
        Operation operation = new ClipboardHolder(clipboard).createPaste(editSession)
                .to(BlockVector3.at(CHUNK.getX() * 16, 0, CHUNK.getZ() * 16))
                .ignoreAirBlocks(false).build();
        try {
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
            sendErrorMessage("Error pasting schematic: " + e + " for " + this.toString());
        }
        editSession.close();
    }

    private void clearChunkEntities() {
        for (Entity entity : CHUNK.getEntities()) {
            if (!(entity instanceof Player)) {
                Location location = entity.getLocation();
                entity.teleport(location.add(0, -1000, 0));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArenaChunk chunk = (ArenaChunk) o;
        return CHUNK.equals(chunk.CHUNK);
    }

    @Override
    public int hashCode() {
        return Objects.hash(CHUNK);
    }

    @Override
    public String toString() {
        return "ArenaChunk:{chunk={x=" + this.CHUNK.getX() + ",z=" + this.CHUNK.getZ() + "}"
                + ",arena=" + this.arena.getName() + "}";
    }

    public int getLoadVersion() {
        return loadVersion;
    }

    public int getSaveVersion() {
        return saveVersion;
    }


    private void sendErrorMessage(String message) {
        PLUGIN.getServer().broadcastMessage(ChatColor.RED + "[ResettableArenas] ERROR: " + message);
    }

    public static class Serializer implements JsonSerializer<ArenaChunk>, JsonDeserializer<ArenaChunk> {

        private final ResettableArenas plugin;
        public final Gson blockGson;

        public Serializer(ResettableArenas plugin) {
            this.plugin = plugin;
            this.blockGson = new GsonBuilder()
                    .registerTypeAdapter(ArenaChunk.class, this)
                    .disableHtmlEscaping()
                    .create();
        }

        @Override
        public ArenaChunk deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            //* The basic data elems
            int chunkX = jsonObject.get("chunkX").getAsInt();
            int chunkZ = jsonObject.get("chunkZ").getAsInt();
            String worldName = jsonObject.get("world").getAsString();
            World world = plugin.getServer().getWorld(worldName);
            Chunk worldChunk = world.getChunkAt(chunkX, chunkZ);
            String arenaName = jsonObject.get("arena").getAsString();
            Arena arena = plugin.ARENA_REGISTRY.getArenaByName(arenaName);

            ArenaChunk arenaChunk = new ArenaChunk(plugin, arena, worldChunk);
            //* Save and load nums
            int loadVersion = jsonObject.get("loadVersion").getAsInt();
            int saveVersion = jsonObject.get("saveVersion").getAsInt();
            arenaChunk.loadVersion = loadVersion;
            arenaChunk.saveVersion = saveVersion;

            return arenaChunk;
        }

        @Override
        public JsonElement serialize(ArenaChunk src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("chunkX", src.CHUNK.getX());
            jsonObject.addProperty("chunkZ", src.CHUNK.getZ());
            jsonObject.addProperty("world", src.CHUNK.getWorld().getName());
            jsonObject.addProperty("arena", src.arena.getName());
            jsonObject.addProperty("loadVersion", src.loadVersion);
            jsonObject.addProperty("saveVersion", src.saveVersion);

            return jsonObject;
        }
    }
}
