DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
                          `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
                          `name` varchar(128) NOT NULL,
                          `age` int(2) NOT NULL,
                          `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          PRIMARY KEY (`id`)
);
