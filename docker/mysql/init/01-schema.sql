-- Application  database
create database batchdb;
CREATE USER 'batchuser'@'%' IDENTIFIED BY 'batchuser';
GRANT ALL PRIVILEGES ON batchdb.* TO 'batchuser'@'%';


