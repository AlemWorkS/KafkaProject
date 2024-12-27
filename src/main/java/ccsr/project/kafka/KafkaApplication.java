package ccsr.project.kafka;
import ccsr.project.kafka.Controllers.KafkaService;
import jakarta.annotation.PostConstruct;
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

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);

	}


	// Endpoint pour afficher la page Publisher
	@PostConstruct
	public void startKafkaListener() {
		KafkaService.KafkaListener.listenToTopic("sport"); // Remplacez par votre topic ou ajoutez plusieurs topics si nécessaire
		System.out.println("L'écouteur Kafka pour le topic 'sport' a démarré.");
	}


}