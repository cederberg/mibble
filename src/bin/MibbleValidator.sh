#!/bin/sh
#
# MibbleValidator.sh: Runs the Mibble MIB validator
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB validator
$JAVA_HOME/bin/java net.percederberg.mibble.MibbleValidator $*
