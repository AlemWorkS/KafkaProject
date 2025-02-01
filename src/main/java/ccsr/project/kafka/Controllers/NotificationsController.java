
package ccsr.project.kafka.Controllers;


import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.*;





@RestController
public class  NotificationsController {

    @PostMapping("/make-planning")
    public ResponseEntity<String> makePlanning(
            @RequestParam(required = false) Integer planning_heure,
            @RequestParam(required = false) Integer planning_interval,
            @RequestParam(defaultValue = "0") String always,
            @RequestParam(required = false) String topicName,
            HttpSession session
    ) {
        String userEmail = (String) session.getAttribute("userConsumerEmail");

        if (userEmail == null || topicName == null || topicName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Erreur : Email utilisateur ou nom du topic non fourni.");
        }

        System.out.println("a");
        System.out.println("Always: " + always);
        System.out.println("Topic: " + topicName);

        String query = "SELECT * FROM mailplanning WHERE user_mail = ? AND topic = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setString(1, userEmail);
            statement.setString(2, topicName);
            ResultSet resultSet = statement.executeQuery();
            System.out.println("b");

            if (resultSet.next()) {
                query = "UPDATE mailplanning SET heure_env = ?, interval_de_jour = ? WHERE user_mail = ? AND topic = ?";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement stat = connection.prepareStatement(query)) {

                    if (!always.equals("on")) {
                        stat.setObject(1, planning_heure, Types.INTEGER);
                        stat.setObject(2, planning_interval, Types.INTEGER);
                    } else {
                        stat.setNull(1, Types.INTEGER);
                        stat.setNull(2, Types.INTEGER);
                    }

                    stat.setString(3, userEmail);
                    stat.setString(4, topicName);
                    int rowsAffected = stat.executeUpdate();
                    System.out.println("c");
                }
                if ("on".equals(always)) {
                    return ResponseEntity.ok("Vous allez recevoir des notifications à chaque nouveau message sur le topic : " + topicName);
                } else {
                    return ResponseEntity.ok("Vous recevrez vos notifications pour le topic '" + topicName + "' chaque " + planning_interval + " jours à " + planning_heure);
                }
            } else {
                return ResponseEntity.ok("Vous devez souscrire à ce topic "+topicName+" avant");
            }


        } catch (SQLException e) {
            System.out.println("e");
            System.out.println(e.getMessage());
            return ResponseEntity.badRequest().body("Erreur lors de la création : " + e.getMessage());
        }
    }


}
