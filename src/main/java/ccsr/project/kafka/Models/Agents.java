package ccsr.project.kafka.Models;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Properties;
import java.util.UUID;

abstract class Agents {

    private final static Properties props = new Properties();
    private static final AdminClient adminClient;
    private static final KafkaConsumer kafkaConsumer;


    static {
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:29092,localhost:39092,localhost:49092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "web-consumer-group-" + UUID.randomUUID());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        adminClient = AdminClient.create(props);
        kafkaConsumer = new KafkaConsumer(props);
    }


    public static AdminClient getAdminClient() {
        return adminClient;
    }
    public static KafkaConsumer getConsummer() {
        return kafkaConsumer;
    }


}
