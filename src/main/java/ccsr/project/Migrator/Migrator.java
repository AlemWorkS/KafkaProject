package ccsr.project.Migrator;

import ccsr.project.kafka.Controllers.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class Migrator {
    public static void main(String[] args) {
        /*if (args.length == 0) {
            System.out.println("Usage: java MyCommand <option>");
            return;
        }*/

        /*String command = args[0];
        if (command.equalsIgnoreCase("Migrate")) {*/
        try {
            runMigrations();
        } catch (Exception e) {
            e.printStackTrace();
        }
        /*} else {
            System.out.println("Unknown command: " + command);
        }*/
    }

    private static void runMigrations() throws IOException, InterruptedException {


        // Chemin vers le dossier contenant les migrations
        String migrationsPath = "src/main/resources/db/migrations";
        File migrationsFolder = new File(migrationsPath);

        if (!migrationsFolder.exists() || !migrationsFolder.isDirectory()) {
            System.out.println("Le dossier des migrations est introuvable : " + migrationsPath);
            return;
        }

        // Parcourir les fichiers SQL
        File[] sqlFiles = migrationsFolder.listFiles((dir, name) -> name.endsWith(".sql"));
        if (sqlFiles == null || sqlFiles.length == 0) {
            System.out.println("Aucun fichier SQL trouvé dans le dossier : " + migrationsPath);
            return;
        }

        // Exécuter chaque fichier SQL
        for (File sqlFile : sqlFiles) {
            System.out.println("Exécution de la migration : " + sqlFile.getName());

            try (Statement stmt = DatabaseConnection.getConnection().createStatement()) {
                // Lire le contenu du fichier SQL
                String sql = Files.readString(sqlFile.toPath()); // Lire tout le contenu du fichier
                executeSqlScript(sql); // Exécuter le SQL contenu dans le fichier
                System.out.println("Migration réussie : " + sqlFile.getName());
            } catch (SQLException e) {
                System.err.println("Erreur pendant la migration : " + sqlFile.getName());
                e.printStackTrace();
            }
        }
    }

    private static void executeSqlScript(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // Diviser le script SQL en commandes séparées par ";"
            String[] commands = sql.split(";");

            for (String command : commands) {
                System.out.println(" -> " + command);
                command = command.trim();

                if (command.isEmpty() || command.startsWith("--") || command.startsWith("/*!")) {
                    if(readSqlCommands(command).isEmpty()) continue; // Ignorer les commentaires et les blocs de version
                    command = readSqlCommands(command).getFirst();
                }

                // Affichage de la commande pour vérifier
                System.out.println("Exécution de la commande : " + command);

                // Traiter les commandes liées à la transaction (START TRANSACTION, COMMIT, etc.)
                try {
                    // Si la commande est une transaction, ne pas l'exécuter ici
                    if (command.equalsIgnoreCase("START TRANSACTION") || command.equalsIgnoreCase("COMMIT")) {
                        // On ignore ces commandes, elles seront gérées par MySQL
                        continue;
                    }

                    statement.execute(command); // Exécuter la commande
                } catch (SQLException e) {
                    System.err.println("Erreur lors de l'exécution de la commande : " + command);
                    throw e;
                }
            }
        }
    }

    private static List<String> readSqlCommands(String sql) {
        List<String> sqlCommands = new ArrayList<>();
        StringBuilder commandBuilder = new StringBuilder();
        String[] lines = sql.split("\n"); // Diviser par lignes
        boolean started = false;  // Variable pour savoir quand commencer à récupérer les lignes

        for (String line : lines) {
            line = line.trim();  // Nettoyer la ligne

            // Vérifier si la ligne commence par une lettre (A-Z, a-z)
            if (line.matches("^[a-zA-Z].*")) {
                started = true;  // Début de la section des commandes SQL

                // Si une commande est déjà en construction, l'ajouter avant de commencer une nouvelle
                if (commandBuilder.length() > 0) {
                    sqlCommands.add(commandBuilder.toString().trim());  // Ajouter la commande terminée
                    commandBuilder.setLength(0);  // Réinitialiser le StringBuilder
                }

                // Commencer une nouvelle commande
                commandBuilder.append(line);
            } else if (started && commandBuilder.length() > 0) {
                // Ajouter la ligne à la commande en cours si on a déjà commencé à récupérer les commandes
                commandBuilder.append(" ").append(line);
            }
        }

        // Ajouter la dernière commande si elle existe
        if (commandBuilder.length() > 0) {
            sqlCommands.add(commandBuilder.toString().trim());
        }

        return sqlCommands;
    }

}
