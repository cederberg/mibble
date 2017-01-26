#!/bin/bash
#
# MibblePrinter.sh: Runs the Mibble MIB printer
#

# Set Mibble environment variables (silently)
. `dirname "$0"`/setenv.sh >/dev/null

# Run Mibble MIB printer
exec $JAVA net.percederberg.mibble.MibblePrinter $*
