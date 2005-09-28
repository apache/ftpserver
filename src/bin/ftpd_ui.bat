@echo off
rem Copyright 2002-2004 The Apache Software Foundation or its licensors,
rem as applicable.
rem
rem Licensed under the Apache License, Version 2.0 (the "License");
rem you may not use this file except in compliance with the License.
rem You may obtain a copy of the License at
rem
rem     http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing, software
rem distributed under the License is distributed on an "AS IS" BASIS,
rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem See the License for the specific language governing permissions and
rem limitations under the License.

if "%OS%"=="Windows_NT" @setlocal
if "%OS%"=="WINNT" @setlocal

rem ----- use the location of this script to infer $FTPD_HOME -------
if NOT "%OS%"=="Windows_NT" set DEFAULT_FTPD_HOME=.
if "%OS%"=="Windows_NT" set DEFAULT_FTPD_HOME=%~dp0
if "%OS%"=="WINNT" set DEFAULT_FTPD_HOME=%~dp0
if "%FTPD_HOME%"=="" set FTPD_HOME=%DEFAULT_FTPD_HOME%

rem ----- Save and set CLASSPATH --------------------------------------------
set OLD_CLASSPATH=%CLASSPATH%
set CLASSPATH=%CLASSPATH%;
cd /d "%FTPD_HOME%\lib"
for %%i in ("*.jar") do call "%FTPD_HOME%\appendcp.bat" "%FTPD_HOME%\lib\%%i"
cd /d "%FTPD_HOME%"

rem ----- call java.. ---------------------------------------------------
set MAIN_CLASS=org.apache.ftpserver.gui.ServerFrame
set JAVA_CMD=%JAVA_HOME%\bin\java
"%JAVA_CMD%" %MAIN_CLASS% %*

rem ---- Restore old CLASSPATH
set CLASSPATH=%OLD_CLASSPATH%
