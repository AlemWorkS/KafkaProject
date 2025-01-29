package ccsr.project.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import java.util.Properties;

public class KafkaLoadTest {
    private static final String TOPIC_NAME = "test-topic";

    public static void main(String[] args) {
        int numberOfUsers = 10;  // 🔥 Simuler 10 utilisateurs en parallèle

        for (int i = 0; i < numberOfUsers; i++) {
            final int userId = i;
            new Thread(() -> publishMessage(userId)).start();
        }
    }

    private static void publishMessage(int userId) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:29092");  // 🔥 Change avec l'IP de ton serveur Kafka
        props.put("acks", "all");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        for (int i = 0; i < 5; i++) {  // 🔥 Chaque user envoie 5 messages
            String message = "User " + userId + " - Message " + i;
            ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, message);
            producer.send(record);
            System.out.println("Envoyé : " + message);
        }

        producer.close();
    }
}
