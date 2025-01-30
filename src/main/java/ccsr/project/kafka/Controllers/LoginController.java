package ccsr.project.kafka.Controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {

    private final LoginService userService;

    @Autowired
    public LoginController(LoginService userService) {

        this.userService = userService;
    }

    @PostMapping("/inscription")
    public ResponseEntity<String> inscrireUtilisateur(@RequestParam String firstName, @RequestParam String lastName,
                                                      @RequestParam String userName, @RequestParam String email,
                                                      @RequestParam String password, @RequestParam String role) {
        try {
            if (userService.registerUser(email, userName, firstName, lastName, password, role)) {
                return ResponseEntity.ok("Inscription réussie !");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("L'utilisateur existe déjà !");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de l'inscription !");
        }
    }

    @PostMapping("/process-login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        String role = userService.authenticateUser(email, password);
        if (role != null) {
            session.setAttribute("userEmail", email);
            session.setAttribute("userRole", role);

            return switch (role) {
                case "Consumer" -> ResponseEntity.ok("/home");
                case "Producer" -> ResponseEntity.ok("/producer");
                default -> ResponseEntity.status(HttpStatus.FORBIDDEN).body("Rôle inconnu.");
            };
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Email ou mot de passe invalide !");
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logoutUser(HttpSession session) {
        session.invalidate();
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
