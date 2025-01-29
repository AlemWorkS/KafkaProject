package ccsr.project.kafka;
import ccsr.project.kafka.Controllers.ConsumerController;
import ccsr.project.kafka.config.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;



@SpringBootApplication
@Controller
public class KafkaApplication {

	public static void main(String[] args) {

		Config.loadConfigFile();

		SpringApplication.run(KafkaApplication.class, args);

	}
	// Endpoint pour afficher la page Publisher
	@PostConstruct
	public void startKafkaListener() {
		ConsumerController.KafkaListener.listenToTopic("sport"); // Remplacez par votre topic ou ajoutez plusieurs topics si nécessaire
		ConsumerController.KafkaListener.listenToPlanning();
	}


}