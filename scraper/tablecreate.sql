DROP TABLE IF EXISTS `us`.`stock_daily`;
CREATE TABLE  `us`.`stock_daily` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `ticker` varchar(45) DEFAULT NULL,
  `date` datetime DEFAULT NULL,
  `open` double DEFAULT NULL,
  `high` double DEFAULT NULL,
  `low` double DEFAULT NULL,
  `close` double DEFAULT NULL,
  `volume` int(10) unsigned DEFAULT NULL,
  `adj_open` double DEFAULT NULL,
  `adj_high` double DEFAULT NULL,
  `adj_low` double DEFAULT NULL,
  `adj_close` double DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=UTF-8;
CREATE Index ticker_date on stock_daily(ticker,date);
CREATE Index date_ticker on stock_daily(date,ticker);