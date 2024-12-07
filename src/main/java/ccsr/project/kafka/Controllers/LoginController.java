package ccsr.project.kafka.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;


@Controller
public class LoginController {
    @GetMapping("/connexion")
    public String processLogin(@RequestParam String email, @RequestParam String username, HttpSession session) {
        if (email.isEmpty() || !email.contains("@") || username.isEmpty()) {
            session.setAttribute("error", "Email ou nom d'utilisateur invalide.");
            return "connexion";
        }

        boolean userExists = KafkaService.verifyOrRegisterUser(email, username);
        if (userExists) {
            session.setAttribute("userEmail", email);
            session.setAttribute("username", username);
            return "redirect:/home";
        } else {
            session.setAttribute("error", "Erreur lors de la connexion.");
            return "connexion";
        }
    }

}
