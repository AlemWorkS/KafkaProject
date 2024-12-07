package ccsr.project.kafka;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class Router {
    
    @GetMapping("/hello")
    public String hello(Model model) {
        model.addAttribute("message","Bienvenue");
        return "home";
    }

    // Endpoint pour afficher la page Publisher
    @GetMapping("/publisher")
    public String publisher(Model model) {
        return "Publisher";
    }
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/connexion"; // Redirige vers la page de connexion
    }



}
