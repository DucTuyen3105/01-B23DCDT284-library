package dao;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/** Shared JDBC configuration. Each operation owns and closes its connection. */
public abstract class DAO4 {
    protected Connection connection;
    protected DAO4() { }
    protected DAO4(Connection connection) { this.connection = connection; }

    protected Connection openConnection() throws SQLException {
        return connection == null ? getConnection() : connection;
    }

    protected void closeOwnedConnection(Connection opened) throws SQLException {
        if (connection == null) opened.close();
    }
    private static final Properties CONFIG = loadConfig();

    private static Properties loadConfig() {
        Properties config = new Properties();
        try (InputStream in = DAO4.class.getResourceAsStream("/db.properties")) {
            if (in == null) throw new IllegalStateException("Missing db.properties");
            config.load(in);
            Class.forName("com.mysql.cj.jdbc.Driver");
            return config;
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Cannot initialize JDBC configuration", e);
        }
    }

    private static String value(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null) value = System.getenv(key);
        return value == null ? CONFIG.getProperty(fallback) : value;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(value("DB_URL", "db.url"),
                value("DB_USER", "db.user"), value("DB_PASSWORD", "db.password"));
    }
}
