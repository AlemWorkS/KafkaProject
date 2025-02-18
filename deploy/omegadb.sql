-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1:3306
-- Généré le : lun. 17 fév. 2025 à 21:51
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
  `topic` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `interval_de_jour` int DEFAULT NULL,
  `heure_env` int DEFAULT NULL,
  `user_mail` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `mail_lu` tinyint(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb3;

--
-- Déchargement des données de la table `mailplanning`
--

INSERT INTO `mailplanning` (`id`, `topic`, `interval_de_jour`, `heure_env`, `user_mail`, `mail_lu`) VALUES
(12, 'topic1', NULL, NULL, 'freddegny@gmail.com', 0),
(13, 'Test', NULL, NULL, 'freddegny@gmail.com', 0);

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
) ENGINE=InnoDB AUTO_INCREMENT=31 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `subscription`
--

INSERT INTO `subscription` (`id`, `email`, `topic_name`, `created_at`) VALUES
(29, 'freddegny@gmail.com', 'topic1', '2025-02-01 00:43:21'),
(30, 'freddegny@gmail.com', 'Test', '2025-02-01 16:12:05');

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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `topics`
--

INSERT INTO `topics` (`topics_id`, `name`, `created_at`) VALUES
(1, 'sport', '2024-12-07 21:08:35'),
(2, 'javaguides', '2024-12-07 23:44:23'),
(3, 'smart', '2025-01-06 22:14:48'),
(4, 'kivu', '2025-01-07 13:13:53'),
(5, 'topic1', '2025-01-21 13:42:10'),
(6, 'Test', '2025-01-29 14:30:03');

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE IF NOT EXISTS `users` (
  `email` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `username` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `first_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `last_name` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL,
  `role` varchar(50) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'Consumer',
  PRIMARY KEY (`email`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`email`, `username`, `created_at`, `first_name`, `last_name`, `password`, `role`) VALUES
('eooyotode@gmail.com', 'jojo225@', '2024-12-07 21:22:14', '', '', 'jk', 'Consumer'),
('eoyotode@gmail.com', 'jojo20', '2024-12-26 14:22:26', 'eunice', 'oyotodé', 'jojo225', 'Consumer'),
('eun@gmail.com', 'n@gmail.com', '2024-12-29 15:47:06', 'aaa', 'bbb', 'nnn', 'Producer'),
('freddegny@gmail.com', 'morales', '2024-12-12 10:30:27', '', '', '0101', 'Consumer');

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `subscription`
--
ALTER TABLE `subscription`
  ADD CONSTRAINT `subscription_ibfk_1` FOREIGN KEY (`email`) REFERENCES `users` (`email`),
  ADD CONSTRAINT `subscription_ibfk_2` FOREIGN KEY (`topic_name`) REFERENCES `topics` (`name`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
