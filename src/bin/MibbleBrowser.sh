#!/bin/sh
#
# MibbleBrowser.sh: Runs the Mibble MIB browser
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB browser
$JAVA_HOME/bin/java -Xmx200M net.percederberg.mibble.MibbleBrowser
