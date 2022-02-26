package com.github.thedeathlycow.resettablearenas;

import com.fastasyncworldedit.core.FaweAPI;
import com.github.thedeathlycow.resettablearenas.database.Database;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.*;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.*;
import org.bukkit.entity.*;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

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
     * @param arena Arena this chunk belongs to. May be null.
     * @param chunk The world chunk this arena chunk belongs to.
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

    public void tick() {
        if (CHUNK.isLoaded()) {
            boolean saved = false;
            boolean loaded = false;
            if (this.saveVersion != arena.getSaveVersion()) {
                this.save();
                saved = true;
            }
            if (!saved && (this.loadVersion != arena.getLoadVersion())) {
                this.load();
                loaded = true;
            }
            Database db = PLUGIN.getDatabase();
            updateDatabase(db, saved, loaded);
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

    /**
     * Saves the current state of the chunk to a schematic.
     */
    public void save() {
        System.out.println("Saving " + this);
        this.saveVersion = arena.getSaveVersion();
        ChunkSnapshot chunk = CHUNK.getSnapshot();
        final BlockVector3 min = BlockVector3.at(chunk.getX() * 16, -64, chunk.getZ() * 16);
        final BlockVector3 max = BlockVector3.at(chunk.getX() * 16 + 15, 319, chunk.getZ() * 16 + 15);

        World world = FaweAPI.getWorld(chunk.getWorldName());
        Region region = new CuboidRegion(min, max);
        BlockArrayClipboard clipboard = new BlockArrayClipboard(region);

        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(world).changeSetNull().fastMode(true).build()) {
            ForwardExtentCopy copy = new ForwardExtentCopy(session, region, clipboard, region.getMinimumPoint());
            copy.setCopyingEntities(false);
            Operations.complete(copy);
            Operations.complete(clipboard.commit());
        } catch (WorldEditException e) {
            e.printStackTrace();
            sendErrorMessage("Error " + e + " while copying " + this);
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
        System.out.println("Loading " + this);
        this.loadVersion = arena.getLoadVersion(); // stop trying to load if this load fails
        if (!SCHEMATIC.exists()) {
            return;
        }
        // load clipboard
        Clipboard clipboard = null;
        ClipboardFormat clipboardFormat = ClipboardFormats.findByFile(this.SCHEMATIC);
        try {
            assert clipboardFormat != null;
            try (ClipboardReader reader = clipboardFormat.getReader(new FileInputStream(this.SCHEMATIC))) {
                clipboard = reader.read();
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
            sendErrorMessage("Error loading schematic file: " + this.SCHEMATIC.getAbsolutePath()
                    + "in chunk " + this);
        }

        if (clipboard == null) {
            return;
        }


        // remove players and other entities
        clearChunkEntities();

        // new EditSessionBuilder(world).changeSetNull().fastmode(true).build()

        // paste clipboard
        ChunkSnapshot chunk = CHUNK.getSnapshot();
        BlockVector3 pos = BlockVector3.at(chunk.getX() * 16, -64, chunk.getZ() * 16);
        World world = FaweAPI.getWorld(chunk.getWorldName());
        clipboard.setOrigin(pos);
        try (EditSession session = WorldEdit.getInstance().newEditSessionBuilder()
                .world(world).changeSetNull().fastMode(true).build()) {
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(session)
                    .to(pos)
                    .ignoreAirBlocks(false)
                    .build();
            Operations.complete(operation);
            Operations.complete(clipboard.commit());
        } catch (WorldEditException e) {
            e.printStackTrace();
            sendErrorMessage("Error pasting schematic: " + e + " for " + this);
        }
    }

    private void checkPlayer(Player player) {
        ScoreboardHandler handler = new ScoreboardHandler();
        String leaveTag = String.format(PLUGIN.getConfig().getString("LeaveTag", "leave_%s"), arena.getName());
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

    /**
     * Removes entities from this chunk, except for
     * players, markers, armour stands, and hanging entities.
     */
    private void clearChunkEntities() {
        for (Entity entity : CHUNK.getChunk().getEntities()) {
            if (entity instanceof Player) {
                checkPlayer((Player) entity);
            } else if ((entity instanceof Item) || (entity instanceof LivingEntity
                    && !(entity instanceof ArmorStand))) {
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
        Bukkit.getLogger().log(Level.SEVERE, message);
    }

}
