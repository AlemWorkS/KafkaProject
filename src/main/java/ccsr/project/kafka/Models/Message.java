package ccsr.project.kafka.Models;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;


public class Message {

    public static List<String> getMessagesFromTopic(String topicName) {

        List<String> messages = new ArrayList<>();

        try {
            Agents.getConsummer().subscribe(Collections.singletonList(topicName));

            System.out.println("Connexion au topic : " + topicName);
            ConsumerRecords<String, String> records = Agents.getConsummer().poll(Duration.ofSeconds(5));

            for (ConsumerRecord<String, String> record : records) {
                messages.add(record.value());
            }

            if (messages.isEmpty()) {
                messages.add("Aucun message disponible pour le moment.");
            }
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des messages : " + e.getMessage());
            messages.add("Erreur lors de la récupération des messages.");
        }
        System.out.println(messages);
        return messages;

    }


    public static Map<String, List<String>> searchMessagesInAllTopics(String keywords) throws ExecutionException, InterruptedException {

        // Séparer les mots-clés par espace et les stocker dans une liste
        String[] keywordList = keywords.split("\\s+");

        Map<String, List<String>> result = new HashMap<>();

        // Récupérer la liste de tous les topics disponibles dans Kafka (supposons que tu as une méthode pour cela)

        Set<String> allTopics = Publisher.listTopic(); // Cette méthode doit retourner tous les topics disponibles dans Kafka.

        for (String topicName : allTopics) {

            List<String> messages = new ArrayList<>();

            try {
                // S'abonner au topic
                Agents.getConsummer().subscribe(Collections.singletonList(topicName));
                Agents.getConsummer().assignment().forEach(partition -> {
                    // Positionner l'offset au début de chaque partition
                    Agents.getConsummer().seekToBeginning(Collections.singletonList(partition));
                });
                System.out.println("Connexion au topic : " + topicName);
                

                // Poll des messages (on peut ajuster la durée en fonction des besoins)
                ConsumerRecords<String, String> records = Agents.getConsummer().poll(Duration.ofSeconds(5));

                // Parcourir les messages du topic
                for (ConsumerRecord<String, String> record : records) {
                    String message = record.value();
                    System.out.println(message+" message");

                    // Vérifier si le message contient l'un des mots-clés
                    for (String keyword : keywordList) {
                        if (message.contains(keyword) || topicName.contains(keyword)) {

                            messages.add(message);
                            break;  // Si le mot-clé est trouvé, on arrête la recherche pour ce message
                        }
                    }
                }

                // Si des messages ont été trouvés pour ce topic, on les ajoute au résultat
                if (!messages.isEmpty()) {
                    result.put(topicName, messages);
                }
            } catch (Exception e) {
                System.out.println("Erreur lors de la récupération des messages pour le topic " + topicName + ": " + e.getMessage());
            }
        }

        // Si aucun message n'a été trouvé pour les mots-clés dans les topics, on retourne un message spécifique
        if (result.isEmpty()) {
            result.put("Aucun topic ne contient les mots-clés spécifiés "+keywords, Collections.emptyList());
        }

        System.out.println(result);
        return result;
    }


    /*public static void creerMessage(String topic,String article,String message,String user){
        ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topic,article,message);
        producerRecord.headers().add("producer-id", user.getBytes(StandardCharsets.UTF_8));
        Agents.getProducer().send(producerRecord);
    }*/
}
