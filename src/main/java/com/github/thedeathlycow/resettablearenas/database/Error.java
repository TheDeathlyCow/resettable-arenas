/**
 * Database code derived from resource by pablo67340
 * <p>
 * https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */
package com.github.thedeathlycow.resettablearenas.database;


import com.github.thedeathlycow.resettablearenas.ResettableArenas;

import java.util.logging.Level;

public class Error {
    public static void execute(ResettableArenas plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Couldn't execute SQLite statement: ", ex);
    }
    public static void close(ResettableArenas plugin, Exception ex){
        plugin.getLogger().log(Level.SEVERE, "Failed to close SQLite connection: ", ex);
    }
}
