package ccsr.project.kafka.Controllers;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Service;

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


    // Abonnement d'un consumer à un topic
    public void subscribeConsumerToTopic(String consumerId, String topicName) {
        // Vous pouvez utiliser une base de données ou un stockage local pour enregistrer l'abonnement
        System.out.println("Consumer " + consumerId + " s'est abonné au topic " + topicName);
        // Logique supplémentaire à implémenter ici
    }
}
