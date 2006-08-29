@echo off

REM Licensed to the Apache Software Foundation (ASF) under one
REM or more contributor license agreements.  See the NOTICE file
REM distributed with this work for additional information
REM regarding copyright ownership.  The ASF licenses this file
REM to you under the Apache License, Version 2.0 (the
REM "License"); you may not use this file except in compliance
REM with the License.  You may obtain a copy of the License at
REM
REM  http://www.apache.org/licenses/LICENSE-2.0
REM
REM Unless required by applicable law or agreed to in writing,
REM software distributed under the License is distributed on an
REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
REM KIND, either express or implied.  See the License for the
REM specific language governing permissions and limitations
REM under the License.


if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem ----- use the location of this script to infer $FTPD_HOME -------
if NOT "%OS%"=="Windows_NT" set DEFAULT_FTPD_HOME=.
if "%OS%"=="Windows_NT" set DEFAULT_FTPD_HOME=%~dp0
if "%OS%"=="WINNT" set DEFAULT_FTPD_HOME=%~dp0
if "%FTPD_HOME%"=="" set FTPD_HOME=%DEFAULT_FTPD_HOME%

rem ----- Create CLASSPATH --------------------------------------------
set FTPD_CLASSPATH=%CLASSPATH%;%FTPD_HOME%\common\classes;%FTPD_HOME%\target\classes;
cd /d "%FTPD_HOME%\common\lib"
for %%i in ("*.jar") do call "%FTPD_HOME%\appendcp.bat" "%FTPD_HOME%\common\lib\%%i"
cd /d %FTPD_HOME%

rem ----- call java.. ---------------------------------------------------
set MAIN_CLASS=org.apache.ftpserver.commandline.CommandLine
set JAVA_CMD=%JAVA_HOME%\bin\java
"%JAVA_CMD%" -classpath "%FTPD_CLASSPATH%" %MAIN_CLASS% %*
