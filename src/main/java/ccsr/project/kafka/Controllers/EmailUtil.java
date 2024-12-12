package ccsr.project.kafka;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;

public class EmailUtil {

    /**
     * Méthode pour envoyer un email.
     *
     * @param recipient Adresse email du destinataire.
     * @param subject Sujet de l'email.
     * @param body Contenu de l'email.
     * @throws EmailException Si une erreur survient lors de l'envoi.
     */
    public static void sendEmail(String recipient, String subject, String body) throws EmailException {
        // Configurer l'email
        Email email = new SimpleEmail();
        email.setHostName("smtp.gmail.com"); // Utiliser le serveur SMTP de Gmail
        email.setSmtpPort(587); // Port SMTP pour TLS
        email.setAuthenticator(new DefaultAuthenticator("eoyotode@gmail.com", "qzor ajzm uopl bxua")); // Remplacez par vos identifiants
        email.setStartTLSEnabled(true); // Activer TLS

        email.setFrom("eoyotode@gmail.com", "Notification Kafka");
        email.setSubject(subject);
        email.setMsg(body);
        email.addTo(recipient);

        // Envoyer l'email
        email.send();
    }
}
