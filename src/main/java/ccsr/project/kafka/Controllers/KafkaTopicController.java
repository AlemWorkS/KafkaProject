package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.Models.Publisher;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class KafkaTopicController {

    /**
     * @return
     */
    @GetMapping("/search-topics")
    public ResponseEntity<HashMap<Integer, HashMap<String, String>>> searchTopics(@RequestParam String interest, @RequestParam boolean fromBeginning, HttpSession session) {
        HashMap<Integer, HashMap<String, String>> topics;
        try {
            if(session != null && !((String) session.getAttribute("userEmail")).isEmpty()) {
                System.out.println((String) session.getAttribute("userEmail"));
                Properties props = new Properties();
                props.put("bootstrap.servers", "localhost:29092,localhost:39092,localhost:49092"); // Remplacez par votre serveur Kafka
                props.put("group.id", "thread-"+session.getAttribute("userEmail")+1);
                props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                props.put("auto.offset.reset", "earliest");
                props.put("enable.auto.commit", "false"); // Gestion manuelle des offsets
                KafkaConsumer kafkaConsumer = new KafkaConsumer(props);
                System.out.println("consummer ok");


                // Appel correct de la méthode du service Kafka
                topics = Message.getMessagesFromTopic(interest,fromBeginning,kafkaConsumer);
            }else{
                topics = Message.getMessagesFromTopic(interest,fromBeginning,null);
            }

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


