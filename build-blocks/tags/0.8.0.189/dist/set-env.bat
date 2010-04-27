@echo off

if not "%JAVA_HOME%"=="" goto build
echo Error: JAVA_HOME is not defined.
exit /b -1

:build
set BB_HOME=%~dp0..\
set BB_BIN="%BB_HOME%bin"
set BB_LIB="%BB_HOME%lib"
set BB_CP="%BB_LIB%"

DIR /b "%BB_LIB%\*.jar" > %TEMP%\bb-lib.tmp
FOR /F %%I IN (%TEMP%\bb-lib.tmp) DO CALL "%BB_BIN%\add-path.bat" "%BB_LIB%\%%I"
