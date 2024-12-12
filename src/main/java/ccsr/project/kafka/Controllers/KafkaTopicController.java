package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Consumer;
import ccsr.project.kafka.Models.Message;
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


    // Méthode pour nettoyer le nom du topic
    private String sanitizeTopicName(String topicName) {
        return topicName
                .replaceAll("[^a-zA-Z0-9._-]", "") // Supprime les caractères non valides
                .replaceAll("\\s+", "-")           // Remplace les espaces par des tirets
                .toLowerCase();                    // Convertir en minuscules pour la cohérence
    }

    // Validation avancée du nom du topic
    private boolean isValidKafkaTopicName(String topicName) {
        return topicName.matches("[a-zA-Z0-9._-]{1,249}");
    }



    // Endpoint pour créer un nouveau topic sur le serveur Kafka fourni
    @PostMapping("/create-topic")
    public ResponseEntity<String> createTopic(@RequestParam String topicName, @RequestParam String serverAddress) {
        // Nettoyer le nom du topic
        String sanitizedTopicName = sanitizeTopicName(topicName);

        // Validation du nom du topic
        if (!isValidKafkaTopicName(sanitizedTopicName)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Le nom du topic est invalide : " + sanitizedTopicName);
        }

        try {
            Publisher.creerTopic(sanitizedTopicName);
            return ResponseEntity.ok("Topic \"" + sanitizedTopicName + "\" créé avec succès");
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de la création du topic.");
        }
    }

    /**
     * @param interest
     * @return
     */
    @GetMapping("/search-topics")
    public ResponseEntity<Map<String, HashMap<String, String>>> searchTopics(@RequestParam String interest) {
        try {
            // Appel correct de la méthode du service Kafka
            Map<String,HashMap<String,String>> topics = Message.searchMessagesInAllTopics(interest);
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((Map<String, HashMap<String,String>>) Collections.emptyList());
        }
    }



}