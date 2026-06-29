package com.scrafms.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DatabaseConnection — thread-safe Singleton that provides JDBC connections to SQL Server.
 *
 * GRASP Pattern: N/A (infrastructure class)
 * GoF Pattern: Singleton — exactly one DatabaseConnection instance per JVM; double-checked locking
 * Layer: Data Access (Infrastructure)
 *
 * UC: All use cases (every repository depends on this class)
 */
public class DatabaseConnection {

    private static volatile DatabaseConnection instance;
    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new RuntimeException("db.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load db.properties", e);
        }
        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");
    }

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
