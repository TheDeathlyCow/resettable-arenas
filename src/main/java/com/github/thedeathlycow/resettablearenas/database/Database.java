/**
 * Database code derived from resource by pablo67340
 * <p>
 * https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */
package com.github.thedeathlycow.resettablearenas.database;

import com.github.thedeathlycow.resettablearenas.Arena;
import com.github.thedeathlycow.resettablearenas.ArenaChunk;
import com.github.thedeathlycow.resettablearenas.ResettableArenas;
import org.bukkit.ChunkSnapshot;
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
        ResultSet resultSet = null;
        try {
            resultSet = runQuery("SELECT * FROM " + arenasTable + ";");
            while (resultSet.next()) {
                String resultName = resultSet.getString("name");
                result.add(new Arena(resultName));
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to process query", e);
        } finally {
            try {
                if (result != null) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                Error.close(plugin, e);
            }
        }
        return result;
    }

    private ResultSet runQuery(String sql) {
        Connection conn = getSQLConnection();
        ResultSet resultSet = null;
        PreparedStatement statement = null;
        try {
            statement = conn.prepareStatement(sql);
            resultSet = statement.executeQuery();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Unable to retrieve connection", e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException e) {
                Error.close(plugin, e);
            }
        }
        return resultSet;
    }

    public Arena getArena(Arena arena) {
        return getArena(arena.getName());
    }

    @Nullable
    public Arena getArena(String arenaName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet result = runQuery("SELECT * FROM " + arenasTable + " WHERE arenaName = '" + arenaName + "';");
        try {
            while (result.next()) {
                String resultName = result.getString("name");
                if (resultName.equals(arenaName)) {
                    return new Arena(resultName);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), e);
        } finally {
            close(result);
        }
//        try {
//            conn = getSQLConnection();
//            ps = conn.prepareStatement("SELECT * FROM " + arenasTable + " WHERE arenaName = '" + arenaName + "';");
//            result = ps.executeQuery();
//            while (result.next()) {
//                String resultName = result.getString("name");
//                if (resultName.equals(arenaName)) {
//                    return new Arena(resultName);
//                }
//            }
//        } catch (SQLException ex) {
//            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
//        } finally {
//            close(ps, conn);
//        }
        return null;
    }

    @Nullable
    public ArenaChunk getArenaChunk(ChunkSnapshot snapshot) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String worldName = snapshot.getWorldName();
        int posX = snapshot.getX();
        int posZ = snapshot.getZ();

        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("SELECT * FROM " + arenaChunksTable + " WHERE worldName = '" + worldName + "' " +
                    "AND posX = " + posX + " " +
                    "AND posZ = " + posZ + ";");
            result = ps.executeQuery();
            while (result.next()) {
                String resWorldName = result.getString("worldName");
                int resX = result.getInt("posX");
                int resZ = result.getInt("posZ");
                if (resWorldName.equals(worldName)
                        && resX == posX
                        && resZ == posZ) {
                    String arenaName = result.getString("arenaName");
                    Arena parentArena = getArena(arenaName);
                    if (parentArena == null) {
                        return null;
                    }
                    ArenaChunk found = new ArenaChunk(parentArena, snapshot);
                    int saveVer = result.getInt("saveVer");
                    int loadVer = result.getInt("loadVer");
                    found.setSaveVersion(saveVer);
                    found.setLoadVersion(loadVer);
                    return found;
                }
            }
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
        }
        return null;
    }

    public void addArena(Arena arena) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getSQLConnection();
            ps = conn.prepareStatement("REPLACE INTO " + arenasTable + " (arenaName, saveVer, loadVer) VALUES (?, ?, ?)");
            ps.setString(1, arena.getName());
            ps.setInt(2, arena.getSaveVersion());
            ps.setInt(3, arena.getLoadVersion());
            ps.executeUpdate();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
        } finally {
            close(ps, conn);
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

    private void close(PreparedStatement preparedStatement, Connection connection) {
        try {
            if (preparedStatement != null)
                preparedStatement.close();
            if (connection != null)
                connection.close();
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
        }
    }

    private void close(PreparedStatement ps, ResultSet rs) {
        try {
            if (ps != null)
                ps.close();
            if (rs != null)
                rs.close();
        } catch (SQLException ex) {
            Error.close(plugin, ex);
        }
    }

    private void close(ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
        } catch (SQLException ex) {
            Error.close(plugin, ex);
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