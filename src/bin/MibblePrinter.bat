@echo off
REM
REM MibblePrinter.bat: Runs the Mibble MIB printer
REM

REM Set Mibble environment variables
call .\setenv.bat

REM Run Mibble MIB printer
"%JAVA_HOME%\bin\java" net.percederberg.mibble.MibblePrinter %1 %2 %3 %4 %5 %6 %7 %8 %9
