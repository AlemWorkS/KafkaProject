import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Configuration Kafka (déjà dans votre fichier application.properties)
        String bootstrapServers = "localhost:29092,localhost:39093,localhost:49094";

        System.out.println("Bienvenue dans le service Kafka.");
        System.out.println("Êtes-vous un nouvel utilisateur ou existant ? (nouveau/existant)");
        String status = scanner.nextLine();

        String consumerId = "";
        List<String> interests = new ArrayList<>();

        if (status.equalsIgnoreCase("nouveau")) {
            System.out.println("Veuillez entrer un identifiant unique : ");
            consumerId = scanner.nextLine();

            System.out.println("Définissez vos centres d'intérêt (séparés par des virgules) : ");
            String interestsInput = scanner.nextLine();
            interests = Arrays.asList(interestsInput.split(","));

            System.out.println("Vos centres d'intérêt ont été enregistrés.");
        } else if (status.equalsIgnoreCase("existant")) {
            System.out.println("Veuillez entrer votre identifiant : ");
            consumerId = scanner.nextLine();

            // Simule la récupération des centres d'intérêt pour un utilisateur existant


            if (interests.isEmpty()) {
                System.out.println("Aucun centre d'intérêt enregistré pour cet identifiant.");
                return;
            }
        } else {
            System.out.println("Option invalide. Veuillez réessayer.");
            return;
        }

        System.out.println("Voici vos centres d'intérêt : " + interests);

        // Connexion au cluster Kafka pour récupérer les topics
        System.out.println("Récupération des topics disponibles...");
        List<String> availableTopics = getTopics(bootstrapServers);

        System.out.println("Topics disponibles : " + availableTopics);

        // Filtrer les topics par centres d'intérêt
        List<String> subscribedTopics = new ArrayList<>();
        for (String interest : interests) {
            if (availableTopics.contains(interest)) {
                subscribedTopics.add(interest);
            }
        }

        if (subscribedTopics.isEmpty()) {
            System.out.println("Aucun topic correspondant à vos centres d'intérêt.");
            return;
        }

        System.out.println("Vous êtes abonné aux topics suivants : " + subscribedTopics);

        // Consommer les messages des topics abonnés
        consumeMessages(bootstrapServers, subscribedTopics);
    }

    // Méthode pour récupérer les topics depuis le cluster
    private static List<String> getTopics(String bootstrapServers) {
        List<String> topics = new ArrayList<>();
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(config)) {
            topics = new ArrayList<>(adminClient.listTopics(new ListTopicsOptions().timeoutMs(5000)).names().get());
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des topics : " + e.getMessage());
        }

        return topics;
    }

    // Méthode pour consommer les messages des topics
    private static void consumeMessages(String bootstrapServers, List<String> topics) {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "consumer-group-" + UUID.randomUUID());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties)) {
            // S'abonner aux topics
            consumer.subscribe(topics);

            System.out.println("En attente des messages...");
            boolean messageReceived = false; // Permet de vérifier si un message a été reçu

            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Message  " + record.topic() + ":=>   " + record.value());
                    messageReceived = true; // Réinitialise lorsque des messages sont reçus
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la consommation des messages : " + e.getMessage());
        }
    }

}
