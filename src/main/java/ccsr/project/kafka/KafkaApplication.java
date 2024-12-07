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


@SpringBootApplication
@Controller
public class KafkaApplication {
	private final KafkaService kafkaService;

	// Injection via le constructeur
	@Autowired
	public KafkaApplication(KafkaService kafkaService) {
		this.kafkaService = kafkaService;
	}

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);

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



}