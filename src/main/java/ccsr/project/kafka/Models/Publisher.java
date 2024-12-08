package ccsr.project.kafka.Models;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.network.KafkaChannel;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.utils.KafkaThread;
import org.springframework.kafka.support.KafkaNull;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Publisher {

    /**
     * @param topicName
     * Permet de creer un topic
     */
    public static void creerTopic(String topicName) throws ExecutionException, InterruptedException {
        NewTopic newTopic = new NewTopic(topicName, 1, (short) 3);
        Agents.getAdminClient().createTopics(Collections.singleton(newTopic)).all().get();
    }

    /**
     *
     * @return un ensemble de topic
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static Set<String> listTopic() throws ExecutionException, InterruptedException {
        return Agents.getAdminClient().listTopics().names().get();
    }

    /**
     *
     * @throws ExecutionException
     * @throws InterruptedException
     * return
     */
    public static void connexion() throws ExecutionException, InterruptedException {
        Agents.getAdminClient().describeCluster().nodes().get();
    }

}
