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
                @RequestParam String userName,
                @RequestParam String role) { // Ajout de userName
            try {
                boolean isRegistered = userService.registerUser(firstName, lastName, email, password, userName,role);
                if (isRegistered) {
                    return ResponseEntity.ok("Inscription réussie !");
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'utilisateur existe déjà !");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'inscription !");
            }
        }




    /**
     * Endpoint pour la connexion
     */
    @PostMapping("/process-login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        System.out.println("Tentative de connexion : " + email);

        String role = userService.authenticateUser(email, password);
        if (role != null) {
            System.out.println("Utilisateur authentifié avec le rôle : " + role);
            session.setAttribute("userEmail", email);
            session.setAttribute("userRole", role);

            if ("Consumer".equals(role)) {
                return ResponseEntity.ok("/home");
            } else if ("Producer".equals(role)) {
                return ResponseEntity.ok("/producer");
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Rôle inconnu.");
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe invalide !");
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
