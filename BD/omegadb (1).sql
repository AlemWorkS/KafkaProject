-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : ven. 27 déc. 2024 à 00:12
-- Version du serveur : 8.3.0
-- Version de PHP : 8.2.18

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `omegadb`
--

-- --------------------------------------------------------

--
-- Structure de la table `mailplanning`
--

DROP TABLE IF EXISTS `mailplanning`;
CREATE TABLE IF NOT EXISTS `mailplanning` (
  `id` int NOT NULL AUTO_INCREMENT,
  `topic` varchar(15) NOT NULL,
  `interval_de_jour` int NOT NULL,
  `dernier_jour_env` varchar(15) NOT NULL,
  `heure_env` int NOT NULL,
  `user_mail` varchar(60) NOT NULL,
  `mail_lu` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Déchargement des données de la table `mailplanning`
--

INSERT INTO `mailplanning` (`id`, `topic`, `interval_de_jour`, `dernier_jour_env`, `heure_env`, `user_mail`, `mail_lu`) VALUES
(1, 'smart', 0, 'JEUDI', 23, 'freddegny@gmail.com', 0);

-- --------------------------------------------------------

--
-- Structure de la table `subscription`
--

DROP TABLE IF EXISTS `subscription`;
CREATE TABLE IF NOT EXISTS `subscription` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `topic_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `email` (`email`),
  KEY `topics_id` (`topic_name`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `subscription`
--

INSERT INTO `subscription` (`id`, `email`, `topic_name`, `created_at`) VALUES
(10, 'eoyotode@gmail.com', 'sport', '2024-12-08 11:16:16'),
(11, 'eoyotode@gmail.com', 'sport', '2024-12-12 12:54:08'),
(12, 'eoyotode@gmail.com', 'sport', '2024-12-12 12:54:08'),
(13, 'eoyotode@gmail.com', 'sport', '2024-12-12 12:54:08'),
(14, 'eoyotode@gmail.com', 'sport', '2024-12-12 12:54:08'),
(15, 'eoyotode@gmail.com', 'sport', '2024-12-12 12:54:08'),
(16, 'eoyotode@gmail.com', 'sport', '2024-12-12 12:54:08'),
(17, 'eoyotode@gmail.com', 'sport', '2024-12-12 12:54:09'),
(18, 'freddegny@gmail.com', 'smart', '2024-12-26 22:22:36');

-- --------------------------------------------------------

--
-- Structure de la table `topics`
--

DROP TABLE IF EXISTS `topics`;
CREATE TABLE IF NOT EXISTS `topics` (
  `topics_id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`topics_id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `topics`
--

INSERT INTO `topics` (`topics_id`, `name`, `created_at`) VALUES
(1, 'sport', '2024-12-07 21:08:35'),
(2, 'javaguides', '2024-12-07 23:44:23'),
(3, 'smart', '2024-12-26 22:21:18');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `username` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`email`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`email`, `username`, `created_at`) VALUES
('eooyotode@gmail.com', 'jojo225@', '2024-12-07 21:22:14'),
('eoyotode@gmail.com', 'jojo225', '2024-12-07 16:58:19'),
('freddegny@gmail.com', 'fred', '2024-12-26 22:22:05');

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `subscription`
--
ALTER TABLE `subscription`
  ADD CONSTRAINT `subscription_ibfk_1` FOREIGN KEY (`email`) REFERENCES `users` (`email`),
  ADD CONSTRAINT `subscription_ibfk_2` FOREIGN KEY (`topic_name`) REFERENCES `topics` (`name`) ON DELETE RESTRICT ON UPDATE RESTRICT;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
