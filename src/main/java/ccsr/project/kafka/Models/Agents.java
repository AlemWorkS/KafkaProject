package ccsr.project.kafka.Models;

import ccsr.project.kafka.config.Config;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;
import java.util.UUID;

public class Agents {

    private final static Properties props = new Properties();
    private final static Properties propsProducer = new Properties();
    private static final AdminClient adminClient;

    private static final KafkaConsumer kafkaConsumer;
    private static final KafkaProducer kafkaProducer;


    static {
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.KAFKA_SERVERS);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "web-consumer-group-" + 1);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        adminClient = AdminClient.create(props);
        kafkaConsumer = new KafkaConsumer(props);

        propsProducer.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Config.KAFKA_SERVERS);
        propsProducer.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsProducer.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        propsProducer.put(ProducerConfig.ACKS_CONFIG, "all");

        kafkaProducer = new KafkaProducer(propsProducer);
    }


    public static AdminClient getAdminClient() {
        return adminClient;
    }

    public static KafkaConsumer getConsummer() {
        return kafkaConsumer;
    }

    public static KafkaProducer getProducer() {return kafkaProducer;}

}
