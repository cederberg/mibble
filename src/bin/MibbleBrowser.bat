@echo off
REM
REM MibbleBrowser.bat: Runs the Mibble MIB browser
REM

REM Set Mibble environment variables
call .\setenv.bat

REM Run Mibble MIB browser
%JAVA_HOME%\bin\java -mx200M net.percederberg.mibble.MibbleBrowser
