package ccsr.project.kafka;

import ccsr.project.kafka.Controllers.DatabaseConnection;
import ccsr.project.kafka.Controllers.ProducerController;
import ccsr.project.kafka.config.Config;
import kafka.utils.SchedulerTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


class KafkaApplicationTests {


    @Test
    void testFileLoads() {
        Config.loadConfigFile();
        Config.BD_CONFIG.forEach((k,v) ->{
            Assertions.assertNotNull(v,"Les configurations ne doivent pas être null");
        });
    }

    @Test
    void testClusterOn() {
        testFileLoads();
        ProducerController producerController = new ProducerController();
        Assertions.assertEquals(HttpStatus.OK, producerController.connectToKafka().getStatusCode(),"Les serveurs sont déconnectés");
    }

    /**
     * Test : Connexion réussie avec les bonnes informations.
     */
    @Test
    void testSuccessfulConnection() {
        try {
            testFileLoads();
            Connection connection = DatabaseConnection.getConnection();
            Assertions.assertNotNull(connection, "La connexion ne doit pas être null");
            Assertions.assertTrue(connection.isValid(2), "La connexion doit être valide");
            connection.close();
        } catch (SQLException e) {
            Assertions.fail("La connexion a échoué alors qu'elle devrait réussir : " + e.getMessage());
        }
    }

    /**
     * Test : Échec de la connexion avec des informations incorrectes.
     */
    @Test
    void testFailedConnection() {
        // Configuration temporaire pour simuler des informations incorrectes
        String originalUrl = Config.BD_CONFIG.get("db_host");
        Config.BD_CONFIG.put("db_host", "invalid_host"); // Hôte invalide

        try {
            Assertions.assertThrows(SQLException.class, DatabaseConnection::getConnection,
                    "Une exception doit être levée avec des informations incorrectes");
        } finally {
            // Rétablir la configuration correcte après le test
            Config.BD_CONFIG.put("db_host", originalUrl);
        }
    }

    /**
     * Test : Comportement sous charge (simule plusieurs connexions simultanées).
     */
    @Test
    void testMultipleConnections() {

        testFileLoads();
        testSuccessfulConnection();

        int numberOfConnections = 10000; // Nombre de connexions simultanées à tester
        ExecutorService executorService = Executors.newFixedThreadPool(50); // Pool de 50 threads
        List<SQLException> exceptions = Collections.synchronizedList(new ArrayList<>()); // Liste thread-safe pour les exceptions
        ArrayList<Future<?>> futures = new ArrayList<>(); // Liste pour suivre les tâches soumises

            for (int i = 0; i < numberOfConnections; i++) {
                executorService.submit(() -> {
                    try {
                            Connection connection = DatabaseConnection.getConnection();

                            Assertions.assertNotNull(connection, "La connexion ne doit pas être null");
                            Assertions.assertTrue(connection.isValid(2), "La connexion doit être valide");

                            connection.close();

                            System.out.println(connection.isClosed());


                } catch (SQLException e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            });

        }

        // Attendre la fin de toutes les tâches
        /*for (Future<?> future : futures) {
            try {
                future.get(); // Bloque jusqu'à la fin de la tâche
            } catch (Exception e) {
                Assertions.fail("Une tâche a échoué : " + e.getMessage());
            }
        }*/

        // Arrêter le service d'exécution
        executorService.shutdown();
        // Attendre la fin de tous les threads
        /*for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                Assertions.fail("Test interrompu : " + e.getMessage());
            }
        }*/

        // Vérifier qu'il n'y a pas eu d'exceptions
        Assertions.assertTrue(exceptions.isEmpty(),
                "Des erreurs sont survenues lors de connexions simultanées : " + exceptions);
    }


}
