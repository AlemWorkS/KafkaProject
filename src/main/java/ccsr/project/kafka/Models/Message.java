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
    public static Map<String, HashMap<String,String>> searchMessagesInAllTopics(String keywords) throws ExecutionException, InterruptedException {

        // 1ere partie : Recherhce du ou des mot(s) clé(s) parmi les topics et retour des différents messages

        // Séparer les mots-clés par espace et les stocker dans une liste
        ArrayList<String> listInteret = new ArrayList<>(Arrays.asList(keywords.split("\\s+")));

        //La map qui va recevoir les topics et les messages assoiés
        Map<String, HashMap<String,String>> result = new HashMap<>();

        // Récupérer la liste de tous les topics disponibles dans Kafka (supposons que tu as une méthode pour cela)
        Set<String> allTopics = Publisher.listTopic(); // Cette ligne pourrait retourner la liste des topics recherchées

        for (String topicName : allTopics) {

            if(listInteret.contains(topicName)) {

                //Variable de la récupération des messages
                HashMap<String, String> messages = new HashMap<>();

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

                        //Récupérer les messages du topic
                        String message = record.value();

                        //Hashmap pour récupérer le theme et le producer du message
                        record.headers().headers("theme").forEach(header -> {
                            messages.put("theme", new String(header.value()));
                            messages.put("producer", record.key());
                        });
                        messages.put("message", message);
                        // Si le mot-clé est trouvé, on arrête la recherche pour ce message
                    }

                    // Si des messages ont été trouvés pour ce topic, on les ajoute au résultat
                    if (!messages.isEmpty()) {
                        result.put(topicName, messages);
                    }
                } catch (Exception e) {
                    System.out.println("Erreur lors de la récupération des messages pour le topic " + topicName + ": " + e.getMessage());
                }
            }
        }

        // Si aucun message n'a été trouvé pour les mots-clés dans les topics, on retourne un message spécifique
        if (result.isEmpty()) {
            result.put("Aucun topic ne contient les mots-clés spécifiés "+keywords,null);
        }


        return result;

    }


    public static void creerMessage(String topic, String article, String message, String user) {

        // Créer le ProducerRecord avec les en-têtes
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "zaha", message);

        // Ajouter l'en-tête "theme"
        producerRecord.headers().add("theme", article.getBytes(StandardCharsets.UTF_8));

        // Afficher un message pour vérifier que l'en-tête a bien été ajouté
        System.out.println("En-tête 'theme' ajouté avec la valeur : " + article);

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
