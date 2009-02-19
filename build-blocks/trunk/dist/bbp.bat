@echo off

CALL set-env.bat

"%JAVA_HOME%\bin\java" -Xmx512m -cp %BB_CP% buildBlocks.bootstrap.ProjectBuilder %BB_HOME% %BB_CP% %*
