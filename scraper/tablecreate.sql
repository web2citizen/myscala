DROP TABLE IF EXISTS `test`.`uskabu`;
CREATE TABLE  `test`.`uskabu` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` varchar(45) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `volume` int(10) unsigned DEFAULT NULL,
  `adj_close` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=133 DEFAULT CHARSET=latin1;