package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Controllers.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @PostMapping("/subscriptions/subscribe")
    public ResponseEntity<String> subscribe(@RequestParam String topicName, @RequestParam String userEmail) {
        try {
            subscriptionService.addSubscription(userEmail, topicName);
            return ResponseEntity.ok("Abonnement enregistré avec succès !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'abonnement : " + e.getMessage());
        }
    }
}
