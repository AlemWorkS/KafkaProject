package ccsr.project.kafka.Models;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.internals.Topic;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.*;
import java.util.concurrent.ExecutionException;


public class Message {


    // Nettoyage et validation des noms de topics avant interaction
    public static String sanitizeTopicName(String topicName) {
        return topicName
                .replaceAll("[^a-zA-Z0-9._-]", "") // Supprime les caractères non valides
                .replaceAll("\\s+", "-")           // Remplace les espaces par des tirets
                .toLowerCase();
    }

    public static List<String> getMessagesFromTopic(String topicName) {
        List<String> messages = new ArrayList<>();

        try {
            if(Topic.isValid(topicName)) {
                String sanitizedTopicName = sanitizeTopicName(topicName);
                Agents.getConsummer().subscribe(Collections.singletonList(sanitizedTopicName));

                System.out.println("Connexion au topic : " + sanitizedTopicName);
                ConsumerRecords<String, String> records = Agents.getConsummer().poll(Duration.ofSeconds(5));

                for (ConsumerRecord<String, String> record : records) {
                    messages.add(record.value());
                }
            }else {
                 messages.add("Aucun message disponible pour le moment.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des messages : " + e.getMessage());
            messages.add("Erreur lors de la récupération des messages.");
        }
        return messages;
    }


    /**
     * Cette fonction doit s'exécuter en 2 parties :
     *  - La recherche du ou des mots-clés parmi les topics
     *  - La création et le remplissage d'un nouveau topic contenant le(s) mot(s) clé(s) recherché(s), cas où le topic n'existe pas
     *  A savoir qu'un message possède ( un thème, un producer etc... )
     * @param keywords
     * @return une Map de topics et de leurs différents messages
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static Map<Integer, HashMap<String,String>> searchMessagesInAllTopics(String keywords) throws ExecutionException, InterruptedException {

        // 1ere partie : Recherhce du ou des mot(s) clé(s) parmi les topics et retour des différents messages

        // Séparer les mots-clés par espace et les stocker dans une liste
        ArrayList<String> listInteret = new ArrayList<>(Arrays.asList(keywords.split("\\s+")));
        ArrayList<String> listInteretExistant = new ArrayList<>();

        //La map qui va recevoir les topics et les messages assoiés
        Map<Integer, HashMap<String,String>> result = new HashMap<>();

        // Récupérer la liste de tous les topics disponibles dans Kafka (supposons que tu as une méthode pour cela)
        Set<String> allTopics = Publisher.listTopic(); // Cette ligne pourrait retourner la liste des topics recherchées



        allTopics.forEach(topicName ->{

            System.out.println(1);
            System.out.println(listInteret.contains(topicName));

            if(listInteret.contains(topicName)) {
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
                    });*/

                    // Parcourir les messages du topic ( appelés records )
                    for (ConsumerRecord<String, String> record : records) {
                        HashMap<String, String> messages = new HashMap<>();


                        //Récupérer les messages du topic
                        String message = record.value();
                        System.out.println(message);

                        //Hashmap pour récupérer le theme et le producer du message
                        // Vérifier si le header "theme" existe et s'il n'est pas vide
                        if (record.headers().headers("theme") != null) {
                            // Si l'en-tête "theme" existe et n'est pas vide
                            record.headers().headers("theme").forEach(header -> {
                                String themeValue = new String(header.value());
                                // Vérifier si la valeur de l'en-tête est vide ou null
                                messages.put("theme", (themeValue == null || themeValue.isEmpty()) ? "Inconnu" : themeValue);
                                messages.put("producer", (record.key() == null || record.key().isEmpty()) ? "Key" : record.key());
                            });
                        } else {
                            // Si l'en-tête "theme" n'existe pas ou est vide
                            messages.put("theme", "Inconnu");
                            messages.put("producer", (record.key() == null || record.key().isEmpty()) ? "Key" : record.key());
                        }



                        messages.put("message", message);
                        messages.put("topic", topicName);
                        result.put(result.size()+1, messages);
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
        listInteret.forEach(i ->{
            if(!listInteretExistant.contains(i)) {listInteretInexistant.add(i);System.out.println(i);}
            //System.out.println(listInteretInexistant.get(listInteretInexistant.indexOf(i)));
        });

        //Si aucun topic ne porte le lib de l'intérêt
        if(result.isEmpty()){

            System.out.println(2);
            // Si aucun message n'a été trouvé pour les mots-clés dans les topics, on crée un ou des nouveau(x) topic(s) avec le(s) mots-clé(s)

            // Parcourir les mots-clés et crée un nouveau topic pour chaque mot-clé
            listInteret.forEach(interet->{

                    try {
                        if(listInteretInexistant.contains(interet)) {
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
                                });*/

                                // Parcourir les messages du topic ( appelés records )
                                for (ConsumerRecord<String, String> record : records) {

                                    HashMap<String, String> messages2 = new HashMap<>();

                                    //Récupérer les messages du topic
                                    String message = record.value();
                                    System.out.println(message+" "+topicName);

                                if (message.contains(interet)) {
                                    record.headers().headers("theme").forEach(header -> {
                                            messages2.put("theme", new String(header.value()).isEmpty() ? "Inconnu" : new String(header.value()));
                                            messages2.put("producer", record.key().isEmpty() ? "Key" : record.key());
                                        });

                                    messages.put("message", message);
                                    // Si des messages ont été trouvés pour ce topic, on les ajoute au résultat
                                    Message.creerMessage(interet, messages.get("theme"), messages.get("message"), messages.get("producer"));
                                    result.put(result.size() + 1, messages);

                                    // Si le mot-clé est trouvé, on arrête la recherche pour ce message
                                    if (message.contains(interet)) {

                                        // Vérifier si le header "theme" existe et s'il n'est pas vide
                                        if (record.headers().headers("theme") != null) {
                                            // Si l'en-tête "theme" existe et n'est pas vide
                                            record.headers().headers("theme").forEach(header -> {
                                                String themeValue = new String(header.value());
                                                // Vérifier si la valeur de l'en-tête est vide ou null
                                                messages2.put("theme", (themeValue == null || themeValue.isEmpty()) ? "Inconnu" : themeValue);
                                                messages2.put("producer", (record.key() == null || record.key().isEmpty()) ? "Key" : record.key());
                                            });
                                        } else {
                                            // Si l'en-tête "theme" n'existe pas ou est vide
                                            messages2.put("theme", "Inconnu");
                                            messages2.put("producer", (record.key() == null || record.key().isEmpty()) ? "Key" : record.key());
                                        }

                                        messages2.put("message", message);
                                        // Si des messages ont été trouvés pour ce topic, on les ajoute au résultat
                                        Message.creerMessage(interet, messages2.get("theme"), messages2.get("message"), messages2.get("producer"));
                                        result.put(result.size()+1, messages2);

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
        }

        // Si aucun message n'a été trouvé pour les mots-clés dans les topics, on retourne un message spécifique
        if (result.isEmpty()) {

            HashMap<String,String> empty = new HashMap<>();
            empty.put("producer", "System");
            empty.put("theme","null");
            empty.put("message", "Aucun message ne contient les mots-clés spécifiés "+keywords);
            result.put(0, empty);

            System.out.println(allTopics);

        }

        return result;

    }


    public static void creerMessage(String topic, String article, String message, String user) {

        // Créer le ProducerRecord avec les en-têtes
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "zaha", message);


        // Ajouter l'en-tête "theme"
        producerRecord.headers().add("theme",(article != null && article.isEmpty()) ? "ArticleInconnu".getBytes(StandardCharsets.UTF_8) : article.getBytes(StandardCharsets.UTF_8));

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
