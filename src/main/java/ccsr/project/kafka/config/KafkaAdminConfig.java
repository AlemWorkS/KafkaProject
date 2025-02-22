package ccsr.project.kafka.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaAdminConfig {

    @Bean
    public AdminClient adminClient() {
        String bootstrapServers = Config.KAFKA_SERVERS;

        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); // Adresse de votre cluster Kafka
        return AdminClient.create(configs);
    }
}
