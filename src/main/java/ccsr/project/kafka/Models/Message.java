package ccsr.project.kafka.Models;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /*public static void creerMessage(String topic,String article,String message,String user){
        ProducerRecord<String,String> producerRecord = new ProducerRecord<>(topic,article,message);
        producerRecord.headers().add("producer-id", user.getBytes(StandardCharsets.UTF_8));
        Agents.getProducer().send(producerRecord);
    }*/
}
