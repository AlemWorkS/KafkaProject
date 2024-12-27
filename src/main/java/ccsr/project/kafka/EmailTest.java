package ccsr.project.kafka; // Assurez-vous que ce package correspond à votre projet.

public class EmailTest {
    public static void main(String[] args) {
        try {
            // Changez l'adresse email, le sujet et le corps pour tester
            ccsr.project.kafka.Controllers.EmailUtil.sendEmail("eoyotode@gmail.com", "Test Kafka Email", "Ceci est un test d'envoi d'email via Kafka.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
