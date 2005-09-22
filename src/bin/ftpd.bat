@echo off
REM CUSTOM_CLASSPATH=
%JAVA_HOME%\bin\java -classpath .\lib\ftpserver.jar;%CUSTOM_CLASSPATH% org.apache.ftpserver.FtpServer %*
