#!/bin/bash
#
# MibbleBrowser.sh: Runs the Mibble MIB browser
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB browser
$JAVA net.percederberg.mibble.MibbleBrowser $*
