package ccsr.project.kafka.Models;


import org.apache.kafka.clients.admin.ListTopicsResult;


import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Consumer{



    public static List<String> searchTopicsByInterest(String interest) throws ExecutionException, InterruptedException {

            // Liste tous les topics du cluster
            ListTopicsResult topicsResult = Agents.getAdminClient().listTopics();
            Set<String> allTopics = topicsResult.names().get();

            // Filtre les topics par mot-clé (intérêt)
            return allTopics.stream().filter(topic -> topic.toLowerCase().contains(interest.toLowerCase())).collect(Collectors.toList());

    }


}
