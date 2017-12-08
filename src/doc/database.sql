
CREATE TABLE `sequence_generator_table` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(30) COLLATE utf8mb4_bin DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;


CREATE TABLE `sequence_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `sequence_id` bigint(20) DEFAULT NULL,
  `ip` varchar(50) COLLATE utf8mb4_bin DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `sequence_id` (`sequence_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;