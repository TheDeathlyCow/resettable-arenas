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
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * An arena chunk is a chunk that is part of an arena. Each arena chunk may
 * only belong to one arena, and only one arena chunk may exist per world
 * chunk. Contains pointer variables to the chunk and arena, as well as
 * the schematic file for the blockdata of this chunk.
 */
public class ArenaChunk {

    /**
     * The world chunk of this arena chunk.
     */
    @Nonnull
    private final Chunk CHUNK;
    /**
     * The schematic file of this chunks blockdata.
     */
    @Nonnull
    private final File SCHEMATIC;
    /**
     * The resettable arenas plugin.
     */
    @Nonnull
    private final ResettableArenas PLUGIN;
    /**
     * The arena this chunk is listening to.
     */
    @Nullable
    private Arena arena;
    /**
     * The load version of this chunk.
     */
    private int loadVersion = 0;
    /**
     * The save version of this chunk.
     */
    private int saveVersion = 0;

    /**
     * Creates a new arena chunk.
     *
     * @param plugin Plugin reference for this chunk.
     * @param arena Arena this chunk belongs to. May be null.
     * @param chunk The world chunk this arena chunk belongs to.
     */
    public ArenaChunk(@Nonnull ResettableArenas plugin, @Nullable Arena arena, @Nonnull Chunk chunk) {
        this.CHUNK = chunk;
        this.arena = arena;
        this.PLUGIN = plugin;
        String schematicFile = String.format("/schematics/region.%d.%d/ArenaChunk.%d.%d.schem",
                chunk.getX()/32, chunk.getZ()/32,
                chunk.getX(), chunk.getZ());
        this.SCHEMATIC = new File(plugin.getDataFolder().getAbsolutePath() + schematicFile);
        SCHEMATIC.getParentFile().mkdirs();
    }

    /**
     * Updates the arena of this arena chunk.
     * @param arena New arena of this chunk.
     */
    public void setArena(@Nullable Arena arena) {
        this.arena = arena;
    }

    /**
     * Runs periodically to check if this arena chunk should be reloaded, saved,
     * or deleted.
     */
    public void tick() {
        if (!PLUGIN.ARENA_REGISTRY.getArenas().contains(this.arena)) {
            delete();
        }
        if (arena != null && CHUNK.isLoaded()) {
            if (this.saveVersion != arena.getSaveVersion()) {
                this.save();
            }
            if (this.loadVersion != arena.getLoadVersion()) {
                this.loadVersion = arena.getLoadVersion(); // immediately update load version, so no spam if error occurs
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        load();
                    }
                }.runTaskLater(this.PLUGIN, ThreadLocalRandom.current().nextInt(40) + 1);

                this.load();
            }
        }
    }

    /**
     * Marks this chunk as unowned and deletes block data associated with it.
     */
    private void delete() {
        arena = null;
        SCHEMATIC.delete();
    }

    /**
     * Saves the current state of the chunk to a schematic.
     */
    private void save() {
        final BlockVector3 min = BlockVector3.at(CHUNK.getX()*16, 0, CHUNK.getZ()*16);
        final BlockVector3 max = BlockVector3.at(CHUNK.getX()*16 + 16, 255, CHUNK.getZ()*16 + 16);

        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(this.CHUNK.getWorld());
        CuboidRegion region = new CuboidRegion(adaptedWorld, min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, -1);
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

    /**
     * Loads this arena chunk from its last saved state, if that state exists.
     */
    private void load() {
        if (!SCHEMATIC.exists()) {
            return;
        }
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
        EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, -1);
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

    /**
     * Removes the entities from this chunk.
     */
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

    /**
     * Handles JSON serialization and deserialization for arena chunks.
     */
    public static class Serializer implements JsonSerializer<ArenaChunk>, JsonDeserializer<ArenaChunk> {

        /**
         * Plugin reference.
         */
        private final ResettableArenas plugin;

        /**
         * Constructs an arena chunk serializer with a plugin reference.
         *
         * @param plugin Resettable arenas plugin.
         */
        public Serializer(ResettableArenas plugin) {
            this.plugin = plugin;
        }

        /**
         * Converts a JSON object to an arena chunk.
         * JSON objects of ArenaChunks must have the following format:
         * <pre>
         *     {
         *         "chunkX": int,
         *         "chunkZ": int,
         *         "world": string,
         *         "arena": string,
         *         "loadVersion": int,
         *         "saveVersion": int
         *     }
         * </pre>
         *
         * @param json
         * @param typeOfT
         * @param context
         * @return
         * @throws JsonParseException
         */
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