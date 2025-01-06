package ccsr.project.kafka.Controllers;

import java.sql.*;
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
            preparedStatement.close();

            String insertMailPlanningQuery = "INSERT INTO mailplanning (topic,interval_de_jour,heure_env,user_mail,mail_lu) VALUES (?,?,?,?,?)";
            preparedStatement = connection.prepareStatement(insertMailPlanningQuery);
            preparedStatement.setString(1, topicName);
            preparedStatement.setInt(2, 1);
            preparedStatement.setInt(3, 12);
            preparedStatement.setString(4, userEmail);
            preparedStatement.setBoolean(5, false);
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
        List<String> subscribers = new ArrayList<>();
        String query = "SELECT email FROM subscription WHERE topic_name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, topicName);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                subscribers.add(resultSet.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return subscribers;
    }

    // Service pour interagir avec la base de données concernant les abonnements
    public static List<String> getTopicsForUser(String userEmail) {
        List<String> topics = new ArrayList<>();
        String query = "SELECT topic_name FROM subscription WHERE email = ?"; // Requête corrigée

        System.out.println("Début de la récupération des topics pour l'utilisateur : " + userEmail);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            // Remplace le paramètre ? par l'email de l'utilisateur
            statement.setString(1, userEmail);

            System.out.println("Exécution de la requête : " + query);

            // Exécute la requête et récupère les résultats
            ResultSet resultSet = statement.executeQuery();

            // Vérifie si des résultats sont trouvés
            if (!resultSet.isBeforeFirst()) {
                System.out.println("Aucun topic trouvé pour l'utilisateur : " + userEmail);
            }

            while (resultSet.next()) {
                String topicName = resultSet.getString("topic_name");
                topics.add(topicName);
                System.out.println("Topic récupéré : " + topicName);
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération des topics pour l'utilisateur : " + userEmail);
            e.printStackTrace(); // Affiche la trace de l'erreur
        }

        System.out.println("Topics récupérés pour l'utilisateur : " + topics);

        // Retourne la liste des topics
        return topics;
    }

    public static boolean doesTopicExist(String topicName) {
        String query = "SELECT COUNT(*) FROM topics WHERE name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, topicName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void addTopic(String topicName) {
        String query = "INSERT INTO topics (name) VALUES (?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, topicName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isUserSubscribed(String email, String topicName) {
        String query = "SELECT COUNT(*) FROM subscription WHERE email = ? AND topic_name = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, topicName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void subscribeUserToTopic(String email, String topicName) {
        String query = "INSERT INTO subscription (email, topic_name) VALUES (?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            statement.setString(2, topicName);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}