#!/bin/sh
#
# mibvalidator.sh: Runs the Mibble MIB validator
#

# Set Mibble environment variables
. `dirname "$0"`/setenv.sh

# Run Mibble MIB validator
java net.percederberg.mibble.MibbleValidator
