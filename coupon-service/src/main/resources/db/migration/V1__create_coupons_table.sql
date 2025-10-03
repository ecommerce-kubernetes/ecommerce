CREATE TABLE `coupons` (
  `discount_type` tinyint NOT NULL,
  `discount_value` int NOT NULL,
  `max_discount_amount` int NOT NULL,
  `min_purchase_amount` int NOT NULL,
  `reusable` bit(1) NOT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `valid_from` datetime(6) DEFAULT NULL,
  `valid_to` datetime(6) DEFAULT NULL,
  `category` varchar(255) DEFAULT NULL,
  `code` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKeplt0kkm9yf2of2lnx6c1oy9b` (`code`),
  UNIQUE KEY `UK5h861ks1w4coatjjf52xd7w7m` (`name`),
  CONSTRAINT `coupons_chk_1` CHECK ((`discount_type` between 0 and 1))
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb3