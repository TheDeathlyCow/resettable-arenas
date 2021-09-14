/**
 * Database code derived from resource by pablo67340
 *
 * https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */
package com.github.thedeathlycow.resettablearenas.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLite extends Database {

    private final String databaseName;

    public SQLite() {
        this.databaseName = plugin.getConfig().getString("SQLite.Filename", "ResettableArenasSQLite");
    }

    private String createArenaTable = "CREATE TABLE IF NOT EXISTS " + arenasTable + " (" +
            "arenaName VARCHAR(13) NOT NULL CONSTRAINT isValidName CHECK (arenaName REGEXP '^[a-zA-Z][a-zA-Z0-9_\\-+]{0,12}$') PRIMARY KEY," +
            "saveVer INT," +
            "loadVer INT," +
            "PRIMARY KEY (arenaName)" +
            ");";

    private String createArenaChnkTable = "CREATE TABLE IF NOT EXISTS " + arenaChunksTable + " (" +
            "worldName VARCHAR(32) NOT NULL," +
            "posX INT NOT NULL," +
            "posZ INT NOT NULL," +
            "saveVer INT," +
            "loadVer INT," +
            "arenaName VARCHAR(13) REFERENCES " + arenasTable + "," +
            "CONSTRAINT arenaChunkPrimaryKey PRIMARY KEY (worldName, posX, posZ)" +
            ");";

    @Override
    public Connection getSQLConnection() {
        File dataFolder = new File(plugin.getDataFolder(), this.databaseName + ".db");
        if (!dataFolder.exists()){
            try {
                dataFolder.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "File write error: " + databaseName + ".db");
            }
        }
        try {
            if (dbConnection != null && !dbConnection.isClosed()){
                return dbConnection;
            }
            Class.forName("org.sqlite.JDBC");
            dbConnection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return dbConnection;
        } catch (SQLException ex) {
            plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
        }
        return null;
    }

    @Override
    public void load() {
        dbConnection = getSQLConnection();
        try {
            Statement createArena = dbConnection.createStatement();
            createArena.executeUpdate(createArenaTable);
            createArena.close();
            Statement createArenaChunk = dbConnection.createStatement();
            createArenaChunk.executeUpdate(createArenaChnkTable);
            createArenaChunk.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        initialize();
    }
}
