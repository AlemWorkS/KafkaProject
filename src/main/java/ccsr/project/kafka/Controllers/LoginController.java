package ccsr.project.kafka.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    private final KafkaService kafkaService;

    @Autowired
    public LoginController(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }


    /**
     * Traite la requête de connexion
     * URL : /process-login
     */
    @PostMapping("/process-login")
    public String processLogin(
            @RequestParam(required = true) String email,
            @RequestParam(required = true) String username,
            HttpSession session,
            Model model
    ) {
        // Validation côté serveur
        if (!email.contains("@") || username.isEmpty()) {
            model.addAttribute("error", "Email ou nom d'utilisateur invalide.");
            return "redirect:/"; // Retourne à la page de connexion avec un message d'erreur
        }

        // Vérifie ou enregistre l'utilisateur
        try {
            boolean userExists = KafkaService.verifyOrRegisterUser(email, username);

            if (userExists) {
                // Enregistre les informations utilisateur dans la session
                session.setAttribute("userEmail", email);
                session.setAttribute("username", username);
                return "redirect:/home"; // Redirige vers la page d'accueil
            } else {
                model.addAttribute("error", "Erreur lors de la connexion.");
                return "redirect:/"; // Retourne à la page de connexion
            }
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur est survenue : " + e.getMessage());
            return "redirect:/"; // Retourne à la page de connexion avec une erreur
        }
    }
}
