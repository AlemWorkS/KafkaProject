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
import java.util.ArrayList;
import java.util.Collections;
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



    public static Map<Integer, HashMap<String, String>> searchMessagesInAllTopics(String keywords) throws ExecutionException, InterruptedException {

        // 1. Séparer les mots-clés et les stocker dans une liste
        List<String> keywordList = Arrays.asList(keywords.split("\\s+"));

        // 2. Initialiser la map des résultats
        Map<Integer, HashMap<String, String>> result = new ConcurrentHashMap<>();
        /*
        // 3. Obtenir la liste des topics depuis Kafka
        Set<String> allTopics = Publisher.listTopic();

        // 4. Filtrer les topics en fonction des mots-clés
        List<String> existingTopics = findExistingTopics(allTopics, keywordList);

        // 5. Créer un ExecutorService pour gérer les threads
        ExecutorService executorService = Executors.newFixedThreadPool(10);  // Vous pouvez ajuster la taille du pool

        // 6. Soumettre des tâches pour rechercher des messages dans les topics existants
        List<Future<Void>> futures = new ArrayList<>();
        for (String topicName : existingTopics) {
            Future<Void> future = executorService.submit(() -> {
                try {
                    searchMessagesInTopic(topicName, result);
                } catch (Exception e) {
                    System.out.println("Erreur lors de la recherche de messages dans le topic " + topicName + ": " + e.getMessage());
                }
                return null;  // Utiliser Void si on n'a pas de valeur à retourner
            });
            futures.add(future);
        }

        // 7. Attendre la fin de toutes les tâches
        for (Future<Void> future : futures) {
            future.get();  // Bloque jusqu'à ce que la tâche soit terminée
        }

        // 8. Gérer les topics inexistants
        handleNonExistingTopics(keywordList, result);

        // 9. Fermer le pool d'exécution
        executorService.shutdown();*/

        return result;
    }

    private static List<String> findExistingTopics(Set<String> allTopics, List<String> keywordList) {
        List<String> existingTopics = new ArrayList<>();
        /*for (String topic : allTopics) {
            if (keywordList.contains(topic)) {
                existingTopics.add(topic);
            }
        }*/
        return existingTopics;
    }

    private static void searchMessagesInTopic(String topicName, Map<Integer, HashMap<String, String>> result) throws InterruptedException {
        /*try {
            // S'abonner au topic
            Agents.getConsummer().subscribe(Collections.singletonList(topicName));
            Agents.getConsummer().assignment().forEach(partition -> Agents.getConsummer().seekToBeginning(Collections.singletonList(partition)));

            // Poll des messages
            ConsumerRecords<String, String> records = Agents.getConsummer().poll(Duration.ofSeconds(10));

            for (ConsumerRecord<String, String> record : records) {
                HashMap<String, String> messageDetails = extractMessageDetails(record);
                if (messageDetails != null) {
                    result.put(result.size() + 1, messageDetails);
                    System.out.println(messageDetails);  // Log message
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des messages pour le topic " + topicName + ": " + e.getMessage());
        }*/
    }

    private static HashMap<String, String> extractMessageDetails(ConsumerRecord<String, String> record) {
        HashMap<String, String> messages = new HashMap<>();

        /*String message = record.value();
        System.out.println(message);  // Log message

        // Extraire les headers comme 'theme' et 'producer'
        record.headers().headers("theme").forEach(header -> {
            String themeValue = new String(header.value());
            messages.put("theme", themeValue.equals("null") ? "Theme Inconnu" : themeValue);
            messages.put("producer", record.key().equals("null") ? "Créateur Inconnu" : record.key());
        });

        if (messages.isEmpty()) {
            return null;  // Retourner null si aucun header pertinent n'est trouvé
        }

        messages.put("message", message);*/
        return messages;
    }

    private static void handleNonExistingTopics(List<String> keywordList, Map<Integer, HashMap<String, String>> result) {
        /*List<String> missingTopics = keywordList.stream()
                .filter(keyword -> {
                    try {
                        return !Publisher.listTopic().contains(keyword);
                    } catch (ExecutionException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();

        if (!missingTopics.isEmpty()) {
            System.out.println("Création de nouveaux topics pour les mots-clés manquants : " + missingTopics);
            missingTopics.forEach(keyword -> {
                try {
                    Publisher.creerTopic(keyword);
                    System.out.println("Nouveau topic créé pour le mot-clé : " + keyword);

                    // Re-scan des topics et ajout des messages dans le nouveau topic
                    Publisher.listTopic().forEach(topicName -> {
                        searchAndAddMessagesToNewTopic(keyword, topicName, result);
                    });
                } catch (ExecutionException | InterruptedException e) {
                    System.out.println("Erreur lors de la création du topic pour le mot-clé " + keyword + ": " + e.getMessage());
                }
            });
        }*/
    }

    private static void searchAndAddMessagesToNewTopic(String keyword, String topicName, Map<Integer, HashMap<String, String>> result) {
        /*try {
            Agents.getConsummer().subscribe(Collections.singletonList(topicName));
            Agents.getConsummer().assignment().forEach(partition -> Agents.getConsummer().seekToBeginning(Collections.singletonList(partition)));

            ConsumerRecords<String, String> records = Agents.getConsummer().poll(Duration.ofSeconds(5));

            for (ConsumerRecord<String, String> record : records) {
                if (record.value().contains(keyword)) {
                    HashMap<String, String> messageDetails = extractMessageDetails(record);
                    if (messageDetails != null) {
                        messageDetails.put("topic", keyword);
                        Message.creerMessage(keyword, messageDetails.get("theme"), messageDetails.get("message"), messageDetails.get("producer"));
                        result.put(result.size() + 1, messageDetails);
                        System.out.println(messageDetails);  // Log message
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des messages pour le topic " + topicName + ": " + e.getMessage());
        }*/
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

