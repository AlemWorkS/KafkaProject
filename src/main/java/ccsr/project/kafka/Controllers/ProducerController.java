package ccsr.project.kafka.Controllers;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/producer")
public class ProducerController {

    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(
            @RequestParam String topicName,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String link,
            @RequestParam String serverAddress) {

        // Validation : Message ou lien, mais pas les deux
        if ((message == null || message.isBlank()) && (link == null || link.isBlank())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous devez fournir soit un message, soit un lien.");
        }

        if (message != null && !message.isBlank() && link != null && !link.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous ne pouvez pas envoyer un message et un lien en même temps.");
        }

        // Nettoyage du nom du topic
        String sanitizedTopicName = sanitizeTopicName(topicName);

        // Configuration du Kafka Producer
        Properties props = new Properties();
        props.put("bootstrap.servers", serverAddress);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            // Préparation du message à envoyer
            String content = message != null ? message : "Lien : " + link;

            // Vérifier si le topic existe
            if (!topicExists(serverAddress, sanitizedTopicName)) {
                createTopicIfNotExists(serverAddress, sanitizedTopicName);
            }

            ProducerRecord<String, String> record = new ProducerRecord<>(sanitizedTopicName, content);
            producer.send(record);
            return ResponseEntity.ok("Message envoyé avec succès au topic \"" + sanitizedTopicName + "\".");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de l'envoi du message.");
        }

    }

    // Vérifie si un topic existe
    private boolean topicExists(String serverAddress, String topicName) throws Exception {
        Properties config = new Properties();
        config.put("bootstrap.servers", serverAddress);
        try (AdminClient adminClient = AdminClient.create(config)) {
            return adminClient.listTopics().names().get().contains(topicName);
        }
    }

    // Méthode pour créer un topic s'il n'existe pas
    private void createTopicIfNotExists(String serverAddress, String topicName) throws Exception {
        Properties config = new Properties();
        config.put("bootstrap.servers", serverAddress);
        try (AdminClient adminClient = AdminClient.create(config)) {
            if (!adminClient.listTopics().names().get().contains(topicName)) {
                adminClient.createTopics(Collections.singletonList(new NewTopic(topicName, 1, (short) 1)));
            }
        }
    }

    // Nettoyage du nom du topic
    private String sanitizeTopicName(String topicName) {
        return topicName.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
    }
}