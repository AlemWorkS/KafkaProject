package ccsr.project.kafka.Controllers;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;


import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailUtil {

    private static final String SMTP_HOST = "smtp.gmail.com"; // Serveur SMTP
    private static final String SMTP_PORT = "587"; // Port SMTP
    private static final String USERNAME = "eoyotode@gmail.com"; // Remplacez par votre email
    private static final String PASSWORD = "jojo225@"; // Mot de passe de votre email

    /**
     * Envoie un email à un utilisateur
     *
     * @param recipientEmail Email du destinataire
     * @param topicName Nom du topic
     * @param messageContent Contenu du message
     */
    public static void sendEmail(String recipientEmail, String topicName, String messageContent) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", SMTP_HOST);
        properties.put("mail.smtp.port", SMTP_PORT);

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(USERNAME, PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Nouvelle alerte pour le topic: " + topicName);
            message.setText("Bonjour,\n\nVous avez une nouvelle alerte dans le topic '" + topicName + "'.\n\n"
                    + "Contenu du message:\n" + messageContent + "\n\n"
                    + "Merci d'utiliser Omega Alerts.");

            Transport.send(message);
            System.out.println("Email envoyé avec succès à " + recipientEmail);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi de l'email à " + recipientEmail);
        }
    }
}
