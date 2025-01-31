package ccsr.project.kafka.Models;

import ccsr.project.kafka.Controllers.DatabaseConnection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;


import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class Message {


    // Nettoyage et validation des noms de topics avant interaction
    public static String sanitizeTopicName(String topicName) {
        return topicName
                .replaceAll("[^a-zA-Z0-9._-]", "") // Supprime les caractères non valides
                .replaceAll("\\s+", "-")           // Remplace les espaces par des tirets
                .toLowerCase();
    }

    /**
     * @param topicName
     * @return une Map de touts les messages d'un topic
     * @return
     */
    public static HashMap<Integer, HashMap<String, String>> getMessagesFromTopic(String topicName, boolean fromBeginning, KafkaConsumer<String, String> consumer, String userEmail) {
        HashMap<Integer, HashMap<String, String>> recordMap = new HashMap<>();
        HashMap<String, String> messageNull = new HashMap<>();

        String sanitizedTopicName = sanitizeTopicName(topicName);

        try {
            System.out.println(Agents.getAdminClient().listTopics().names().get());
            // Vérification si le topic existe
            if (Agents.getAdminClient().listTopics().names().get().contains(sanitizedTopicName)) {
                // Configuration du consommateur
                KafkaConsumer<String, String> onLineConsumer = (consumer != null) ? consumer : Agents.getConsummer();

                // Assignation manuelle pour s'assurer que toutes les partitions sont couvertes
                List<TopicPartition> partitions = onLineConsumer.partitionsFor(sanitizedTopicName)
                        .stream()
                        .map(info -> new TopicPartition(sanitizedTopicName, info.partition()))
                        .collect(Collectors.toList());
                onLineConsumer.assign(partitions);

                // Définir l'offset au début si nécessaire
                if (fromBeginning) {
                    onLineConsumer.seekToBeginning(partitions);
                }

                // Récupération des messages
                ConsumerRecords<String, String> records = onLineConsumer.poll(Duration.ofMillis(500));
                while (!records.isEmpty()) {
                    for (ConsumerRecord<String, String> record : records) {
                        HashMap<String, String> message = new HashMap<>();
                        message.put("message", record.value());
                        message.put("theme", Optional.ofNullable(record.headers().lastHeader("theme"))
                                .map(header -> new String(header.value(), StandardCharsets.UTF_8))
                                .orElse("Thème inconnu"));
                        message.put("producer", Optional.ofNullable(record.key()).orElse("Producteur inconnu"));
                        message.put("topic", sanitizedTopicName);
                        recordMap.put(recordMap.size(), message);
                    }
                    records = onLineConsumer.poll(Duration.ofMillis(500));
                }

                //onLineConsumer.close();
            } else {
                // Topic inexistant
                messageNull.put("message", "");
                messageNull.put("producer", "Système");
                messageNull.put("theme", "Ce topic \"" + sanitizedTopicName + "\" n'existe pas.");
                recordMap.put(0, messageNull);
            }

            // Si aucun message n'est trouvé
            if (recordMap.isEmpty()) {
                messageNull.put("message", "Aucun message trouvé sur le topic \"" + sanitizedTopicName + "\".");
                messageNull.put("producer", "Système");
                messageNull.put("theme", "Topic vide");
                recordMap.put(0, messageNull);
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageNull.put("message", "Erreur lors de la récupération des messages : " + e.getMessage());
            messageNull.put("producer", "Système");
            recordMap.put(0, messageNull);
        }
        return recordMap;
    }

    private static void annulerEnv(String userEmail) {

        //requête pour planifier les envoies d'email
        String query = "SELECT mail_lu FROM mailplanning WHERE user_mail = ?";

        try (Connection connection = DatabaseConnection.getConnection()
        ) {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, userEmail);
            preparedStatement.executeQuery();

            ResultSet resultSet = preparedStatement.getResultSet();

            if (resultSet.next() && !resultSet.getBoolean("mail_lu")) {
                //requête pour planifier les envoies d'email
                query = "UPDATE mailplanning SET mail_lu = ? WHERE user_mail = ?";
                try {
                    preparedStatement = connection.prepareStatement(query);
                    preparedStatement.setBoolean(1, true);
                    preparedStatement.setString(2, userEmail);
                    //System.out.println(6);
                    preparedStatement.executeUpdate();

                    System.out.println("Email déplannifié : " + userEmail);
                } catch (Exception e) {
                    System.err.println("Erreur lors de l'update de l'envoi de l'email à " + userEmail + " : " + e.getMessage());
                }
            } else {
                System.out.println("Rien à annuler");
            }

        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du suscriber de l'email à " + userEmail + " : " + e.getMessage());
        }


    }



    public static Future<Void> creerMessage(String topic, String article, String message, String user) {

        // Créer le ProducerRecord avec les en-têtes

        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, user, message);
        if (article == null || article.isEmpty()) {
            article = "ArticleInconnu";
        }

        // Ajouter l'en-tête "theme"

        producerRecord.headers().add("theme", article.getBytes(StandardCharsets.UTF_8));

        return CompletableFuture.runAsync(() ->
            // Afficher un message pour vérifier que l'en-tête a bien été ajouté

            // Envoi du message avec un callback pour gérer les erreurs
            Agents.getProducer().send(producerRecord, (metadata, exception) -> {
                if (exception != null) {
                        // En cas d'erreur lors de l'envoi du message
                        System.err.println("Erreur lors de l'envoi du message : " + exception.getMessage());

                } else {
                    // Message envoyé avec succès
                    System.out.println("Message envoyé avec succès, Topic: " + topic);
                }
            })

        );
    }


}

