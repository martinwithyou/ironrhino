DROP DATABASE IF EXISTS ironrhino;
CREATE DATABASE "ironrhino" WITH TEMPLATE template0 OWNER "postgres" ENCODING 'UTF-8' LC_COLLATE = 'zh_CN.UTF-8' LC_CTYPE = 'zh_CN.UTF-8';
\c ironrhino;
CREATE TABLE  "user"(
  id varchar(32) NOT NULL,
  username varchar(255) NOT NULL,
  password varchar(255) NOT NULL,
  name varchar(255) DEFAULT NULL,
  email varchar(255) DEFAULT NULL,
  phone varchar(255) DEFAULT NULL,
  roles varchar(500) DEFAULT NULL,
  attributes varchar(500) DEFAULT NULL,
  enabled boolean DEFAULT NULL,
  createDate timestamp DEFAULT NULL,
  modifyDate timestamp DEFAULT NULL,
  createUser varchar(32) DEFAULT NULL,
  modifyUser varchar(32) DEFAULT NULL,
  PRIMARY KEY (id)
) ;
insert into "user"(id,username,password,name,enabled,createDate,roles) values ('3PrH25a6pGxAqhPNX8x1EM','admin','fb6603c6be5733bef5d208a1d6721b84','Admin',true,'2010-01-01 00:00:00','ROLE_ADMINISTRATOR');
