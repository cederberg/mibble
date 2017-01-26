#!/bin/bash
#
# MibbleValidator.sh: Runs the Mibble MIB validator
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB validator
exec $JAVA net.percederberg.mibble.MibbleValidator $*
