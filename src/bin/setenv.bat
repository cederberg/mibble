@echo off

::Backup environment vars
set _JAVA_HOME=%JAVA_HOME%
set _CLASSPATH=%CLASSPATH%

::Check for existing JAVA_HOME
if exist "%JAVA_HOME%\bin\java.exe" goto FIXCP

::Find the current (most recent) Java version
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment" > reg1.txt
type reg1.txt | find "CurrentVersion" > reg2.txt
if errorlevel 1 goto ERROR
for /f "tokens=3" %%x in (reg2.txt) do set JAVA_VERSION=%%x
if errorlevel 1 goto ERROR
del reg1.txt reg2.txt 2> nul

::Find the Java home directory
reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\%JAVA_VERSION%" > reg1.txt
type reg1.txt | find "JavaHome" > reg2.txt
if errorlevel 1 goto ERROR
for /f "tokens=3,4" %%x in (reg2.txt) do set JAVA_HOME=%%x %%y
if errorlevel 1 goto ERROR
del reg1.txt reg2.txt 2> nul

::Check for existence of java.exe
if not exist "%JAVA_HOME%\bin\java.exe" goto ERROR

::Setup the classpath variable
:FIXCP
del reg1.txt 2> nul
set CLASSPATH=lib\@NAME@-parser-@VERSION@.jar;lib\@NAME@-mibs-@VERSION@.jar;lib\snmp6_0.jar
goto DONE

:ERROR
del reg1.txt reg2.txt 2> nul
echo ERROR: Failed to find a Java 1.4 compatible installation.
exit /b 1

:DONE
echo Using environment variables:
echo   MIBBLE_HOME = %MIBBLE_HOME%
echo   JAVA_HOME   = %JAVA_HOME%
echo   CLASSPATH   = %CLASSPATH%
echo.
echo If an "out of environment space" message is listed above,
echo please consult Microsoft KB article 230205 to correct that.
echo.
