@echo off
REM
REM MibblePrinter.bat: Runs the Mibble MIB printer
REM

REM Set Mibble environment variables
call .\setenv.bat

REM Run Mibble MIB printer
%JAVA_HOME%\bin\java net.percederberg.mibble.MibblePrinter
