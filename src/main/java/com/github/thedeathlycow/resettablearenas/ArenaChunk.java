package com.github.thedeathlycow.resettablearenas;

import com.fastasyncworldedit.bukkit.util.BukkitTaskManager;
import com.github.thedeathlycow.resettablearenas.database.Database;
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
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private final ChunkWrapper CHUNK;
    /**
     * The schematic file of this chunks blockdata.
     */
    @Nonnull
    private final File SCHEMATIC;
    /**
     * The resettable arenas plugin.
     */
    @Nonnull
    private transient final ResettableArenas PLUGIN;
    /**
     * The arena this chunk is listening to.
     */
    @Nonnull
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
     * @param arena  Arena this chunk belongs to. May be null.
     * @param chunk  The world chunk this arena chunk belongs to.
     */
    public ArenaChunk(@Nonnull Arena arena, @Nonnull ChunkSnapshot chunk) {
        this.CHUNK = new ChunkWrapper(chunk);
        this.arena = arena;
        this.PLUGIN = ResettableArenas.getInstance();
        String schematicFile = String.format("/schematics/region.%d.%d/ArenaChunk.%d.%d.schem",
                chunk.getX() / 32, chunk.getZ() / 32,
                chunk.getX(), chunk.getZ());
        this.SCHEMATIC = new File(PLUGIN.getDataFolder().getAbsolutePath() + schematicFile);

        if (!SCHEMATIC.getParentFile().exists()) {
            SCHEMATIC.getParentFile().mkdirs();
        }
    }

    public void tick(Database db) {

        Chunk chunk = CHUNK.getChunk();

        boolean wasUnloaded = false;

        if (!chunk.isLoaded()) {
            wasUnloaded = true;
            chunk.load();
        }

        boolean saved = false;
        boolean loaded = false;

//        System.out.println(this.toString());
        if (this.saveVersion != arena.getSaveVersion()) {
            this.save();
            saved = true;
        }
        if (!saved && (this.loadVersion != arena.getLoadVersion())) {
            this.load();
            loaded = true;
        }
        updateDatabase(db, saved, loaded);

        if (wasUnloaded) {
            chunk.unload();
        }
    }

    private void updateDatabase(Database db, boolean saved, boolean loaded) {
        Runnable saveDBTask = () -> db.updateChunk(this, "saveVer", this.getSaveVersion());
        Runnable loadDBTask = () -> db.updateChunk(this, "loadVer", this.getLoadVersion());
        if (saved) {
            Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, saveDBTask);
        }
        if (loaded) {
            Bukkit.getScheduler().runTaskAsynchronously(PLUGIN, loadDBTask);
        }
    }

    private void checkPlayers() {

        List<Player> playersInChunk = new ArrayList<>(20);
        for (Entity entity : CHUNK.getChunk().getEntities()) {
            if (entity instanceof Player) {
                playersInChunk.add((Player) entity);
            }
        }

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        ScoreboardHandler handler = new ScoreboardHandler(scoreboard);

        String leaveTag = String.format(PLUGIN.getConfig().getString("LeaveTag", "leave_%s"), arena.getName());

        for (Player player : playersInChunk) {
            Location location = player.getLocation();
            int loadNum = handler.getScore(
                    "ld." + arena.getName(),
                    player.getName());

            if (loadNum != arena.getLoadVersion()) {
                player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "This arena has been reset! You have been sent back to spawn.");
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.ITALIC + "(This usually happens if you left a game and came back after the game finished.)");
                player.addScoreboardTag(leaveTag);
                player.playSound(location, Sound.BLOCK_NOTE_BLOCK_DIDGERIDOO, 1, 0.8f);
            }
        }
    }

    /**
     * Saves the current state of the chunk to a schematic.
     */
    public void save() {
        System.out.println("Saving " + this.toString());
        this.saveVersion = arena.getSaveVersion();
        ChunkSnapshot chunk = CHUNK.getSnapshot();
        final BlockVector3 min = BlockVector3.at(chunk.getX() * 16, 0, chunk.getZ() * 16);
        final BlockVector3 max = BlockVector3.at(chunk.getX() * 16 + 15, 255, chunk.getZ() * 16 + 15);

        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(Bukkit.getWorld(chunk.getWorldName()));
        CuboidRegion region = new CuboidRegion(adaptedWorld, min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession session = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, -1)) {
            ForwardExtentCopy copy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
            copy.setCopyingEntities(false);
            Operations.complete(copy);
        } catch (WorldEditException e) {
            e.printStackTrace();
            sendErrorMessage("Error " + e + " while copying " + this.toString());
        }

        try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(new FileOutputStream(SCHEMATIC))) {
            writer.write(clipboard);
        } catch (IOException e) {
            e.printStackTrace();
            sendErrorMessage("Error saving schematic: " + e + " for " + this.toString());
        }
    }

    /**
     * Loads this arena chunk from its last saved state, if that state exists.
     */
    public void load() {
        System.out.println("Loading " + this.toString());
        this.loadVersion = arena.getLoadVersion(); // stop trying to load if this load fails
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

        // remove players and other entities
        checkPlayers();
        clearChunkEntities();

        // paste clipboard
        Chunk chunk = CHUNK.getChunk();
        com.sk89q.worldedit.world.World adaptedWorld = BukkitAdapter.adapt(chunk.getWorld());
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(adaptedWorld, -1)) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(BlockVector3.at(chunk.getX() * 16, 0, chunk.getZ() * 16))
                    .ignoreAirBlocks(false).build();
            Operations.complete(operation);
        } catch (WorldEditException e) {
            e.printStackTrace();
            sendErrorMessage("Error pasting schematic: " + e + " for " + this.toString());
        }
    }

    /**
     * Removes the entities from this chunk.
     */
    private void clearChunkEntities() {
        for (Entity entity : CHUNK.getChunk().getEntities()) {
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
        return "ArenaChunk:{sv=" + this.getSaveVersion() +
                ",ld=" + this.getLoadVersion() +
                ",chunk={x=" + CHUNK.getChunk().getX() + ",z=" + CHUNK.getChunk().getZ() + "}"
                + ",arena={" + this.arena.toString() + "}}";
    }

    public void setLoadVersion(int loadVersion) {
        this.loadVersion = loadVersion;
    }

    public void setSaveVersion(int saveVersion) {
        this.saveVersion = saveVersion;
    }

    public String getWorldname() {
        return CHUNK.getSnapshot().getWorldName();
    }

    public int getPosX() {
        return CHUNK.getSnapshot().getX();
    }

    public int getPosZ() {
        return CHUNK.getSnapshot().getZ();
    }

    public int getLoadVersion() {
        return loadVersion;
    }

    public int getSaveVersion() {
        return saveVersion;
    }

    public Arena getArena() {
        return arena;
    }

    private void sendErrorMessage(String message) {
        PLUGIN.getServer().broadcastMessage(ChatColor.RED + "[ResettableArenas] ERROR: " + message);
    }

}
