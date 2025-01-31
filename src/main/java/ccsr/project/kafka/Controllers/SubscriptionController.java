package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Controllers.SubscriptionService;
import ccsr.project.kafka.Models.Publisher;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import ccsr.project.kafka.Models.Publisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class SubscriptionController {

    @PostMapping("/subscriptions/subscribe")
    public ResponseEntity<String> subscribe(@RequestParam String topicName, @RequestParam String userEmail) {
        try {
            ConsumerController.subscribeUserToTopic(userEmail, topicName);
            return ResponseEntity.ok("Abonnement enregistré avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'abonnement : " + e.getMessage());
        }
    }

    @PostMapping("/subscribe-to-topic")
    public ResponseEntity<String> subscribeToTopic(
            @RequestParam String email,
            @RequestParam String topicName) {
        try {
            boolean topicExists = SubscriptionService.doesTopicExist(topicName);
            if (!topicExists) {
                SubscriptionService.addTopic(topicName);
            }

            boolean isSubscribed = SubscriptionService.isUserSubscribed(email, topicName);
            if (isSubscribed) {
                return ResponseEntity.ok("Vous êtes déjà abonné à ce topic.");
            }

            SubscriptionService.subscribeUserToTopic(email, topicName);
            String query = "INSERT INTO mailplanning(topic,interval_de_jour,heure_env,user_mail,mail_lu) VALUES (?,?,?,?,?)";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement stat = connection.prepareStatement(query)) {


                stat.setString(1, topicName);
                stat.setNull(2, Types.INTEGER);
                stat.setNull(3,Types.INTEGER);
                stat.setString(4, email);
                stat.setBoolean(5, true);

                int rowsAffected = stat.executeUpdate();
                System.out.println("Planning rows affected");

            }
            return ResponseEntity.ok("Abonnement réussi au topic : " + topicName);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'abonnement : " + e.getMessage());
        }
    }

    @GetMapping("/subscriptions/user")
    public ResponseEntity<String> subscribe(@RequestParam String userEmail) {
        try {
            Set<String> topics = Publisher.listTopic();
            return ResponseEntity.ok("Abonnement enregistré avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'abonnement : " + e.getMessage());
        }
    }

    // Controller pour gérer les abonnements aux topics
    @GetMapping("/subscriptions/topics")
    public ResponseEntity<List<String>> getTopicsForUser(@RequestParam String userEmail) {
        System.out.println("Requête reçue pour récupérer les topics pour l'utilisateur : " + userEmail);
        try {
            // Appel du service pour récupérer les topics
            List<String> topics = SubscriptionService.getTopicsForUser(userEmail);

            // Retourne la liste des topics sous forme de réponse JSON
            return ResponseEntity.ok(topics);
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des topics : " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList()); // Retourne une liste vide en cas d'erreur
        }
    }

    @GetMapping("/current-user-email")
    public ResponseEntity<String> getCurrentUserEmail(HttpSession session) {
        String userEmail = (String) session.getAttribute("userConsumerEmail");
        if (userEmail != null) {
            return ResponseEntity.ok(userEmail);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Utilisateur non connecté");
        }
    }

}
