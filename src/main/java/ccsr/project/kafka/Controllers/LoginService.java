package ccsr.project.kafka.Controllers;


import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class LoginService {

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


    public boolean registerUser(String email, String userName, String firstName, String lastName, String password, String role) {
        String query = "INSERT INTO users (email, username, first_name, last_name, password, role) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, email);       // Email
            statement.setString(2, userName);   // Username
            statement.setString(3, firstName);  // First Name
            statement.setString(4, lastName);   // Last Name
            statement.setString(5, password);   // Password
            statement.setString(6, role);       // Role

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;

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
     * @return true si les informations sont valides, false sinon.
     */
    public String authenticateUser(String email, String password) {
        String query = "SELECT role FROM users WHERE email = ? AND password = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String role = resultSet.getString("role");
                System.out.println("Utilisateur trouvé avec le rôle : " + role);
                return role; // Retourne le rôle
            } else {
                System.out.println("Aucun utilisateur trouvé pour "+email+" et ce mot de passe."+password);
                return null; // Aucun utilisateur trouvé
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Une erreur s'est produite
        }
}
}
