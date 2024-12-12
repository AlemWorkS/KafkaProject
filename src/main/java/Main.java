package src.main.java;
import ccsr.project.kafka.Models.Message;
import ccsr.project.kafka.Models.Publisher;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;


import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) {

        //Message.creerMessage("Articles","Zama","moi encore ?","fred");
        /*Message.creerMessage("Articles","THZ","meilssa","fred");


        try {
            System.out.println(Message.searchMessagesInAllTopics("meilssa"));


        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }*/

        /*try {
            System.out.println(Message.searchMessagesInAllTopics("felix"));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }*/

    }

}
