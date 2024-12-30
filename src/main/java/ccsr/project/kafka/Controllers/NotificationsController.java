package ccsr.project.kafka.Controllers;


import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;



@RestController
public class NotificationsController {

    @PostMapping("/make-planning")
    public ResponseEntity<String> makePlanning(@RequestParam String planning_heure, @RequestParam String planning_interval, HttpSession session) {
        String userEmail = (String) session.getAttribute("userEmail");

        //Enregistrement du planning de notifications
        //Vérifier si le userEmail existe déjà dans la table mailplanning
        //si le mail existe alors on update la colone heure_env et interval_env
        //sinon on crée un nouvel enregistrement
        //Retourner un message de confirmation si la création ou mise à jour du planning est réussie
        //sinon retourner un message d'échec

        System.out.println("a");

        String query = "select * from mailplanning where user_mail = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {


            statement.setString(1, userEmail);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("b");

            if (resultSet.next()) {
                query = "UPDATE mailplanning SET heure_env = ?, interval_de_jour = ? WHERE user_mail = ?";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement stat = conn.prepareStatement(query)) {
                    stat.setInt(1, Integer.parseInt(planning_heure));
                    stat.setInt(2, Integer.parseInt(planning_interval));
                    stat.setString(3, userEmail);

                    int rowsAffected = stat.executeUpdate();
                    System.out.println("c");
                }
            }/*else{
                query = "CREATE mailplannin";
                statement = conn.prepareStatement(query);

                statement.setInt(1,Integer.parseInt(planning_heure));
                statement.setInt(2, Integer.parseInt(planning_interval));
                statement.setString(3, userEmail);

                int rowsAffected = statement.executeUpdate();
                return ResponseEntity.ok("Vous recevrez vos notifications chaque"+planning_interval+ " à "+planning_heure);

            }*/
            return ResponseEntity.ok("Vous recevrez vos notifications chaque" + planning_interval + " à " + planning_heure);

        } catch (SQLException e) {
            System.out.println("e");
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la création"+e.getMessage());
        }
    }

}
