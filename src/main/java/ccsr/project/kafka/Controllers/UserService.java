package ccsr.project.kafka.Controllers;


import ccsr.project.kafka.Controllers.DatabaseConnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class UserService {

    /**
     * Enregistre un nouvel utilisateur dans la base de données.
     *
     * @param firstName Prénom de l'utilisateur.
     * @param lastName  Nom de l'utilisateur.
     * @param email     Email de l'utilisateur.
     * @param password  Mot de passe de l'utilisateur.
     * @param userName  nom utilisateur.
     * @return true si l'enregistrement a réussi, false sinon.
     */


        public boolean registerUser(String firstName, String lastName, String email, String password, String userName) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String query = "INSERT INTO users (first_name, last_name, email, password, username) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, firstName);
                statement.setString(2, lastName);
                statement.setString(3, email);
                statement.setString(4, password);
                statement.setString(5, userName); // Ajout du userName
                int rowsInserted = statement.executeUpdate();
                return rowsInserted > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }



    /**
     * Authentifie un utilisateur en vérifiant ses informations dans la base de données.
     *
     * @param email    Email de l'utilisateur.
     * @param password Mot de passe de l'utilisateur.
     * @return true si les informations sont valides, false sinon.
     */
    public boolean authenticateUser(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Définir les paramètres de la requête
            statement.setString(1, email);
            statement.setString(2, password);

            // Exécuter la requête
            ResultSet resultSet = statement.executeQuery();

            // Vérifier si un utilisateur correspondant est trouvé
            return resultSet.next();
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'authentification de l'utilisateur : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
