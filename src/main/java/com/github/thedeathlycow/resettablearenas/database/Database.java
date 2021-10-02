/**
 * Database code derived from resource by pablo67340
 * <p>
 * https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */
package com.github.thedeathlycow.resettablearenas.database;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public abstract class Database {
    protected ResettableArenas plugin;
    protected Connection dbConnection;

    protected String arenasTable = "arena";
    protected String arenaChunksTable = "arena_chunk";

    public Database() {
        plugin = ResettableArenas.getInstance();
    }

    public abstract Connection getSQLConnection();

    public abstract void load();

    protected void initialize() {
        dbConnection = getSQLConnection();
        try (PreparedStatement arenaStatement = dbConnection.prepareStatement("SELECT * FROM " + arenasTable + ";");
             PreparedStatement arenaChnkStatement = dbConnection.prepareStatement("SELECT * FROM " + arenaChunksTable + ";");) {
            try (ResultSet arenaResultSet = arenaStatement.executeQuery()) {
                plugin.getLogger().log(Level.FINE, "Arena table is working.");
            }
            try (ResultSet arenaChnkResultSet = arenaStatement.executeQuery()) {
                plugin.getLogger().log(Level.FINE, "Arena chunk table is working.");
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        }
    }

    public List<Arena> getAllArenas() {
        List<Arena> result = new ArrayList<>();
        String query = "SELECT * FROM " + arenasTable + ";";
        try (Connection connection = getSQLConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String resultName = resultSet.getString("arenaName");
                    Arena arena = new Arena(resultName);
                    arena.setSaveVersion(resultSet.getInt("saveVer"), false);
                    arena.setLoadVersion(resultSet.getInt("loadVer"), false);
                    result.add(arena);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to process query", e);
        }
        return result;
    }

    public List<ArenaChunk> getAllChunks(Arena parentArena) {
        List<ArenaChunk> result = new ArrayList<>();
        String query = "SELECT * FROM " + arenaChunksTable +
                " WHERE arenaName = '" + parentArena.getName() + "';";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int posX = resultSet.getInt("posX");
                    int posZ = resultSet.getInt("posZ");
                    World world = Bukkit.getWorld(resultSet.getString("worldName"));
                    ChunkSnapshot chunk = world.getEmptyChunkSnapshot(posX, posZ, false, false);
                    ArenaChunk arenaChunk = new ArenaChunk(parentArena, chunk);
                    arenaChunk.setSaveVersion(resultSet.getInt("saveVer"));
                    arenaChunk.setLoadVersion(resultSet.getInt("loadVer"));
                    result.add(arenaChunk);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to process query", e);
        }
        return result;
    }


    @Nullable
    public Arena getArena(String arenaName) {
        String query = "SELECT * FROM " + arenasTable + " WHERE arenaName = '" + arenaName + "';";
        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String resultName = resultSet.getString("arenaName");
                    if (resultName.equals(arenaName)) {
                        Arena resultArena = new Arena(resultName);
                        resultArena.setSaveVersion(resultSet.getInt("saveVer"), false);
                        resultArena.setLoadVersion(resultSet.getInt("loadVer"), false);
                        return resultArena;
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
        return null;
    }

    @Nullable
    public ArenaChunk getArenaChunk(ChunkSnapshot snapshot) {
        String worldName = snapshot.getWorldName();
        int posX = snapshot.getX();
        int posZ = snapshot.getZ();

        String query = "SELECT * FROM " + arenaChunksTable + " WHERE worldName = '" + worldName + "'" +
                " AND posX = " + posX +
                " AND posZ = " + posZ + ";";

        try (PreparedStatement statement = dbConnection.prepareStatement(query)) {
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    String resWorldName = result.getString("worldName");
                    int resX = result.getInt("posX");
                    int resZ = result.getInt("posZ");
                    int saveVer = result.getInt("saveVer");
                    int loadVer = result.getInt("loadVer");
                    if (resWorldName.equals(worldName)
                            && resX == posX
                            && resZ == posZ) {
                        String arenaName = result.getString("arenaName");
                        Arena parentArena = getArena(arenaName);
                        if (parentArena != null) {
                            ArenaChunk found = new ArenaChunk(parentArena, snapshot);
                            found.setSaveVersion(saveVer);
                            found.setLoadVersion(loadVer);
                            return found;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
        return null;
    }

    public void updateArena(Arena arena) throws SQLException {
        updateArena(arena, "saveVer", arena.getSaveVersion());
        updateArena(arena, "loadVer", arena.getLoadVersion());
    }

    public void updateArena(Arena arena, String col, int value) throws SQLException {
        SQLException exception = null;
        String query = "UPDATE " + arenasTable +
                " SET " + col + " = " + value +
                " WHERE arenaName = '" + arena.getName() + "';";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(query);
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
            exception = e;
        }

        if (exception != null) {
            throw exception;
        }
    }

    public void updateChunk(ArenaChunk arenaChunk, String col, int value) {
        String query = "UPDATE " + arenaChunksTable +
                " SET " + col + " = " + value +
                " WHERE worldName = '" + arenaChunk.getWorldname() + "' AND " +
                " posX = " + arenaChunk.getPosX() + " AND " +
                " posZ = " + arenaChunk.getPosZ() + ";";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(query);
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        }
    }

    public void addArena(Arena arena) throws SQLException {
        SQLException exception = null;
        String query = "REPLACE INTO " + arenasTable + " (arenaName, saveVer, loadVer) VALUES (?, ?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setString(1, arena.getName());
            ps.setInt(2, arena.getSaveVersion());
            ps.setInt(3, arena.getLoadVersion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            exception = ex;
        }
        if (exception != null) {
            throw exception;
        }
    }

    public void addArenaChunk(ArenaChunk chunk) {
        String query = "REPLACE INTO " + arenaChunksTable +
                " (worldName, posX, posZ, saveVer, loadVer, arenaName) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = dbConnection.prepareStatement(query)) {
            ps.setString(1, chunk.getWorldname());
            ps.setInt(2, chunk.getPosX());
            ps.setInt(3, chunk.getPosZ());
            ps.setInt(4, chunk.getSaveVersion());
            ps.setInt(5, chunk.getLoadVersion());
            ps.setString(6, chunk.getArena().getName());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        }
    }

    private void close(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
            }
        }
    }

    public void onDisable() {
        try {
            if (this.dbConnection != null) {
                this.dbConnection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
        }
    }
}