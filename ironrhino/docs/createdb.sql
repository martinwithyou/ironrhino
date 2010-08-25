DROP DATABASE IF EXISTS ironrhino;
CREATE DATABASE ironrhino DEFAULT CHARSET utf8 COLLATE utf8_general_ci;
use ironrhino;
set names gbk;
CREATE TABLE  user(
  id varchar(32) NOT NULL,
  username varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  name varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  phone varchar(255) DEFAULT NULL,
  roles varchar(500) DEFAULT NULL,
  attributes varchar(500) DEFAULT NULL,
  enabled bit(1) DEFAULT NULL,
  createDate datetime DEFAULT NULL,
  modifyDate datetime DEFAULT NULL,
  createUser varchar(32) DEFAULT NULL,
  modifyUser varchar(32) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY username (username),
  UNIQUE KEY email (email),
  KEY FK36EBCB48643179 (createUser),
  KEY FK36EBCB3EFBFA37 (modifyUser)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;
insert into user(id,username,password,name,enabled,createDate,roles) values ('3PrH25a6pGxAqhPNX8x1EM','admin','fb6603c6be5733bef5d208a1d6721b84','管理员',1,'2010-01-01 00:00:00','ROLE_ADMINISTRATOR');
