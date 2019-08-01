-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               10.3.11-MariaDB - mariadb.org binary distribution
-- Server OS:                    Win64
-- HeidiSQL Version:             9.4.0.5125
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for sharing_index
CREATE DATABASE IF NOT EXISTS `sharing_index` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */;
USE `sharing_index`;

-- Dumping structure for table sharing_index.fileshared
CREATE TABLE IF NOT EXISTS `fileshared` (
  `peerID` int(10) unsigned NOT NULL,
  `fileID` int(10) unsigned NOT NULL,
  KEY `FK_peerID` (`peerID`),
  KEY `FK_fileID` (`fileID`),
  CONSTRAINT `FK_fileID` FOREIGN KEY (`fileID`) REFERENCES `peerfile` (`fileGUID`) ON DELETE CASCADE,
  CONSTRAINT `FK_peerID` FOREIGN KEY (`peerID`) REFERENCES `peer` (`peerGUID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.
-- Dumping structure for table sharing_index.peer
CREATE TABLE IF NOT EXISTS `peer` (
  `peerGUID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `hostAddress` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`peerGUID`),
  UNIQUE KEY `unique_addr` (`hostAddress`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Data exporting was unselected.
-- Dumping structure for table sharing_index.peerfile
CREATE TABLE IF NOT EXISTS `peerfile` (
  `fileGUID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `fileName` varchar(256) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`fileGUID`)
) ENGINE=InnoDB AUTO_INCREMENT=56 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


USE `sharing_index`;
CREATE USER 'index_user'@'localhost' IDENTIFIED BY 'd44d614319262e21363be1c86d6f9fc2';
GRANT USAGE ON *.* TO 'index_user'@'localhost';
GRANT SELECT, SHOW VIEW, DELETE, INSERT, UPDATE  ON `sharing\_index`.* TO 'index_user'@'localhost';
FLUSH PRIVILEGES;


-- Data exporting was unselected.
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
