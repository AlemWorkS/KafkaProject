package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Agents;
import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.config.Config;
import jakarta.servlet.http.HttpSession;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/producer")
public class ProducerController {


    @PostMapping("/send-message")
    public ResponseEntity<String> sendMessage(
            @RequestParam String topicName,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String titre,
            HttpSession session
    ) {

        if (session.getAttribute("userProducerEmail") == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Veuillez vous connecter avant");
        }


        // Nettoyage du nom du topic
        String sanitizedTopicName = sanitizeTopicName(topicName);

        // ** Verrouillage pour éviter la concurrence**
        synchronized (this) {
            try {
                // Vérifier si le topic existe avant d'envoyer le message
                if (!topicExists(sanitizedTopicName)) {
                    createTopicIfNotExists(sanitizedTopicName);
                }

                // Configuration optimisée du Kafka Producer pour éviter la perte de messages
                Properties props = new Properties();
                props.put("bootstrap.servers", Config.KAFKA_SERVERS);
                props.put("acks", "all");  // Garantit qu'un message est bien stocké avant confirmation
                props.put("retries", 5);   // Réessaye en cas d’échec
                props.put("batch.size", 16384);  // Envoie en batch pour optimiser la charge
                props.put("linger.ms", 5);  // Attente minimale avant d’envoyer
                props.put("buffer.memory", 33554432);  // Ajuste la mémoire tampon
                props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
                props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

                KafkaProducer<String, String> producer = new KafkaProducer<>(props);

                // Envoi du message
                String content = message;

                // Enregistrer le message dans la base de données
                Message.creerMessage(sanitizedTopicName, titre, content, session.getAttribute("userProducerEmail").toString());



                return ResponseEntity.ok("Message envoyé avec succès au topic \"" + sanitizedTopicName + "\".");
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Échec de l'envoi du message.");
            }
        }
    }


    @PostMapping("/connect-publisher")
    public ResponseEntity<String> connectToKafka() {
        try {

            // Vérification de la connexion au serveur Kafka
            boolean isConnected = !Agents.getAdminClient()
                    .describeCluster()
                    .nodes()
                    .get()
                    .isEmpty();

            if (isConnected) {
                return ResponseEntity.ok("Connecté au serveur Kafka");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Erreur : Aucun nœud Kafka trouvé");
            }
        } catch (ExecutionException | InterruptedException e) {
            // Log l'exception avec un message clair pour faciliter le débogage
            Thread.currentThread().interrupt(); // Rétablir l'état d'interruption si l'interruption est levée
            String errorMessage = "Erreur lors de la connexion au serveur Kafka : " + e.getCause().getMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        } catch (Exception e) {
            // Gestion d'autres exceptions inattendues
            String errorMessage = "Erreur inattendue : " + e.getMessage();
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
    }

   //verifie si un topic existe
    private boolean topicExists(String topicName) throws Exception {
        return Agents.getAdminClient().listTopics().names().get().contains(topicName);
    }

    //Méthode pour créer un topiv s'il n'existe pas
    private void createTopicIfNotExists(String topicName) throws Exception {

        if (!Agents.getAdminClient().listTopics().names().get().contains(topicName)) {
            int partitions = 3;
            short replicationFactor = 1;

            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
            Agents.getAdminClient().createTopics(Collections.singletonList(newTopic));

            // Attendre un peu pour que le topic soit bien disponible
            Thread.sleep(3000);
        }

    }





    // Nettoyage du nom du topic
    private String sanitizeTopicName(String topicName) {
        return topicName.replaceAll("[^a-zA-Z0-9._-]", "").toLowerCase();
    }
    @GetMapping("/list-topics")
    public ResponseEntity<List<String>> listTopics() {
        try {


            Set<String> topics = Agents.getAdminClient().listTopics().names().get();
            return ResponseEntity.ok(new ArrayList<>(topics));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonList("Erreur lors de la récupération des topics : " + e.getMessage()));
        }
    }

}