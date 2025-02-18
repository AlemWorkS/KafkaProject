Ce qu’il faut savoir une fois le projet téléchargé

Version de Java Utilisée: Java22

1.	Télécharger le projet sur votre ordinateur

----------Configuration de la base de données----------

2.	Activer votre serveur de base de données
3.	Importer la base de données contenu dans le dossier deploy du projet

----------Configuration de docker----------

4.	Activer docker
5.	Ouvrir le dossier deploy en ligne de commande (''Assurez-vous que le fichier zookafnew.yml est présent dans le dossier deploy'')
6.	Entrer la commande docker-compose -f zookafnew.yml up -d
7.	Les brockeurs devraient se mettre en marche dans docker

---------Configuration de l’application Java---------

8.	Récupérer le chemin d’accès jusqu’au fichier config.yaml contenu dans le dossier deploy ( ex: ‘..\config.yaml’)
9.	Créer une variable d’environnement CONFIG_PATH
10.	Placer le chemin d’accès dans la variable d’environnement
11.	Dans le fichier config.yaml, modifier les information nécessaires
a.	Exemple
servers_kafka: kafka1:29092,kafka2:39092,kafka3:49092
db_host: 127.0.0.1
db_port: 3306
db_name: omegadb
db_user: user
db_password: password

----------Lancement du service Java Kafka---------
12.	Lancer le projet kafka (La class KafkaApplication.java)
13.	Entrez dans un navigateur et testez ‘’localhost:8080’’
	

---------------------------------------------------------------------------------TEST DU DEPLOIEMENT DOCKER----------------------------------------------------------------------------

Ouvrir le dossier  deploy 

Configurer le fichier config.yaml:

4.	Dans le fichier config.yaml, modifier les information nécessaires tel que:

	servers_kafka: kafka1:19092,kafka2:19092,kafka3:19092
	db_host: mysql
	db_port: 3306
	db_name: omegadb
	db_user: user
	db_password: password

5.	Ensuite ouvrir le dossier deploy en ligne de commande 
6.	Entrer la commande : docker build -t kafka_app:v1 .
7.	Entrer la commande docker-compose -f zookafnew.yml up -d
8.	Patienter 10 secondes le temps de lancement de tout

Ouvrir docker et vérifier que tous les servers sont lancés 
Tester dans un navigateur l’adresse localhost:8080