package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.config.Config;
import java.sql.*;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://" + Config.BD_CONFIG.get("db_host") + ":" + Config.BD_CONFIG.get("db_port") + "/" + Config.BD_CONFIG.get("db_name");
    private static final String USERNAME = Config.BD_CONFIG.get("db_user");
    private static final String PASSWORD = Config.BD_CONFIG.get("db_password");

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC Driver not found", e);
        } catch (SQLException e) {
            throw new SQLException("Failed to connect to the database", e);
        }
    }

    static {
        try (Connection connection = getConnection()) {
            System.out.println("Database connection test successful.");
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
        }
    }
}
