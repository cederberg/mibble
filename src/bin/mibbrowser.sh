#!/bin/sh
#
# mibbrowser.sh: Runs the Mibble MIB browser
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB browser
java -mx200M net.percederberg.mibble.MibbleBrowser
