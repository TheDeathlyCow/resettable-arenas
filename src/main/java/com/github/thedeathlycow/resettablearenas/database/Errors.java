/**
 * Database code derived from resource by pablo67340
 * <p>
 * https://www.spigotmc.org/threads/how-to-sqlite.56847/
 */
package com.github.thedeathlycow.resettablearenas.database;

public class Errors {
    public static String sqlConnectionExecute(){
        return "Couldn't execute SQLite statement: ";
    }
    public static String sqlConnectionClose(){
        return "Failed to close SQLite connection: ";
    }
    public static String noSQLConnection(){
        return "Unable to retreive SQLite connection: ";
    }
    public static String noTableFound(){
        return "Database Error: No Table Found";
    }
}
