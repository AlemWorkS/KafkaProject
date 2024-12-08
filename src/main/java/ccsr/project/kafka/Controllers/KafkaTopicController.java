package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Consumer;
import ccsr.project.kafka.Models.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
public class KafkaTopicController {

    // Endpoint pour lister tous les topics sur le serveur Kafka fourni
    @GetMapping("/list-topics")
    public ResponseEntity<List<String>> listTopics(@RequestParam String serverAddress) {

        try {
            Set<String> topics = Publisher.listTopic();
            return ResponseEntity.ok(new ArrayList<>(topics));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Endpoint pour créer un nouveau topic sur le serveur Kafka fourni
    @PostMapping("/create-topic")
    public ResponseEntity<String> createTopic(@RequestParam String topicName, @RequestParam String serverAddress) {

        try {
            Publisher.creerTopic(topicName);
            return ResponseEntity.ok("Topic créé avec succès");
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * @param interest
     * @return
     */
    @GetMapping("/search-topics")
    public ResponseEntity<List<String>> searchTopics(@RequestParam String interest) {
        try {
            // Appel correct de la méthode du service Kafka
            List<String> topics = Consumer.searchTopicsByInterest(interest);
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

}