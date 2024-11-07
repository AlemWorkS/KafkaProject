package ccsr.project.kafka.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping
public class AdresseController {

        @PostMapping("/receive-address")
        public ResponseEntity<String> receiveAddress(@ModelAttribute ServerAdress serverAdress, BindingResult result, Model model) {
            if (result.hasErrors()) {
                return ResponseEntity.ok("Erreur"); // Retourner à la vue du formulaire avec des erreurs
            }
            model.addAttribute("adresse", serverAdress);
            System.out.println("Adresse reçue : " + serverAdress.getAddress());
            return ResponseEntity.ok("Adresse reçue : " + serverAdress.getAddress());
        }

        // Classe interne pour mapper la requête
        public static class ServerAdress {
            private String address;

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }
        }
    }

