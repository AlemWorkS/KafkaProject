package ccsr.project.kafka;

import ccsr.project.kafka.Models.Agents;
import ccsr.project.kafka.Models.Message;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class KafkaProducerTests {

    KafkaApplicationTests kafkaApplicationTests = new KafkaApplicationTests();

    @Test
    void testSendMessage() {

        // Tester le chargement des configurations avant tout
        kafkaApplicationTests.testClusterOn();

        Future<Void> future = Message.creerMessage(VariableTest.topic, VariableTest.theme, VariableTest.message, VariableTest.user);

        // Test sending a message to Kafka topic
        try {
            assertNull(future.get());
        } catch (InterruptedException | ExecutionException e) {
            Assertions.fail("Test doesn't sended a message to Kafka topic");
        }


    }


}
