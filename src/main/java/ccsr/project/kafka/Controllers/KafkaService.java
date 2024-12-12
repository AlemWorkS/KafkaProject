package ccsr.project.kafka.Controllers;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KafkaService {

    private final AdminClient adminClient;


    @Autowired
    public KafkaService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }
    // Récupération des topics correspondant à un centre d'intérêt
    public List<String> searchTopicsByInterest(String interest) {
        try {
            // Liste tous les topics du cluster
            ListTopicsResult topicsResult = adminClient.listTopics();
            Set<String> allTopics = topicsResult.names().get();

            // Filtre les topics par mot-clé (intérêt)
            return allTopics.stream()
                    .filter(topic -> topic.toLowerCase().contains(interest.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Récupération des messages depuis un topic spécifique
    public List<String> getMessagesFromTopic(String topicName) {
        List<String> messages = new ArrayList<>();
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "web-consumer-group-" + UUID.randomUUID());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            consumer.subscribe(Collections.singletonList(topicName));

            System.out.println("Connexion au topic : " + topicName);
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(5));

            for (ConsumerRecord<String, String> record : records) {
                messages.add(record.value());
            }

            if (messages.isEmpty()) {
                messages.add("Aucun message disponible pour le moment.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des messages : " + e.getMessage());
            messages.add("Erreur lors de la récupération des messages.");
        }
        return messages;
    }

    public static boolean verifyOrRegisterUser(String email, String username) {
        // Vérifie si l'utilisateur existe déjà
        String query = "SELECT * FROM users WHERE email = ?";
        String insertQuery = "INSERT INTO users (email, username) VALUES (?, ?)";

        try (Connection connection = DatabaseConnection.getConnection()) {
            System.out.println("Connexion réussie à la base de données");

            try (PreparedStatement checkStatement = connection.prepareStatement(query)) {
                checkStatement.setString(1, email);
                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    System.out.println("Utilisateur trouvé : " + email);
                    return true;
                } else {
                    System.out.println("Utilisateur non trouvé, insertion en cours : " + email);
                    try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                        insertStatement.setString(1, email);
                        insertStatement.setString(2, username);
                        insertStatement.executeUpdate();
                        System.out.println("Utilisateur enregistré avec succès : " + email);
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur SQL : " + e.getMessage());
            return false;
        }

    }


    // Abonnement d'un consumer à un topic
    public static boolean subscribeUserToTopic(String email, String topicName) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO subscription (email,topic_name) VALUES (?,?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, topicName);
            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void publishMessage(String topicName, String message) throws Exception {
        Producer<String, String> producer = getProducer();
        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, message);
        producer.send(record);
        producer.close();
    }

    public static Producer<String, String> getProducer() {
        // Crée un objet Properties pour stocker les configurations nécessaires du producteur Kafka
        Properties props = new Properties();

        // Définir l'adresse du broker Kafka auquel le producteur doit se connecter
        // Ici, on utilise "localhost:9092", ce qui signifie que Kafka est exécuté localement sur le port 9092.
        props.put("bootstrap.servers", "localhost:9092");

        // Configurer le sérialiseur pour les clés des messages.
        // Ce sérialiseur transforme les clés en chaînes de caractères pour qu'elles soient compréhensibles par Kafka.
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Configurer le sérialiseur pour les valeurs des messages.
        // De même, cela transforme les valeurs en chaînes de caractères compréhensibles par Kafka.
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        // Crée et retourne une instance de KafkaProducer en utilisant les propriétés configurées.
        // Ce producteur sera utilisé pour envoyer des messages à des topics Kafka.
        return new KafkaProducer<>(props);
    }


}
