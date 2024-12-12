package ccsr.project.kafka.Controllers;

import java.sql.*;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://localhost:3308/Omegabd";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "";
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    public static void main(String[] args) {
        Connection connection = null;

        try {
            // Charger le driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Se connecter à la base de données
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connexion réussie à la base de données !");

        } catch (ClassNotFoundException e) {
            System.out.println("Erreur : Le driver JDBC n'a pas été trouvé !");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Erreur : Impossible de se connecter à la base de données !");
            e.printStackTrace();
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                    System.out.println("Connexion fermée.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
