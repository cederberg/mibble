#!/bin/sh
#
# mibprinter.sh: Runs the Mibble MIB printer
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB printer
java net.percederberg.mibble.MibblePrinter
