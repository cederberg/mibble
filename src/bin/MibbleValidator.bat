@echo off
REM
REM MibbleValidator.bat: Runs the Mibble MIB validator
REM

REM Set Mibble environment variables
call .\setenv.bat

REM Run Mibble MIB validator
%JAVA_HOME%\bin\java net.percederberg.mibble.MibbleValidator
