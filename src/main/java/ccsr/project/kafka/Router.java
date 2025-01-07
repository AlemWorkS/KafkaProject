package ccsr.project.kafka;

import ccsr.project.kafka.Models.Message;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Controller
public class Router {

    @GetMapping("/home")
    public String hello(HttpSession session, Model model) {

        model.addAttribute("message", "Bienvenue");

        String userEmail = (String) session.getAttribute("userEmail");
        System.out.println("Session userEmail home : " + userEmail); // Ajoutez ceci pour déboguer

        if (userEmail == null) {
            System.out.println("Utilisateur non connecté, redirection vers /");
            return "redirect:/";
        }

        System.out.println("Utilisateur connecté, affichage de la page home");
        return "home"; // Retourne la vue "home.html"
    }


    // Endpoint pour afficher la page Publisher
    @GetMapping("/publisher")
    public String publisher(Model model) {
        return "Publisher";
    }

    @GetMapping("/")
    public String redirectToLogin() {
        return "login"; // Redirige vers la page de connexion
    }


    // Endpoint pour afficher la page Publisher
    @GetMapping("/producer")
    public String producer(Model model) {
        return "producer";
    }

    // Api des messages
    @GetMapping("/messages")
    public Map<Integer, HashMap<String, String>> messages(Model model, @RequestParam String i) {
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

    @GetMapping("/full-message")
    public String fullMessage() {
        // Retourne la vue "full-message.html"
        return "full-message";
    }


}
