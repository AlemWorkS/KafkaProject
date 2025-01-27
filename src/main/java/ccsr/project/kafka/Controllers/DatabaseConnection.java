package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.config.Config;
import io.github.cdimascio.dotenv.Dotenv;

import java.sql.*;

public class DatabaseConnection {

    private static final String URL = "jdbc:mysql://"+ Config.BD_CONFIG.get("db_host")+":"+Config.BD_CONFIG.get("db_port")+"/"+Config.BD_CONFIG.get("db_name");
    private static final String USERNAME = Config.BD_CONFIG.get("db_user");
    private static final String PASSWORD = Config.BD_CONFIG.get("db_password");


    public static Connection getConnection() throws SQLException {
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
        }

        return DriverManager.getConnection(URL, USERNAME, PASSWORD);

    }

    static{
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
