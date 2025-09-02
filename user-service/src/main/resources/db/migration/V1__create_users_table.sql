CREATE TABLE `users` (
  `birth_date` date NOT NULL,
  `cache` int NOT NULL,
  `phone_verified` bit(1) NOT NULL,
  `point` int NOT NULL,
  `created_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `phone_number` varchar(20) NOT NULL,
  `email` varchar(50) NOT NULL,
  `name` varchar(50) NOT NULL,
  `encrypted_pwd` varchar(255) NOT NULL,
  `gender` enum('FEMALE','MALE') NOT NULL,
  `role` enum('ROLE_ADMIN','ROLE_USER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK6dotkott2kjsp8vw4d0m25fb7` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb3