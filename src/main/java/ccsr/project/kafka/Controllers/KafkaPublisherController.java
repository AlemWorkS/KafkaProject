package ccsr.project.kafka.Controllers;
import ccsr.project.kafka.Controllers.KafkaService;

import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.Models.Publisher;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class KafkaPublisherController {

    Publisher publisher = new Publisher();

    @PostMapping("/connect-publisher")
    public ResponseEntity<String> connectPublisher(@RequestParam String serverAddress) {
        try {
            Publisher.connexion(); // Essayer d'obtenir des informations du cluster
            return ResponseEntity.ok("Connecté au serveur Kafka avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de se connecter au serveur Kafka");
        }
    }
    @PostMapping("/publish-message")
    public ResponseEntity<String> publishMessage(@RequestParam String topicName, @RequestParam String message) {
        try {
            KafkaService.publishMessage(topicName, message);

            // Récupérez les utilisateurs abonnés au topic
            List<String> subscribers = SubscriptionService.getSubscribersEmailsForTopic(topicName);

            // Envoyez un email à chaque abonné
            for (String subscriberEmail : subscribers) {
                String emailContent = "Un nouveau message a été publié dans le topic '" + topicName + "':\n\n" + message;
                EmailUtil.sendEmail(subscriberEmail, "Nouvelle alerte - " + topicName, emailContent);
            }

            return ResponseEntity.ok("Message publié et notifications envoyées avec succès.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de la publication du message : " + e.getMessage());
        }
    }


    @PostMapping("producer/send-message")
    public ResponseEntity<String> sendMessage(@RequestParam String theme,@RequestParam String message,@RequestParam String link,@RequestParam boolean isLink) {
        try {
            String topic = isLink ? "LienWeb" : "Articles";
            String valeur = isLink ? link : message;

            Message.creerMessage(topic,theme,valeur,"fred");

            return ResponseEntity.ok("L'article est enregistré sur les serveurs");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de se connecter au serveur Kafka");
        }
    }
}

