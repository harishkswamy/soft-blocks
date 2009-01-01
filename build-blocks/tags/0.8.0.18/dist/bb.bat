@echo off

if not "%JAVA_HOME%"=="" goto build
echo Error: JAVA_HOME is not defined.
goto end

:build
set BB_HOME=%~dp0
set BB_LIB=%BB_HOME%lib
set BB_CP="%BB_LIB%\build-blocks-0.8.0.jar"

dir /b "%BB_LIB%\ext\*.jar" > %TEMP%\bb-lib-ext.tmp
FOR /F %%I IN (%TEMP%\bb-lib-ext.tmp) DO CALL "%BB_HOME%addpath.bat" "%BB_LIB%\ext\%%I"

"%JAVA_HOME%\bin\java" -Xmx512m -cp %BB_CP% buildBlocks.bootstrap.Builder %BB_HOME% %BB_CP% %*

:end
