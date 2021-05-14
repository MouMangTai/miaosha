# Host: 127.0.0.1  (Version: 5.5.15)
# Date: 2021-05-14 10:24:10
# Generator: MySQL-Front 5.3  (Build 4.269)

/*!40101 SET NAMES utf8 */;

#
# Structure for table "goods"
#

CREATE TABLE `goods` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(100) NOT NULL,
  `info` text,
  `price` decimal(10,2) DEFAULT NULL,
  `save` int(11) NOT NULL,
  `begin_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  `end_time` timestamp NOT NULL DEFAULT '0000-00-00 00:00:00',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;

#
# Data for table "goods"
#

INSERT INTO `goods` VALUES (1,'小天鹅滚筒洗衣机','性价比高',999.98,1000,'2021-05-14 08:00:00','2021-05-10 18:00:00'),(2,'格力电频空调','一点只用一度电',2999.98,20,'2021-05-14 10:00:00','2021-05-10 16:00:00'),(3,'苹果手机','性价比高',5999.98,888,'2021-05-14 10:00:00','2021-05-10 20:00:00');

#
# Structure for table "orders"
#

CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `oid` varchar(40) NOT NULL DEFAULT '',
  `gid` int(11) NOT NULL,
  `uid` int(11) NOT NULL,
  `gnumber` tinyint(4) NOT NULL DEFAULT '1',
  `all_price` decimal(10,2) NOT NULL,
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint(4) DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `oid` (`oid`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8;

#
# Data for table "orders"
#

INSERT INTO `orders` VALUES (22,'e8e0beae-aec2-4670-8cf9-544610ca5508',2,1,1,998.00,'2021-05-14 10:00:14',0),(23,'b1785414-7e31-4219-8b91-bf07a785f0c2',2,1,1,998.00,'2021-05-14 10:00:14',0),(24,'7acccc4e-e67f-48d6-a10c-b186bcc4d8b9',2,1,1,998.00,'2021-05-14 10:00:14',0),(25,'364f5b00-75cf-4dc9-849e-25a0db8dca7f',2,1,1,998.00,'2021-05-14 10:00:15',0);
