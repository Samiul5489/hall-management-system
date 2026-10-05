package com.hallmanagement.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static final String CONFIG_FILE = "/config.properties";

    private static final Properties CONFIG = new Properties();

    static {
        try (InputStream in = DBConnection.class.getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                throw new ExceptionInInitializerError(
                    "config.properties not found on classpath. " +
                    "Make sure it exists at src/main/resources/config.properties"
                );
            }
            CONFIG.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(
                "Failed to load config.properties: " + e.getMessage()
            );
        }
    }

    private DBConnection() {}

    public static Connection getConnection() throws SQLException {
        String host     = CONFIG.getProperty("db.host", "localhost");
        String port     = CONFIG.getProperty("db.port", "3306");
        String name     = CONFIG.getProperty("db.name", "hall_management");
        String user     = CONFIG.getProperty("db.user", "root");
        String password = CONFIG.getProperty("db.password", "");
        String useSSL   = CONFIG.getProperty("db.useSSL", "false");
        String allowPKR = CONFIG.getProperty("db.allowPublicKeyRetrieval", "true");
        String tz       = CONFIG.getProperty("db.serverTimezone", "UTC");

        String url = String.format(
            "jdbc:mysql://%s:%s/%s?useSSL=%s&allowPublicKeyRetrieval=%s&serverTimezone=%s",
            host, port, name, useSSL, allowPKR, tz
        );

        return DriverManager.getConnection(url, user, password);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection] Connectivity test failed: " + e.getMessage());
            return false;
        }
    }
}
