package com.laundrylink.database;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gives out connections to the SQLite database file.
 * Every DAO method opens a connection with try-with-resources,
 * uses it, and closes it automatically.
 */
public class DatabaseConnection {

    public static final String DB_FILE = "laundrylink.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private DatabaseConnection() {
        // utility class, no objects needed
    }

    public static Connection getConnection() throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.enforceForeignKeys(true); // SQLite ignores foreign keys unless this is ON
        config.setBusyTimeout(5000);     // wait up to 5s if another thread is writing
        return DriverManager.getConnection(DB_URL, config.toProperties());
    }
}