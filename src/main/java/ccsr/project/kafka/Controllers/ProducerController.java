package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Message;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.servlet.http.HttpSession;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.Future;

@RestController
@RequestMapping("/producer")
public class ProducerController {


    private AdminClient adminClient; // Client Kafka pour la gestion

    @PostMapping("/connect-publisher")
    public ResponseEntity<String> connectToKafka(@RequestParam String serverAddress) {
        try {
            Properties config = new Properties();
            config.put("bootstrap.servers", serverAddress);
            adminClient = AdminClient.create(config);

            // Vérification de la connexion au serveur Kafka
            if(!adminClient.describeCluster().nodes().get().isEmpty()) {
                return ResponseEntity.ok("Connecté au serveur Kafka");
            }
            else{
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur lors de la connexion au serveur Kafka");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la connexion au serveur Kafka : " + e.getMessage());
        }
    }


    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(
            @RequestParam String topicName,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String link,
            @RequestParam(required = false) String titre,
            HttpSession session
            ) {

        if(session.getAttribute("userEmail") == null){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Veuillez vous connecter avant");

        }

        // Validation : Message ou lien, mais pas les deux
        if ((message == null || message.isBlank()) && (link == null || link.isBlank())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous devez fournir soit un message, soit un lien.");
        }

        if (message != null && !message.isBlank() && link != null && !link.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vous ne pouvez pas envoyer un message et un lien en même temps.");
        }

        // Nettoyage du nom du topic
        String sanitizedTopicName = sanitizeTopicName(topicName);
        System.out.println(Dotenv.load().get("KAFKA_SERVERS") + 1);
        // Configuration du Kafka Producer
        Properties props = new Properties();
        props.put("bootstrap.servers", Dotenv.load().get("KAFKA_SERVERS"));
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.out.println(Dotenv.load().get("KAFKA_SERVERS") + 2);


        try (KafkaProducer<String, String> producer = new KafkaProducer<>(props)) {
            // Préparation du message à envoyer
            String content = message != null ? message : "Lien : " + link;

            // Vérifier si le topic existe
            if (!topicExists(sanitizedTopicName)) {
                createTopicIfNotExists(sanitizedTopicName);
            }
            System.out.println(Dotenv.load().get("KAFKA_SERVERS") + 3);

            ProducerRecord<String, String> record = new ProducerRecord<>(sanitizedTopicName, content);
            System.out.println(Dotenv.load().get("KAFKA_SERVERS") + 4);
            Message.creerMessage(sanitizedTopicName,titre,content,session.getAttribute("userEmail").toString());
            System.out.println(Dotenv.load().get("KAFKA_SERVERS") + 5);
            return ResponseEntity.ok("Message envoyé avec succès au topic \"" + sanitizedTopicName + "\".");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de l'envoi du message.");
        }

    }

    // Vérifie si un topic existe
    private boolean topicExists(String topicName) throws Exception {
        Properties config = new Properties();
        config.put("bootstrap.servers",Dotenv.load().get("KAFKA_SERVERS"));
        try (AdminClient adminClient = AdminClient.create(config)) {
            return adminClient.listTopics().names().get().contains(topicName);
        }
    }

    // Méthode pour créer un topic s'il n'existe pas
    private void createTopicIfNotExists(String topicName) throws Exception {
        Properties config = new Properties();
        config.put("bootstrap.servers", Dotenv.load().get("KAFKA_SERVERS"));
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
    @GetMapping("/list-topics")
    public ResponseEntity<List<String>> listTopics() {
        try {
            if (adminClient == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonList("Veuillez d'abord vous connecter au serveur Kafka."));
            }

            Set<String> topics = adminClient.listTopics().names().get();
            return ResponseEntity.ok(new ArrayList<>(topics));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Erreur lors de la récupération des topics : " + e.getMessage()));
        }
    }

}