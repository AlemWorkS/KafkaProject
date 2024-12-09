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
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String link,
            @RequestParam String theme,
            @RequestParam String category,
            @RequestParam String serverAddress) {

        // Validation : Message ou lien, mais pas les deux
        if ((message == null || message.isBlank()) && (link == null || link.isBlank())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous devez fournir soit un message, soit un lien.");
        }

        if (message != null && !message.isBlank() && link != null && !link.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous ne pouvez pas envoyer un message et un lien en même temps.");
        }

        // Assurez-vous que le topic existe avant d'envoyer le message
        String topic = theme + "-" + category;
        try {
            createTopicIfNotExists(serverAddress, topic);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la création du topic : " + e.getMessage());
        }

        // Configuration du Kafka Producer
        Properties props = new Properties();
        props.put("bootstrap.servers", serverAddress); // Adresse du serveur Kafka
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            // Préparation du message à envoyer
            String content = message != null ? message : "Lien : " + link;

            // Envoi du message à Kafka
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, content);
            Future<RecordMetadata> future = producer.send(record);

            // Attendre la confirmation de l'envoi
            RecordMetadata metadata = future.get();
            return ResponseEntity.ok("Message envoyé avec succès au topic \"" + topic + "\". Partition: " + metadata.partition() + ", Offset: " + metadata.offset());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de l'envoi du message.");
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

}
