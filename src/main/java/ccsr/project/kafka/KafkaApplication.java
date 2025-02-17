package ccsr.project.kafka;
import ccsr.project.kafka.Controllers.ConsumerController;
import ccsr.project.kafka.config.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@SpringBootApplication
@Controller
public class KafkaApplication {
	private static final ExecutorService executorService = Executors.newFixedThreadPool(5);

	public static void main(String[] args) {


		executorService.submit(()->{
			Config.loadConfigFile();
			SpringApplication.run(KafkaApplication.class, args);});

	}
	// Endpoint pour afficher la page Publisher
	@PostConstruct
	public void startKafkaListener() {
		executorService.submit(()-> ConsumerController.KafkaListener.listenToTopic("sport")); // Remplacez par votre topic ou ajoutez plusieurs topics si nécessaire
		executorService.submit(ConsumerController.KafkaListener::listenToPlanning);
	}


}