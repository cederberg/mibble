#!/bin/sh
#
# setenv.sh: Sets the Mibble environment variables
#

# Set MIBBLE_HOME variable
if [ -z "$MIBBLE_HOME" ]
then
    DIR=`dirname "$0"`
    if [ "$DIR" = "." ]
    then
        MIBBLE_HOME=".."
    else
        MIBBLE_HOME=`dirname "$DIR"`
    fi
    export MIBBLE_HOME
fi

# Adjust CLASSPATH variable
if [ ! -x "$MIBBLE_HOME/lib" ]
then
    echo "Error: $MIBBLE_HOME/lib does not exist"
    exit 1
fi
MIBBLE_JAR=$MIBBLE_HOME/lib/mibble-@VERSION@.jar
GRAMMATICA_JAR=$MIBBLE_HOME/lib/grammatica-1.4.jar
SNMP_JAR=$MIBBLE_HOME/lib/snmp4_13.jar
if [ -z "$CLASSPATH" ]
then
    CLASSPATH=$MIBBLE_JAR:$GRAMMATICA_JAR:$SNMP_JAR
else
    CLASSPATH=$CLASSPATH:$MIBBLE_JAR:$GRAMMATICA_JAR:$SNMP_JAR
fi
export CLASSPATH

# Display variables
echo "Using environment variables:"
echo "  MIBBLE_HOME = $MIBBLE_HOME"
echo "  CLASSPATH   = $CLASSPATH"
