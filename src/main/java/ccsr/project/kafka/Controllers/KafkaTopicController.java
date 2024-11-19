package ccsr.project.kafka.Controllers;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class KafkaTopicController {

    // Endpoint pour lister tous les topics sur le serveur Kafka fourni
    @GetMapping("/list-topics")
    public ResponseEntity<List<String>> listTopics(@RequestParam String serverAddress) {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);

        try (AdminClient adminClient = AdminClient.create(config)) {
            Set<String> topics = adminClient.listTopics().names().get();
            return ResponseEntity.ok(new ArrayList<>(topics));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Endpoint pour créer un nouveau topic sur le serveur Kafka fourni
    @PostMapping("/create-topic")
    public ResponseEntity<String> createTopic(@RequestParam String topicName, @RequestParam String serverAddress) {

        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, serverAddress);

        try (AdminClient adminClient = AdminClient.create(config)) {

            NewTopic newTopic = new NewTopic(topicName, 1, (short) 3);
            adminClient.createTopics(Collections.singleton(newTopic)).all().get();
            return ResponseEntity.ok("Topic créé avec succès");

        } catch (Exception e) {

            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Échec de la création du topic");

        }

    }


}