package ccsr.project.kafka;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@SpringBootApplication
@Controller
public class KafkaApplication {
	public static void main(String[] args) {
		SpringApplication.run(KafkaApplication.class, args);


	}
	@GetMapping("/hello")
	public String hello(Model model) {
		model.addAttribute("message","Bienvenue");
		System.out.println("Hello World");
		return "index";
	}
}