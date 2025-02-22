package ccsr.project.kafka.Controllers;

import ccsr.project.kafka.Models.Agents;
import ccsr.project.kafka.config.Config;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ConsumerController {

    private final AdminClient adminClient;

    @Autowired
    public ConsumerController(AdminClient adminClient) {
        this.adminClient = adminClient;
    }


    // Abonnement d'un consumer à un topic
    public static void subscribeUserToTopic(String email, String topicName) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "INSERT INTO subscription (email,topic_name) VALUES (?,?);";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, email);
            statement.setString(2, topicName);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public class KafkaListener {
        static ScheduledExecutorService topicScheduler = Executors.newScheduledThreadPool(1);
        static ExecutorService topicExecutor;

        static {
            try {
                topicExecutor = Executors.newFixedThreadPool(Agents.getAdminClient().listTopics().names().get().size()/2);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }


        public static void listenToPlanning() {
            ExecutorService executorService = Executors.newFixedThreadPool(5);

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();


            Runnable runnable = () -> {
                System.out.println("Verification du Planning.........................................................");
                //Requête de récupération des emails à envoyer
                String query = "SELECT topic,heure_env,user_mail FROM mailplanning where mail_lu = 1";

                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement statement = connection.prepareStatement(query)) {

                    ResultSet resultSet = statement.executeQuery();

                    //On récupère l'heure du système
                    Calendar calendar = Calendar.getInstance();
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);

                    //on vérifie si il y'a un résultat dans resultSet et si l'heure système est égale heure_env dans resultSet
                    while (resultSet.next() && (resultSet.getInt("heure_env") == currentHour || resultSet.getInt("heure_env") == 0)) {

                        //System.out.println("Message envoyé");


                        //On récupère les emails et les topics à qui il a été planifié d'envoyer un message
                        String subscriberEmail = resultSet.getString("user_mail");
                        String topicName = resultSet.getString("topic");

                        String emailSubject = "Nouvelle alerte pour le topic : " + topicName;
                        String emailContent = "Bonjour,\n\nUn nouveau message a été publié dans le topic '" + topicName + "' :\n\n"
                                + "\n\nCordialement,\nL'équipe de Notification Kafka";

                        executorService.execute(()-> {

                                //On envoie l'email
                                EmailConfig.sendEmail(subscriberEmail, emailSubject, emailContent);
                                //On marque qu'il n'y a plus de message à envoyer dans la table mailplanning
                                deplanifierMail(subscriberEmail);

                        });
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            };

            scheduler.scheduleAtFixedRate(runnable, 0, 60, TimeUnit.SECONDS);

        }

        public static void listenToTopic(String topicName) {

            // Configurer les propriétés du consumer

            Properties props = new Properties();
            props.put("bootstrap.servers", Config.KAFKA_SERVERS); // Remplacez par votre serveur Kafka
            props.put("group.id", "thread-daemon-1");
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("auto.offset.reset", "latest"); // Pour consommer depuis le début si aucun offset n'existe

            System.out.println(Config.KAFKA_SERVERS + "azerty--------------------------------------------");

            // Créer le consumer Kafka
            KafkaConsumer<String, String> consumerTop = new KafkaConsumer<>(props);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {

                    //Pour chaque topic on va créer un daemon pour pouvoir écouter l'arrivée de nouveau mmessage
                    consumerTop.listTopics().forEach((topic, partitionInfos) -> {
                        topicExecutor.submit(() -> {
                            if (!topic.equals("__consumer_offsets")) {
                                // Créer le consumer Kafka
                                KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);


                                try (consumer) {
                                    consumer.subscribe(Collections.singleton(topic));
                                    System.out.println("Écoute des messages du topic : " + topic);
                                    ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(1));

                                    for (ConsumerRecord<String, String> record : records) {


                                        System.out.println("Message reçu : " + record.value());

                                        // Ajouter la logique pour envoyer un email
                                        new Thread(() -> {
                                            planifierMail(topic);
                                        }).run();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    });
                }
            };

            topicScheduler.scheduleAtFixedRate(runnable,0,1,TimeUnit.SECONDS);
        }


            /**
             * Méthode pour notifier les abonnés par email.
             *
             * @param topicName Nom du topic où le message a été publié.
             */

        private static void planifierMail(String topicName) {

            boolean insert = false;
            // Récupérer les abonnés au topic
            List<String> subscribers = SubscriptionService.getSubscribersEmailsForTopic(topicName);

            // Vérifier s'il y a des abonnés
            if (subscribers.isEmpty()) {
                System.out.println("Aucun abonné pour le topic : " + topicName);
                return;
            }

            System.out.println("Azertypically");

            // Plannifier l'envoi d'un message pour chaque subscriber du topic
            for (String subscriberEmail : subscribers) {

                String querySelect = "SELECT * FROM mailplanning WHERE user_mail = ? and topic = ?";
                try (Connection connection = DatabaseConnection.getConnection();
                     PreparedStatement preparedStatement = connection.prepareStatement(querySelect)) {
                    preparedStatement.setString(1, subscriberEmail);
                    preparedStatement.setString(2, topicName);


                        ResultSet resultSet = preparedStatement.executeQuery();
                    if(resultSet == null || !resultSet.next()){
                        insert = true;
                        String query = "INSERT INTO mailplanning(topic,interval_de_jour,heure_env,user_mail,mail_lu) VALUES (?,?,?,?,?)";

                        try (PreparedStatement stat = connection.prepareStatement(query)) {


                                stat.setString(1, topicName);
                                stat.setNull(2,Types.INTEGER);
                                stat.setNull(3, Types.INTEGER);
                                stat.setString(4, subscriberEmail);
                                stat.setBoolean(5, false);

                            int rowsAffected = stat.executeUpdate();
                            System.out.println("Planning rows affected");

                        }
                    }
                    } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                if(!insert) {

                    //requête pour planifier les envoies d'email
                    String query = "UPDATE mailplanning SET mail_lu = ? WHERE user_mail = ? AND topic = ?";

                    try (Connection connection = DatabaseConnection.getConnection();
                         PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                        preparedStatement.setBoolean(1, true);
                        preparedStatement.setString(2, subscriberEmail);
                        preparedStatement.setString(3, topicName);
                        //System.out.println(6);
                        preparedStatement.executeUpdate();

                        System.out.println("Email planifié : " + subscriberEmail);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de la planification de l'email à " + subscriberEmail + " : " + e.getMessage());
                    }
                }
            }
        }

        private static void deplanifierMail(String subscriberEmail) {

            //requête pour planifier les envoies d'email
            String query = "UPDATE mailplanning SET mail_lu = ? WHERE user_mail = ?";

            try (Connection connection = DatabaseConnection.getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setBoolean(1, false);
                preparedStatement.setString(2, subscriberEmail);
                //System.out.println(6);
                preparedStatement.executeUpdate();

                System.out.println("Email envoyé : " + subscriberEmail);
            } catch (Exception e) {
                System.err.println("Erreur lors de l'envoi de l'email à " + subscriberEmail + " : " + e.getMessage());
            }

        }
    }


}

