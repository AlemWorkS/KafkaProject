package ccsr.project.kafka;

import ccsr.project.kafka.Controllers.KafkaTopicController;
import ccsr.project.kafka.Models.Agents;
import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.Models.Publisher;
import ccsr.project.kafka.config.Config;
import jakarta.servlet.http.HttpSession;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.util.Assert;

import java.lang.reflect.Executable;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mockStatic;

public class KafkaClientTests {

    KafkaApplicationTests kafkaApplicationTests = new KafkaApplicationTests();
    KafkaProducerTests kafkaProducerTests = new KafkaProducerTests();

    @Test
    void authentificationTest(){

    }

    @Test
    void getMessagesTest(){

        kafkaApplicationTests.testFileLoads();

        Message.creerMessage(VariableTest.topic,VariableTest.theme, VariableTest.message,VariableTest.user);

        HashMap<Integer,HashMap<String,String>> map = Message.getMessagesFromTopic(VariableTest.topic,true,null,VariableTest.user);
        Assertions.assertEquals(map.get(0).get("topic"), VariableTest.topic);
        Assertions.assertEquals(map.get(0).get("theme"), VariableTest.theme);
        Assertions.assertEquals(map.get(0).get("message"), VariableTest.message);
        Assertions.assertEquals(map.get(0).get("producer"), VariableTest.user);

    }

    @Test
    void multiGetMessagesTest() {

        kafkaApplicationTests.testFileLoads();

        Assertions.assertNotNull(Message.creerMessage(VariableTest.topic, VariableTest.theme, VariableTest.message, VariableTest.user));

        ArrayList<Thread> threads = new ArrayList();

        AtomicInteger i = new AtomicInteger(0);

        for (i.get(); i.get() < 1000; i.incrementAndGet()) {
            Thread t = new Thread(()->{
                Properties props = new Properties();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Config.KAFKA_SERVERS); // Remplacez par votre serveur Kafka
                props.put(ConsumerConfig.GROUP_ID_CONFIG, "thread-"+i.get());
                props.put(ConsumerConfig.CLIENT_ID_CONFIG, "thread-"+i.get());

                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringDeserializer");
                props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

                KafkaConsumer kafkaConsumer = new KafkaConsumer(props);
                System.out.println("consummer ok");

                synchronized (this) {
                    HashMap<Integer, HashMap<String, String>> map = Message.getMessagesFromTopic(VariableTest.topic, true, kafkaConsumer, VariableTest.user);
                    Assertions.assertEquals(map.get(0).get("topic"), VariableTest.topic);
                    Assertions.assertEquals(map.get(0).get("theme"), VariableTest.theme);
                    Assertions.assertEquals(map.get(0).get("message"), VariableTest.message);
                    Assertions.assertEquals(map.get(0).get("producer"), VariableTest.user);
                }
            });
            t.start();
            threads.add(t);
        }


        for (Thread t : threads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
