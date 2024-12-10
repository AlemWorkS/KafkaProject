package ccsr.project.kafka;

import ccsr.project.kafka.Models.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class Router {

    @GetMapping("/home")
    public String hello(HttpSession session, Model model) {
        model.addAttribute("message", "Bienvenue");

        // Vérifie si l'utilisateur est connecté
        String userEmail = (String) session.getAttribute("userEmail");

        if (userEmail == null) {
            // Si l'utilisateur n'est pas connecté, le rediriger vers la page de connexion
            return "redirect:/";
        }

        return "home";
    }

    // Endpoint pour afficher la page Publisher
    @GetMapping("/publisher")
    public String publisher(Model model) {
        return "Publisher";
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "connexion"; // Redirige vers la page de connexion
    }


    // Endpoint pour afficher la page Publisher
    @GetMapping("/publisher-publish")
    public String publisherPublish(Model model) {
        return "PublisherPublish";
    }
    @GetMapping("/producer")
    public String producer(Model model) {
        return "producer";
    }

    // Api des messages
    @GetMapping("/messages")
    public Map<String, List<String>> messages(Model model, @RequestParam String i) {
        try {
            return Message.searchMessagesInAllTopics(i);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/connexion")
    public String connexion(Model model) {
        return "connexion";
    }

}
