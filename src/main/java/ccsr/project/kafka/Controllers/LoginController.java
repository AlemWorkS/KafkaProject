package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Controllers.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
public class LoginController {

    private final UserService userService;

    @Autowired
    public LoginController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Endpoint pour l'inscription
     */

        @PostMapping("/inscription")
        public ResponseEntity<String> inscrireUtilisateur(
                @RequestParam String firstName,
                @RequestParam String lastName,
                @RequestParam String email,
                @RequestParam String password,
                @RequestParam String userName) { // Ajout de userName
            try {
                boolean isRegistered = userService.registerUser(firstName, lastName, email, password, userName);
                if (isRegistered) {
                   return ResponseEntity.ok("/login");

                } else {
                    return ResponseEntity.badRequest().body("Erreur lors de l'inscription.");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            }
        }




    /**
     * Endpoint pour la connexion
     */
    @PostMapping("/process-login")
    public String login(
            @RequestParam String email,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {
        boolean isAuthenticated = userService.authenticateUser(email, password);
        if (isAuthenticated) {
            session.setAttribute("userEmail", email);
            return "redirect:/home"; // Redirige vers la page d'accueil
        } else {
            model.addAttribute("error", "Email ou mot de passe incorrect.");
            return "redirect:/login"; // Retourne au formulaire de connexion
        }

    }

    /**
     * Endpoint pour la déconnexion
     */
    @GetMapping("/logout")
    public String logoutUser(HttpSession session) {
        session.invalidate(); // Invalide la session
        return "redirect:/login"; // Retourne à la page de connexion
    }

}
