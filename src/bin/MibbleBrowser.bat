@echo off
REM
REM MibbleBrowser.bat: Runs the Mibble MIB browser
REM

REM Set Mibble environment variables
call .\setenv.bat

REM Run Mibble MIB browser
"%JAVA_HOME%\bin\java" net.percederberg.mibble.MibbleBrowser %1 %2 %3 %4 %5 %6 %7 %8 %9
