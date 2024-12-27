package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Controllers.EmailUtil;
import ccsr.project.kafka.Models.Message;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import jakarta.servlet.http.HttpSession;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class KafkaMessageController {


    @GetMapping("/get-messages")
    public ResponseEntity<HashMap<Integer, HashMap<String, String>>> getMessages(@RequestParam String topicName, @RequestParam boolean fromBeginning, HttpSession session) {
        HashMap<Integer, HashMap<String, String>> messages;

        // Nettoyer le nom du topic avant de le passer au modèle
        String sanitizedTopicName = Message.sanitizeTopicName(topicName);

        try {
                if(session != null && !((String) session.getAttribute("userEmail")).isEmpty()) {
                    System.out.println((String)session.getAttribute("userEmail"));
                    Properties props = new Properties();
                    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:39092,localhost:49092");
                    props.put(ConsumerConfig.GROUP_ID_CONFIG, "web-consumer-group-" + session.getAttribute("userMail"));
                    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
                    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
                    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // Gestion manuelle des offsets
                    KafkaConsumer kafkaConsumer = new KafkaConsumer(props);

                    // Appel correct de la méthode du service Kafka
                    messages = Message.getMessagesFromTopic(sanitizedTopicName,fromBeginning,kafkaConsumer);
                }else{
                    messages = Message.getMessagesFromTopic(sanitizedTopicName,fromBeginning,null);
                }
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body((HashMap<Integer, HashMap<String, String>>) Collections.singletonList("Erreur lors de la récupération des messages."));
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
    /*@PostMapping("/publish")
    public ResponseEntity<String> publishMessage(@RequestParam String topicName, @RequestParam String message) {
        try {

            KafkaService.publishToTopic(topicName, message);

            // Récupérer les emails des abonnés au topic
            List<String> subscribers = SubscriptionService.getSubscribersEmailsForTopic(topicName);

            // Envoyer un email à chaque abonné
            for (String email : subscribers) {
                EmailUtil.sendEmail(email, topicName, message);
            }

            return ResponseEntity.ok("Message publié et notifications envoyées !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la publication du message : " + e.getMessage());
        }

    }*/



}
