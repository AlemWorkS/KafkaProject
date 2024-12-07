package ccsr.project.kafka;
import ccsr.project.kafka.Controllers.KafkaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class KafkaApplication {
	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);

	}
	@GetMapping("/hello")
	public String hello(Model model) {
		model.addAttribute("message","Bienvenue");
		return "home";
	}
	@GetMapping("/home")
	public String home(HttpSession session, Model model) {
		// Vérifie si l'utilisateur est connecté
		String userEmail = (String) session.getAttribute("userEmail");

		if (userEmail == null) {
			// Si l'utilisateur n'est pas connecté, le rediriger vers la page de connexion
			return "redirect:/connexion";
		}

		// Ajoutez des données dynamiques pour la vue
		model.addAttribute("message", "Bienvenue !");
		return "home"; // Renvoie à la page d'accueil
	}



	// Endpoint pour afficher la page Publisher
	@GetMapping("/publisher")
	public String publisher(Model model) {
		model.addAttribute("message", "Bienvenue");
		return "Publisher";
	}

	@GetMapping("/search-topics")
	public ResponseEntity<List<String>> searchTopics(@RequestParam String interest) {
		try {
			// Appel correct de la méthode du service Kafka
			List<String> topics = kafkaService.searchTopicsByInterest(interest);
			return ResponseEntity.ok(topics);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.emptyList());
		}
	}
	@GetMapping("/get-messages")
	public ResponseEntity<List<String>> getMessages(@RequestParam String topicName) {
		try {
			List<String> messages = kafkaService.getMessagesFromTopic(topicName);
			return ResponseEntity.ok(messages);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Collections.singletonList("Erreur lors de la récupération des messages."));
		}
	}

}