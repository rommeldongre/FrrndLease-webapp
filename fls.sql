-- phpMyAdmin SQL Dump
-- version 4.2.9.1
-- http://www.phpmyadmin.net
--
-- Host: localhost:3306
-- Generation Time: Apr 28, 2016 at 03:40 AM
-- Server version: 5.5.40
-- PHP Version: 5.4.34

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `fls`
--

-- --------------------------------------------------------

--
-- Table structure for table `category`
--

CREATE TABLE IF NOT EXISTS `category` (
  `cat_name` varchar(255) NOT NULL,
  `cat_desc` varchar(255) DEFAULT NULL,
  `cat_parent` varchar(255) DEFAULT NULL,
  `cat_child` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `config`
--

CREATE TABLE IF NOT EXISTS `config` (
  `option` varchar(255) NOT NULL,
  `value` varchar(1024) NOT NULL,
`id` int(11) NOT NULL
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

-- --------------------------------------------------------

--
-- Table structure for table `friends`
--

CREATE TABLE IF NOT EXISTS `friends` (
  `friend_id` varchar(255) NOT NULL DEFAULT '',
  `friend_full_name` varchar(255) DEFAULT NULL,
  `friend_mobile` varchar(255) DEFAULT NULL,
  `friend_user_id` varchar(255) NOT NULL DEFAULT ''
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `items`
--

CREATE TABLE IF NOT EXISTS `items` (
`item_id` int(11) NOT NULL,
  `item_name` varchar(255) NOT NULL,
  `item_category` varchar(255) DEFAULT NULL,
  `item_desc` varchar(255) DEFAULT NULL,
  `item_user_id` varchar(255) DEFAULT NULL,
  `item_lease_value` int(11) DEFAULT NULL,
  `item_lease_term` varchar(255) DEFAULT NULL,
  `item_image` mediumtext,
  `item_status` varchar(255) NOT NULL DEFAULT 'Created'
) ENGINE=InnoDB AUTO_INCREMENT=156 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `leases`
--

CREATE TABLE IF NOT EXISTS `leases` (
`lease_id` int(11) NOT NULL,
  `lease_requser_id` varchar(255) NOT NULL DEFAULT '',
  `lease_item_id` varchar(255) NOT NULL DEFAULT '',
  `lease_user_id` varchar(255) DEFAULT NULL,
  `lease_status` varchar(20) NOT NULL DEFAULT 'Active',
  `lease_expiry_date` varchar(30) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `leaseterms`
--

CREATE TABLE IF NOT EXISTS `leaseterms` (
  `term_name` varchar(255) NOT NULL DEFAULT '',
  `term_desc` varchar(255) DEFAULT NULL,
  `term_duration` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `requests`
--

CREATE TABLE IF NOT EXISTS `requests` (
`request_id` int(11) NOT NULL,
  `request_requser_id` varchar(255) NOT NULL DEFAULT '',
  `request_item_id` varchar(255) NOT NULL DEFAULT '',
  `request_status` varchar(20) NOT NULL DEFAULT 'Active',
  `request_date` varchar(30) DEFAULT NULL
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `store`
--

CREATE TABLE IF NOT EXISTS `store` (
  `store_item_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `user_id` varchar(255) NOT NULL DEFAULT '',
  `user_full_name` varchar(255) DEFAULT NULL,
  `user_mobile` varchar(255) DEFAULT NULL,
  `user_location` varchar(255) DEFAULT NULL,
  `user_auth` varchar(255) DEFAULT NULL,
  `user_activation` varchar(255) NOT NULL,
  `user_status` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `wishlist`
--

CREATE TABLE IF NOT EXISTS `wishlist` (
  `wishlist_item_id` int(11) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `category`
--
ALTER TABLE `category`
 ADD PRIMARY KEY (`cat_name`);

--
-- Indexes for table `config`
--
ALTER TABLE `config`
 ADD PRIMARY KEY (`id`);

--
-- Indexes for table `friends`
--
ALTER TABLE `friends`
 ADD PRIMARY KEY (`friend_id`,`friend_user_id`);

--
-- Indexes for table `items`
--
ALTER TABLE `items`
 ADD PRIMARY KEY (`item_id`);

--
-- Indexes for table `leases`
--
ALTER TABLE `leases`
 ADD PRIMARY KEY (`lease_requser_id`,`lease_item_id`), ADD KEY `lease_id` (`lease_id`);

--
-- Indexes for table `leaseterms`
--
ALTER TABLE `leaseterms`
 ADD PRIMARY KEY (`term_name`);

--
-- Indexes for table `requests`
--
ALTER TABLE `requests`
 ADD PRIMARY KEY (`request_id`,`request_requser_id`,`request_item_id`), ADD KEY `request_id` (`request_id`);

--
-- Indexes for table `store`
--
ALTER TABLE `store`
 ADD PRIMARY KEY (`store_item_id`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
 ADD PRIMARY KEY (`user_id`);

--
-- Indexes for table `wishlist`
--
ALTER TABLE `wishlist`
 ADD PRIMARY KEY (`wishlist_item_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `config`
--
ALTER TABLE `config`
MODIFY `id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=3;
--
-- AUTO_INCREMENT for table `items`
--
ALTER TABLE `items`
MODIFY `item_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=156;
--
-- AUTO_INCREMENT for table `leases`
--
ALTER TABLE `leases`
MODIFY `lease_id` int(11) NOT NULL AUTO_INCREMENT;
--
-- AUTO_INCREMENT for table `requests`
--
ALTER TABLE `requests`
MODIFY `request_id` int(11) NOT NULL AUTO_INCREMENT,AUTO_INCREMENT=28;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;


INSERT INTO `category` (`cat_name`, `cat_desc`, `cat_parent`, `cat_child`) VALUES
('', NULL, NULL, NULL),
('House', 'sofa chair table', 'root', 'null'),
('Kids', NULL, 'root', 'null'),
('Vacation', 'skis scuba camera', 'root', 'null');


INSERT INTO `config` (`option`, `value`, `id`) VALUES
('env', 'dev', 1),
('build', '2000', 2);


INSERT INTO `leaseterms` (`term_name`, `term_desc`, `term_duration`) VALUES
('Annual', 'For a long time', 365),
('Month', 'For a trip', 30),
('Season', 'For a while', 100);

