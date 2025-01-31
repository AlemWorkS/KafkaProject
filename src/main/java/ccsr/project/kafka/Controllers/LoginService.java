package ccsr.project.kafka.Controllers;

import org.springframework.stereotype.Service;
import java.sql.*;

@Service
public class LoginService {

    /**
     * Enregistre un nouvel utilisateur dans la base de données.
     *
     * @param email     Email de l'utilisateur.
     * @param userName  Nom d'utilisateur.
     * @param firstName Prénom de l'utilisateur.
     * @param lastName  Nom de famille de l'utilisateur.
     * @param password  Mot de passe de l'utilisateur.
     * @param role      Rôle de l'utilisateur.
     * @return true si l'enregistrement a réussi, false sinon.
     */


    public boolean registerUser(String email, String userName, String firstName, String lastName, String password, String role) {
        String query = "INSERT INTO users (email, username, first_name, last_name, password, role) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, userName);
            statement.setString(3, firstName);
            statement.setString(4, lastName);
            statement.setString(5, password);
            statement.setString(6, role);

            return statement.executeUpdate() > 0; // Retourne true si au moins une ligne est insérée

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'inscription : " + e.getMessage());
            return false;
        }
    }

    /**
     * Authentifie un utilisateur en vérifiant ses informations dans la base de données.
     *
     * @param email    Email de l'utilisateur.
     * @param password Mot de passe de l'utilisateur.
     * @return Le rôle de l'utilisateur si authentifié, sinon null.
     */
    public String authenticateUser(String email, String password) {
        String query = "SELECT username, role FROM users WHERE email = ? AND password = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String username = resultSet.getString("username");
                    String role = resultSet.getString("role");

                    // Vérification des valeurs nulles
                    if (username == null || role == null) {
                        return null;  // Empêche le découpage de chaîne incorrect
                    }

                    return username + "," + role;
                }
            }

        } catch (SQLException e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
        }
        return null;
    }


    /**
     * Marque tous les mails comme lus pour un utilisateur donné.
     *
     * @param email Email de l'utilisateur.
     */
    private void markEmailsAsRead(String email) {
        String query = "UPDATE mailplanning SET mail_lu = ? WHERE user_mail = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setBoolean(1, true);
            statement.setString(2, email);
            statement.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur lors de la mise à jour des mails : " + e.getMessage());
        }
    }
}
