package ccsr.project.kafka;
import ccsr.project.kafka.Controllers.ConsumerController;
import ccsr.project.kafka.config.Config;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.yaml.snakeyaml.Yaml;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;


@SpringBootApplication
@Controller
public class KafkaApplication {

	public static void main(String[] args) {
		try {
			// Récupérer le chemin du fichier depuis la variable d'environnement
			String configPath = System.getenv("CONFIG_PATH")+"/config.yaml";

			if (configPath == null) {
				throw new IllegalArgumentException("La variable d'environnement CONFIG_PATH n'est pas définie !");
			}

			// Charger le fichier YAML
			Yaml yaml = new Yaml();
			try (InputStream inputStream = new FileInputStream(configPath)) {
				Map<String, Object> config = yaml.load(inputStream);
				config.forEach((conf,value)->{
					System.out.println(value);

				});
				Config.setServers(config);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		SpringApplication.run(KafkaApplication.class, args);

	}
	// Endpoint pour afficher la page Publisher
	@PostConstruct
	public void startKafkaListener() {
		ConsumerController.KafkaListener.listenToTopic("sport"); // Remplacez par votre topic ou ajoutez plusieurs topics si nécessaire
		ConsumerController.KafkaListener.listenToPlanning();
	}


}