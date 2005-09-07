CREATE TABLE FTP_USER (      
   uid VARCHAR(64) NOT NULL PRIMARY KEY,       
   userpassword VARCHAR(64),      
   homedirectory VARCHAR(128) NOT NULL,             
   enableflag VARCHAR(8) NOT NULL,    
   writepermission VARCHAR(8) NOT NULL,       
   idletime INT NOT NULL,             
   uploadrate INT NOT NULL,             
   downloadrate INT NOT NULL
)
