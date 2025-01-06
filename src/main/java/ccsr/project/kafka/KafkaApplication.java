package ccsr.project.kafka;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;


@SpringBootApplication
@Controller
public class KafkaApplication {

	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);

	}


	// Endpoint pour afficher la page Publisher
	/*@PostConstruct
	public void startKafkaListener() {
		KafkaService.KafkaListener.listenToTopic("sport"); // Remplacez par votre topic ou ajoutez plusieurs topics si nécessaire
		KafkaService.KafkaListener.listenToPlanning();
	}*/



}