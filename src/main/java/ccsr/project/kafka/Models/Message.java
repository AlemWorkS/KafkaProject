package ccsr.project.kafka.Models;

import ccsr.project.kafka.Controllers.DatabaseConnection;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicCollection;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.internals.Topic;

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
    public static HashMap<Integer, HashMap<String, String>> getMessagesFromTopic(String topicName, boolean fromBeginning, KafkaConsumer consumer, String userEmail) {


        KafkaConsumer onLineConsumer;
        if (consumer == null) {
            System.out.println("No consumer");
            //onLineConsumer si on est sur le web correspond au consumer qui est en ligne
            onLineConsumer = Agents.getConsummer();
        } else {
            System.out.println("Consumer provided");
            //onLineConsumer si on est en local correspond à une instance de consumer
            onLineConsumer = consumer;
        }

        HashMap<Integer, HashMap<String, String>> recordMap = new HashMap<>();
        HashMap<String, String> messageNull = new HashMap<>();

        String sanitizedTopicName = sanitizeTopicName(topicName);

        onLineConsumer.subscribe(Collections.singletonList(sanitizedTopicName));
        onLineConsumer.poll(Duration.ofMillis(100));
        onLineConsumer.unsubscribe();


        onLineConsumer.subscribe(Collections.singletonList(sanitizedTopicName));
        ConsumerRecords<String, String> records = onLineConsumer.poll(Duration.ofMillis(500));

        try {
            if (Topic.isValid(topicName) && Agents.getAdminClient().listTopics().names().get().contains(topicName)) {
                System.out.println(onLineConsumer.groupMetadata().groupId());

                System.out.println(records.count());

                while (!records.isEmpty()) {


                    for (ConsumerRecord<String, String> record : records) {

                        HashMap<String, String> message = new HashMap<>();


                        //Récupére le message du topic
                        System.out.println(record.value() + " value offset " + record.offset());

                        //Hashmap pour récupérer le theme et le producer du message
                        // Vérifier si le header "theme" existe et s'il n'est pas vide
                        // Si l'en-tête "theme" existe et n'est pas vide
                        message.put("theme", "null");
                        message.put("producer", "null");
                        record.headers().headers("theme").forEach(header -> {
                            String themeValue = new String(header.value());
                            // Vérifier si la valeur de l'en-tête est vide ou null
                            message.put("theme", themeValue);
                            message.put("producer", record.key());
                        });

                        if (message.get("theme").equals("null")) {
                            message.put("theme", "Theme Inconnu");
                        }

                        if (message.get("producer").equals("null")) {
                            message.put("producer", "Créateur Inconnu");
                        }

                        message.put("message", record.value());
                        message.put("topic", topicName);
                        recordMap.put(recordMap.size() + 1, message);
                        System.out.println(recordMap.get(recordMap.size()));
                        System.out.println("message");
                    }

                    records = onLineConsumer.poll(Duration.ofMillis(500));

                }
                onLineConsumer.close();
            } else{
                messageNull.put("message", "");
                messageNull.put("producer", "System");
                messageNull.put("theme", "Ce topic" + topicName + "n'existe pas");
                recordMap.put(0, messageNull);
            }

            if (recordMap.isEmpty()) {
                messageNull.put("message", "Aucun Nouveau Message sur le topic" + topicName);
                messageNull.put("producer", "System");
                messageNull.put("theme", "Topic " + topicName + " Vide");

                recordMap.put(0, messageNull);
            }


            /*new Thread(()->{
                annulerEnv(userEmail);
            }).start();*/

            //Confirmer qu'on à lu les derniers messages
            //onLineConsumer.commitSync();
            //Désinscription du consumer de tous les topics
            //onLineConsumer.unsubscribe();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur lors de la récupération des messages : " + e.getMessage());
            messageNull.put("message", "Erreur lors de la récupération des messages.");
            messageNull.put("producer", "System");
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


    /**
     * Cette fonction doit s'exécuter en 2 parties :
     * - La recherche du ou des mots-clés parmi les topics
     * - La création et le remplissage d'un nouveau topic contenant le(s) mot(s) clé(s) recherché(s), cas où le topic n'existe pas
     * A savoir qu'un message possède ( un thème, un producer etc... )
     *
     * @param keywords
     * @return une Map de topics et de leurs différents messages
     * @throws ExecutionException
     * @throws InterruptedException
     */
    /*public static Map<Integer, HashMap<String, String>> searchMessagesInAllTopics(String keywords) throws ExecutionException, InterruptedException {



        // 1ere partie : Recherhce du ou des mot(s) clé(s) parmi les topics et retour des différents messages

        // Séparer les mots-clés par espace et les stocker dans une liste
        ArrayList<String> listInteret = new ArrayList<>(Arrays.asList(keywords.split("\\s+")));
        ArrayList<String> listInteretExistant = new ArrayList<>();

        //La map qui va recevoir les topics et les messages assoiés
        Map<Integer, HashMap<String, String>> result = new HashMap<>();

        // Récupérer la liste de tous les topics disponibles dans Kafka (supposons que tu as une méthode pour cela)
        Set<String> allTopics = Publisher.listTopic(); // Cette ligne pourrait retourner la liste des topics recherchées


        allTopics.forEach(topicName -> {

            System.out.println(1);
            System.out.println(listInteret.contains(topicName));

            if (listInteret.contains(topicName)) {
                listInteretExistant.add(topicName);
                //Variable de la récupération des messages

                try {
                    // S'abonner au topic pour consommer TOUT les messages
                    Agents.getConsummer().subscribe(Collections.singletonList(topicName));
                    Agents.getConsummer().assignment().forEach(partition -> {

                        // Positionner l'offset au début de chaque partition pour consommer tout les messages sinon on consommera juste les nouveaux
                        Agents.getConsummer().seekToBeginning(Collections.singletonList(partition));

                    });
                    //System.out.println("Connexion au topic : " + topicName);

                    // Poll des messages (on peut ajuster la durée en fonction des besoins)
                    ConsumerRecords<String, String> records = Agents.getConsummer().poll(Duration.ofSeconds(10));

                    /*records.forEach(stringStringConsumerRecord -> {
                        stringStringConsumerRecord.value().contains(keywordList);
                    });

                    // Parcourir les messages du topic ( appelés records )
                    for (ConsumerRecord<String, String> record : records) {
                        HashMap<String, String> messages = new HashMap<>();


                        //Récupérer les messages du topic
                        String message = record.value();
                        System.out.println(message);

                        //Hashmap pour récupérer le theme et le producer du message
                        // Vérifier si le header "theme" existe et s'il n'est pas vide
                        // Si l'en-tête "theme" existe et n'est pas vide
                        record.headers().headers("theme").forEach(header -> {
                            String themeValue = new String(header.value());
                            // Vérifier si la valeur de l'en-tête est vide ou null
                            messages.put("theme", themeValue);
                            messages.put("producer", record.key());
                        });

                        if (messages.get("theme").equals("null")) {
                            messages.put("theme", "Theme Inconnu");
                        }

                        if (messages.get("producer").equals("null")) {
                            messages.put("producer", "Créateur Inconnu");
                        }

                        messages.put("message", message);
                        messages.put("topic", topicName);
                        result.put(result.size() + 1, messages);
                        System.out.println(result.get(result.size()));

                        // Si le mot-clé est trouvé, on arrête la recherche pour ce message
                    }

                } catch (Exception e) {
                    System.out.println("Erreur lors de la récupération des messages pour le topic " + topicName + ": " + e.getMessage());
                }
            }
        });

        // 2ème partie : Création et remplissage d'un nouveau topic contenant le(s) mot(s) clé(s) recherché(s), cas où le topic n'existe pas

        ArrayList<String> listInteretInexistant = new ArrayList<>();
        listInteret.forEach(i -> {
            if (!listInteretExistant.contains(i)) {
                listInteretInexistant.add(i);
                System.out.println(i);
            }
            //System.out.println(listInteretInexistant.get(listInteretInexistant.indexOf(i)));
        });

        //Si aucun topic ne porte le lib de l'intérêt
        if (result.isEmpty()) {

            System.out.println(2);
            // Si aucun message n'a été trouvé pour les mots-clés dans les topics, on crée un ou des nouveau(x) topic(s) avec le(s) mots-clé(s)

            // Parcourir les mots-clés et crée un nouveau topic pour chaque mot-clé
            listInteret.forEach(interet -> {

                try {
                    if (listInteretInexistant.contains(interet)) {
                        Publisher.creerTopic(interet);
                        System.out.println("Nouveau topic créé avec le mot-clé : " + interet);
                    }

                    for (String topicName : Publisher.listTopic()) {

                        // Rechercher les messages dans les topics existants pour les mettre dans le nouveau topic

                        try {

                            // S'abonner au topic pour consommer TOUT les messages
                            Agents.getConsummer().subscribe(Collections.singletonList(topicName));
                            Agents.getConsummer().assignment().forEach(partition -> {

                                // Positionner l'offset au début de chaque partition pour consommer tout les messages sinon on consommera juste les nouveaux
                                Agents.getConsummer().seekToBeginning(Collections.singletonList(partition));

                            });
                            //System.out.println("Connexion au topic : " + topicName);

                            // Poll des messages (on peut ajuster la durée en fonction des besoins)
                            ConsumerRecords<String, String> records = Agents.getConsummer().poll(Duration.ofSeconds(5));

                                /*records.forEach(stringStringConsumerRecord -> {
                                    stringStringConsumerRecord.value().contains(keywordList);
                                });

                            // Parcourir les messages du topic ( appelés records )
                            for (ConsumerRecord<String, String> record : records) {

                                HashMap<String, String> messages2 = new HashMap<>();

                                //Récupérer les messages du topic
                                String message = record.value();
                                System.out.println(message + " " + topicName);


                                // Si le mot-clé est trouvé, on arrête la recherche pour ce message
                                if (message.contains(interet)) {

                                    // Vérifier si le header "theme" existe et s'il n'est pas vide

                                    // Si l'en-tête "theme" existe et n'est pas vide
                                    record.headers().headers("theme").forEach(header -> {
                                        String themeValue = new String(header.value());
                                        // Vérifier si la valeur de l'en-tête est vide ou null
                                        messages2.put("theme", themeValue);
                                        messages2.put("producer", record.key());
                                    });

                                    if (messages2.get("theme") == null || messages2.get("theme").equals("null")) {
                                        messages2.put("theme", "Theme Inconnu");
                                    }

                                    if (messages2.get("producer") == null || messages2.get("producer").equals("null")) {
                                        messages2.put("producer", "Créateur Inconnu");
                                    }
                                    // Si l'en-tête "theme" n'existe pas ou est vide


                                    messages2.put("topic", interet);
                                    messages2.put("message", message);
                                    // Si des messages ont été trouvés pour ce topic, on les ajoute au résultat
                                    Message.creerMessage(interet, messages2.get("theme"), messages2.get("message"), messages2.get("producer"));
                                    result.put(result.size() + 1, messages2);

                                    // Si le mot-clé est trouvé, on arrête la recherche pour ce message
                                }
                            }


                        } catch (Exception e) {
                            System.out.println("Erreur lors de la récupération des messages pour le topic " + topicName + ": " + e.getMessage());
                        }
                    }

                } catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });


            // Si aucun message n'a été trouvé pour les mots-clés dans les topics, on retourne un message spécifique
            if (result.isEmpty()) {

                HashMap<String, String> empty = new HashMap<>();
                empty.put("producer", "System");
                empty.put("theme", "null");
                empty.put("message", "Aucun message ne contient les mots-clés spécifiés " + keywords);
                result.put(0, empty);

                System.out.println(allTopics);

            }
        }


        return result;


    }*/
    public static Map<Integer, HashMap<String, String>> searchMessagesInAllTopics(String keywords) throws ExecutionException, InterruptedException {
        // 1. Séparer les mots-clés et les stocker dans une liste
        List<String> keywordList = Arrays.asList(keywords.split("\\s+"));

        // 2. Initialiser la map des résultats
        Map<Integer, HashMap<String, String>> result = new ConcurrentHashMap<>();

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
        executorService.shutdown();

        return result;
    }

    private static List<String> findExistingTopics(Set<String> allTopics, List<String> keywordList) {
        List<String> existingTopics = new ArrayList<>();
        for (String topic : allTopics) {
            if (keywordList.contains(topic)) {
                existingTopics.add(topic);
            }
        }
        return existingTopics;
    }

    private static void searchMessagesInTopic(String topicName, Map<Integer, HashMap<String, String>> result) throws InterruptedException {
        try {
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
        }
    }

    private static HashMap<String, String> extractMessageDetails(ConsumerRecord<String, String> record) {
        HashMap<String, String> messages = new HashMap<>();

        String message = record.value();
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

        messages.put("message", message);
        return messages;
    }

    private static void handleNonExistingTopics(List<String> keywordList, Map<Integer, HashMap<String, String>> result) {
        List<String> missingTopics = keywordList.stream()
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
        }
    }

    private static void searchAndAddMessagesToNewTopic(String keyword, String topicName, Map<Integer, HashMap<String, String>> result) {
        try {
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
        }
    }


    public static void creerMessage(String topic, String article, String message, String user) {

        // Créer le ProducerRecord avec les en-têtes
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, user, message);
        if (article == null || article.isEmpty()) {
            article = "ArticleInconnu";

        }
        producerRecord.headers().add("theme", article.getBytes(StandardCharsets.UTF_8));


        // Ajouter l'en-tête "theme"

        // Afficher un message pour vérifier que l'en-tête a bien été ajouté
        //System.out.println("En-tête 'theme' ajouté avec la valeur : " + article);

        // Envoi du message avec un callback pour gérer les erreurs
        Agents.getProducer().send(producerRecord, (metadata, exception) -> {
            if (exception != null) {
                // En cas d'erreur lors de l'envoi du message
                System.err.println("Erreur lors de l'envoi du message : " + exception.getMessage());
            } else {
                // Message envoyé avec succès
                System.out.println("Message envoyé avec succès, Topic: " + topic);
            }
        });
    }


}

