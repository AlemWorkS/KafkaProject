package ccsr.project;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.Topic;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
// Configuration du consommateur Kafka
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", "localhost:29092,localhost:49092"); // Adresse de votre broker Kafka
        props.setProperty("group.id", "smart-consumer-groupist");
        props.setProperty(ConsumerConfig.CLIENT_ID_CONFIG,"smart-consumer-groupist");// ID du groupe de consommateurs
        props.setProperty("key.deserializer", StringDeserializer.class.getName());
        props.setProperty("value.deserializer", StringDeserializer.class.getName());
        props.setProperty("auto.offset.reset", "latest");       // Pour lire depuis le début si aucun offset n'existe
        //props.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");       // Pour lire depuis le début si aucun offset n'existe

        // Création du consommateur Kafka
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        // Souscription au topic "smart"
        consumer.subscribe(Collections.singletonList("smart"));
// Effectuer un poll initial pour affecter les partitions
        consumer.poll(Duration.ofMillis(100));
        Set<TopicPartition> assignedPartitions = consumer.assignment();
        // Configurer le consommateur pour lire depuis le début des partitions

        System.out.println("Lecture depuis le début du topic 'smart'...");

        try {
            // Boucle infinie pour lire les messages
                // Récupération des messages
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                for (ConsumerRecord<String, String> record : records) {
                    System.out.println("Message reçu:");
                    System.out.println("  Clé: " + record.key());
                    System.out.println("  Valeur: " + record.value());
                    System.out.println("  Partition: " + record.partition());
                    System.out.println("  Offset: " + record.offset());
                }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Fermeture du consommateur
            consumer.close();
        }
    }



}
