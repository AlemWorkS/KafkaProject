package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Publisher;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class KafkaPublisherController {

    @PostMapping("/connect-publisher")
    public ResponseEntity<String> connectPublisher(@RequestParam String serverAddress) {
        try {
            Publisher.connexion(); // Essayer d'obtenir des informations du cluster
            return ResponseEntity.ok("Connecté au serveur Kafka avec succès");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Impossible de se connecter au serveur Kafka");
        }
    }
}

