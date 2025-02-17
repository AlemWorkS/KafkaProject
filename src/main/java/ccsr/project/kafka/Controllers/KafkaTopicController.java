package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.Models.Publisher;
import ccsr.project.kafka.config.Config;
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

import java.awt.event.TextEvent;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
public class KafkaTopicController {


    /**
     * @return
     */
    //@GetMapping("/search-topics")
    public ResponseEntity<HashMap<Integer, HashMap<String, String>>> searchTopics(@RequestParam String interest, @RequestParam boolean fromBeginning, HttpSession session) {
        HashMap<Integer, HashMap<String, String>> topics;
        KafkaConsumer kafkaConsumer = null;
        try {
            boolean condition = session != null && !((String) session.getAttribute("userConsumerEmail")).isEmpty();
            if(condition) {
                System.out.println((String) session.getAttribute("userConsumerEmail"));
                Properties props = new Properties();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.KAFKA_SERVERS); // Remplacez par votre serveur Kafka
                if (fromBeginning) {
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "thread-" + session.getAttribute("userConsumerEmail") + 1 + UUID.randomUUID());
                    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "thread-" + session.getAttribute("userConsumerEmail") + 1 + UUID.randomUUID());

                } else {
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "thread-" + session.getAttribute("userConsumerEmail") + 1);
                    props.put(ConsumerConfig.CLIENT_ID_CONFIG, "thread-" + session.getAttribute("userConsumerEmail") + 1);

                }
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                kafkaConsumer = new KafkaConsumer(props);
                System.out.println("consummer ok");
            }
            if(condition){
                // Appel correct de la méthode du service Kafka
                topics = Message.getMessagesFromTopic(interest,fromBeginning,kafkaConsumer,session.getAttribute("userConsumerEmail").toString());
            }else{
                topics = Message.getMessagesFromTopic(interest,fromBeginning,null,"noMail");
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