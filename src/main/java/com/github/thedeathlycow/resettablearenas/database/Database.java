/**
 * Database code derived from resource by pablo67340
 * <p>
 * https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */
package com.github.thedeathlycow.resettablearenas.database;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.*;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.IOException;
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
        PreparedStatement arenaStatement = null;
        ResultSet arenaResultSet = null;
        PreparedStatement arenaChnkStatement = null;
        ResultSet arenaChnkResultSet = null;
        try {
            arenaStatement = dbConnection.prepareStatement("SELECT * FROM " + arenasTable + ";");
            arenaResultSet = arenaStatement.executeQuery();

            arenaChnkStatement = dbConnection.prepareStatement("SELECT * FROM " + arenaChunksTable + ";");
            arenaChnkResultSet = arenaStatement.executeQuery();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", ex);
        } finally {
            close(arenaStatement, arenaResultSet);
            close(arenaChnkStatement, arenaChnkResultSet);
        }
    }

    public List<Arena> getAllArenas() {
        List<Arena> result = new ArrayList<>();
        Connection conn = getSQLConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String query = "SELECT * FROM " + arenasTable + ";";
        try {
            statement = conn.prepareStatement(query);
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String resultName = resultSet.getString("arenaName");
                Arena arena = new Arena(resultName);
                arena.setSaveVersion(resultSet.getInt("saveVer"));
                arena.setLoadVersion(resultSet.getInt("loadVer"));
                result.add(arena);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to process query", e);
        } finally {
            close(conn, statement, resultSet);
        }
        return result;
    }

    public List<ArenaChunk> getAllChunks(Arena parentArena) {
        List<ArenaChunk> result = new ArrayList<>();
        Connection conn = getSQLConnection();
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        String query = "SELECT * FROM " + arenaChunksTable +
                " WHERE arenaName = '" + parentArena.getName() + "';";
        try {
            statement = conn.prepareStatement(query);
            resultSet = statement.executeQuery();
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
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to process query", e);
        } finally {
            close(conn, statement, resultSet);
        }
        return result;
    }


    @Nullable
    public Arena getArena(String arenaName) {
        Connection conn = getSQLConnection();
        ResultSet resultSet = null;
        String query = "SELECT * FROM " + arenasTable + " WHERE arenaName = '" + arenaName + "';";
        Arena resultArena = null;
        try (PreparedStatement statement = conn.prepareStatement(query)) {
            resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String resultName = resultSet.getString("arenaName");
                if (resultName.equals(arenaName)) {
                    System.out.println("Found arena '" + resultName + "'");
                    resultArena = new Arena(resultName);
                    resultArena.setSaveVersion(resultSet.getInt("saveVer"));
                    resultArena.setLoadVersion(resultSet.getInt("loadVer"));
                    break;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            close(conn, resultSet);
        }
        return resultArena;
    }

    @Nullable
    public ArenaChunk getArenaChunk(ChunkSnapshot snapshot) {
        Connection conn = getSQLConnection();
        ResultSet result = null;
        String worldName = snapshot.getWorldName();
        int posX = snapshot.getX();
        int posZ = snapshot.getZ();

        String query = "SELECT * FROM " + arenaChunksTable + " WHERE worldName = '" + worldName + "'" +
                " AND posX = " + posX +
                " AND posZ = " + posZ + ";";

        ArenaChunk found = null;
        try (PreparedStatement statement = conn.prepareStatement(query)){
            result = statement.executeQuery();
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
                        found = new ArenaChunk(parentArena, snapshot);
                        System.out.println("Found arena chunk '" + found.toString() + "'");
                        found.setSaveVersion(saveVer);
                        found.setLoadVersion(loadVer);

                        break;
                    }
                }
            }
        } catch (SQLException ex) {
//            ResettableArenas.getInstance().getServer().broadcastMessage(ChatColor.RED + "Error: " + ex.getMessage() + "\n" +
//                    query);
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(conn, result);
        }
        return found;
    }

    public void updateArena(Arena arena, String col, int value) throws SQLException {
        Connection connection = getSQLConnection();
        SQLException exception = null;
        String query = "UPDATE " + arenasTable +
                " SET " + col + " = " + value +
                " WHERE arenaName = '" + arena.getName() + "';";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(query);
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
            exception = e;
        } finally {
            close(connection);
        }

        if (exception != null) {
            throw exception;
        }
    }

    public void updateChunk(ArenaChunk arenaChunk, String col, int value) {
        Connection connection = getSQLConnection();
        String query = "UPDATE " + arenaChunksTable +
                " SET " + col + " = " + value +
                " WHERE worldName = '" + arenaChunk.getWorldname() + "' AND " +
                " posX = " + arenaChunk.getPosX() + " AND " +
                " posZ = " + arenaChunk.getPosZ() + ";";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(query);
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            close(connection);
        }
    }

    public void addArena(Arena arena) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        SQLException exception = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + arenasTable + " (arenaName, saveVer, loadVer) VALUES (?, ?, ?)");
            ps.setString(1, arena.getName());
            ps.setInt(2, arena.getSaveVersion());
            ps.setInt(3, arena.getLoadVersion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
            exception = ex;
        } finally {
            close(ps, conn);
        }
        if (exception != null) {
            throw exception;
        }
    }

    public void addArenaChunk(ArenaChunk chunk) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " +
                    arenaChunksTable +
                    " (worldName, posX, posZ, saveVer, loadVer, arenaName) VALUES (?, ?, ?, ?, ?, ?)");
            ps.setString(1, chunk.getWorldname());
            ps.setInt(2, chunk.getPosX());
            ps.setInt(3, chunk.getPosZ());
            ps.setInt(4, chunk.getSaveVersion());
            ps.setInt(5, chunk.getLoadVersion());
            ps.setString(6, chunk.getArena().getName());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
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
            this.dbConnection.close();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), e);
        }
    }
}