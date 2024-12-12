package ccsr.project.kafka.Controllers;

import jakarta.validation.constraints.Email;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;



public class EmailUtil {

    public static void sendEmail(String recipient, String subject, String body) {
        try {
            // Configurer l'email
            Email email = new SimpleEmail();
            email.setHostName("smtp.gmail.com");
            email.setSmtpPort(587);
            email.setAuthenticator(new DefaultAuthenticator("votre.email@example.com", "votreMotDePasse"));
            email.setStartTLSEnabled(true);

            email.setFrom("votre.email@example.com", "Nom de l'expéditeur");
            email.setSubject(subject);
            email.setMsg(body);
            email.addTo(recipient);

            // Envoyer l'email
            email.send();
            System.out.println("Email envoyé avec succès à " + recipient);
        } catch (EmailException e) {
            System.err.println("Erreur lors de l'envoi de l'email : " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Exemple d'utilisation
        sendEmail("destinataire@example.com", "Sujet Test", "Ceci est un test d'envoi d'email.");
    }
}
