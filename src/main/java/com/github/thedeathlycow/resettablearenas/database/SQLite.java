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
            "arenaName VARCHAR(13) NOT NULL PRIMARY KEY," +
            "saveVer INT," +
            "loadVer INT" +
            ");";

    private String createArenaChnkTable = "CREATE TABLE IF NOT EXISTS " + arenaChunksTable + " (" +
            "worldName VARCHAR(32) NOT NULL," +
            "posX INT NOT NULL," +
            "posZ INT NOT NULL," +
            "saveVer INT," +
            "loadVer INT," +
            "arenaName VARCHAR(13) REFERENCES " + arenasTable + "," +
            "PRIMARY KEY (worldName, posX, posZ)" +
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
            Class.forName("org.sqlite.JDBC");
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
            return connection;
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
