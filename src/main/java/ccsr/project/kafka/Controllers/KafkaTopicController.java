package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.Models.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

@RestController
public class KafkaTopicController {

    /**
     * @return
     */
    @GetMapping("/search-topics")
    public ResponseEntity<HashMap<Integer, HashMap<String, String>>> searchTopics(@RequestParam String interest,@RequestParam boolean fromBeginning) {
        try {
            // Appel correct de la méthode du service Kafka
            HashMap<Integer, HashMap<String, String>> topics = Message.getMessagesFromTopic(interest,fromBeginning);
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((HashMap<Integer, HashMap<String, String>>) Collections.emptyList());
        }
    }

    @GetMapping("/list-topics")
    public ResponseEntity<Set<String>> listTopics() {
        try {
            // Appel correct de la méthode du service Kafka
            Set<String> topics = Publisher.listTopic();
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.EMPTY_SET);
        }
    }
}


