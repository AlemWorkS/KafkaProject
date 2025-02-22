Guide de l�application

Version de Java Utilis�e : Java22

----------Configuration pour d�ploiement----------
1.	T�l�charger le projet sur votre ordinateur
2.	Activer docker

3.	Ouvrir le dossier deploy en ligne de commande (��Assurez-vous que le fichier zookafnew.yml est pr�sent dans le dossier deploy��)


4.	Entrer la commande pour construire l�image de l�application�: docker build -t kafka_app:v1 . 

5.	Allez v�rifier dans kafka que vous avez bien cette image


6.	Entrer la commande pour lancer les conteneurs�: docker-compose -f zookafnew.yml up -d

7.	Les brockers devraient se mettre en marche dans docker


----------Configuration pour le d�veloppement----------
1.	R�cup�rer le chemin d�acc�s jusqu�au fichier config.yaml contenu � la racine du projet 

2.	Cr�er une variable d�environnement CONFIG_PATH


3.	Placer le chemin d�acc�s dans la variable d�environnement

3.	Ouvrir votre serveur de base de donn�es

4.	Importez sous le nom de omegadb la base de donn�es

5.	Dans le fichier config.yaml, modifier les information n�cessaires
	Exemple :
servers_kafka: kafka1:29092,kafka2:39092,kafka3:49092
db_host: 127.0.0.1
db_port: 3306
db_name: omegadb
db_user: user
db_password: password

6.	Ouvrez docker et lancez le conteneur deploy qui contient les brokers Kafka. NB�: Pour ne pas avoir un probl�me de port car l�application sert d�j� sur le port 8080 avec docker il y�a 3 solutions�:
a. D�sactivez le conteneur qui porte le nom ccsr_app dans le conteneur deploy

b. Changez le port dans le fichier "..\src\main\resources\application.properties" (vous pouvez choisir un autre port libre)

c.	Changer le port de sortie sur docker lequel sert � l�application � �tre disponible sur notre machine locale dans le fichier "..\deploy\zookafnew.yml"

7.	Lancer le projet kafka (La class KafkaApplication.java) dans votre IDE
	
8.	Entrez dans un navigateur l�url ��localhost:8080��
	
	
Explication de fonctionnement�:
L�application utilise des threads qui se lance d�s la fin de la compilation. Un daemon qui se charge de lire un planning dans la base de donn�es et un autre qui se charge de lire les nouveaux messages sur les topics.
Vous pouvez vous inscrire en tant que Consumer ou Producer vous avez la capacit� de�:
Consumer�: Lire les messages, S�abonner aux topics, Planifier des notifications, Se connecter, S�inscrire
Producer�: Cr�er des messages, Se connecter, S�inscrire
Lorsqu�un message est entr� dans un topic au quel un utilisateur/consommateur est abonn� et a d�fini une heure de r�ception des notifications, le daemon qui se charge de lire les topics chaque 15 seconds va�:
1. Rechercher tous les abonn�s au topic
2. Lancer un thread pour planifier une notification pour chaque abonn� en laissant une marque pour l�autre daemon de planning
Par la suite, Le daemon qui se charge de lire le planning chaque 60 seconds va�: 
1. Lire la variable marqu�e � true (envoy�)
2. Confier la t�che � un thread pour envoyer le mail � l�utilisateur qui est obligatoirement abonn� et a configur� la r�ception de notifications.
Bonne utilisation

