#!/bin/sh
#
# MibblePrinter.sh: Runs the Mibble MIB printer
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB printer
$JAVA_HOME/bin/java net.percederberg.mibble.MibblePrinter $*
