package ccsr.project.kafka.Controllers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ccsr.project.kafka.Controllers.DatabaseConnection;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionService {
    public void addSubscription(String userEmail, String topicName) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String messageContent = "Abonné"; // Valeur par défaut
        try {
            connection = DatabaseConnection.getConnection();

            // Vérifiez si le topic existe déjà
            String getTopicIdQuery = "SELECT topics_id FROM topics WHERE name = ?";
            preparedStatement = connection.prepareStatement(getTopicIdQuery);
            preparedStatement.setString(1, topicName);
            resultSet = preparedStatement.executeQuery();

            int topicId;
            if (resultSet.next()) {
                // Si le topic existe, récupérez son ID
                topicId = resultSet.getInt("topics_id");
            } else {
                // Sinon, insérez le topic et récupérez son ID
                String insertTopicQuery = "INSERT INTO topics (name) VALUES (?)";
                preparedStatement = connection.prepareStatement(insertTopicQuery, PreparedStatement.RETURN_GENERATED_KEYS);
                preparedStatement.setString(1, topicName);
                preparedStatement.executeUpdate();

                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next()) {
                    topicId = resultSet.getInt(1);
                } else {
                    throw new SQLException("Erreur lors de la création du topic.");
                }
            }

            // Enregistrez la souscription
            String insertSubscriptionQuery = "INSERT INTO subscription (email, topics_id,message_content,created_at) VALUES (?, ?,?, NOW())";
            preparedStatement = connection.prepareStatement(insertSubscriptionQuery);
            preparedStatement.setString(1, userEmail);
            preparedStatement.setInt(2, topicId);
            preparedStatement.setString(3, "Abonné");

            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            messageContent = "Échoué";
            throw new SQLException("Erreur lors de l'insertion dans la table subscription : " + e.getMessage(), e);
        } finally {
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (connection != null) connection.close();
        }
    }
    public static List<String> getSubscribersEmailsForTopic(String topicName) {
        List<String> emails = new ArrayList<>();
        String query = "SELECT email FROM subscription WHERE topics_id = (SELECT topics_id FROM topics WHERE name = ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, topicName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                emails.add(resultSet.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return emails;
    }



}
