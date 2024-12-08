package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.Models.Publisher;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.*;

@RestController
public class KafkaMessageController {


    @GetMapping("/get-messages")
    public ResponseEntity<List<String>> getMessages(@RequestParam String topicName) {
        try {
            List<String> messages = Message.getMessagesFromTopic(topicName);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList("Erreur lors de la récupération des messages."));
        }
    }

    /*@PostMapping("/post-messages")
    public ResponseEntity<List<String>> postMessages(@RequestParam String user,@RequestParam String topic,@RequestParam String message,@RequestParam String article){
        try {
            Message.creerMessage(topic,article,message,user);
            return ResponseEntity.status(HttpStatus.CREATED).body(Collections.singletonList("Message créé"));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.singletonList("Erreur lors de l'enregistrement du message."));
        }
    }*/



}
