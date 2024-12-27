package ccsr.project.kafka.config;

import ccsr.project.kafka.Controllers.DatabaseConnection;
import ccsr.project.kafka.Controllers.EmailUtil;
import ccsr.project.kafka.Controllers.KafkaService;
import ccsr.project.kafka.Controllers.SubscriptionService;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Configuration
public class KafkaTopicConfig {
    @Bean
    public NewTopic baseTopic(){
        return TopicBuilder.name("javaguides").build();
    }

}
