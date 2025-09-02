CREATE TABLE `user_coupons` (
  `used` bit(1) NOT NULL,
  `coupon_id` bigint DEFAULT NULL,
  `expires_at` datetime(6) DEFAULT NULL,
  `id` bigint NOT NULL AUTO_INCREMENT,
  `issued_at` datetime(6) DEFAULT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  `phone_number` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9oi3p5xyfe4j32xs54nn7mi20` (`coupon_id`),
  CONSTRAINT `FK9oi3p5xyfe4j32xs54nn7mi20` FOREIGN KEY (`coupon_id`) REFERENCES `coupons` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb3