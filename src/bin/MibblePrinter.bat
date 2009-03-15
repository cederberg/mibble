@echo off

::Check for Windows NT compliance
> reg1.txt echo 1234&rem
type reg1.txt | find "rem"
if not errorlevel 1 goto ERRWIN9X
del reg1.txt 2> nul

::Locate the application directory
for %%x in (%0) do set BATCHPATH=%%~dpsx
SET MIBBLE_HOME=%BATCHPATH:~0,-5%
cd %MIBBLE_HOME%

::Find Java and setup classpath
CALL bin\setenv.bat
if errorlevel 1 goto DONE

:: Run application
"%JAVA_HOME%\bin\java.exe" %JAVA_OPTS% net.percederberg.mibble.MibblePrinter %1 %2 %3 %4 %5 %6 %7 %8 %9

::Restore environment vars
set JAVA_HOME=%_JAVA_HOME%
set CLASSPATH=%_CLASSPATH%
goto DONE

:ERRWIN9X
del reg1.txt 2> nul
echo ERROR: Windows 98 or previous is not supported.

:DONE
