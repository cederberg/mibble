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

# Check JAVA_HOME variable
if [ -z "$JAVA_HOME" ]
then
    echo "Error: JAVA_HOME environment variable not set"
    exit 1
fi

# Set CLASSPATH variable
if [ ! -x "$MIBBLE_HOME/lib" ]
then
    echo "Error: $MIBBLE_HOME/lib does not exist"
    exit 1
fi
MIBBLE_PARSER_JAR=$MIBBLE_HOME/lib/@NAME@-parser-@VERSION@.jar
MIBBLE_MIBS_JAR=$MIBBLE_HOME/lib/@NAME@-mibs-@VERSION@.jar
GRAMMATICA_JAR=$MIBBLE_HOME/lib/grammatica-bin-1.4.jar
SNMP_JAR=$MIBBLE_HOME/lib/snmp4_13.jar
CLASSPATH=$MIBBLE_PARSER_JAR:$MIBBLE_MIBS_JAR:$GRAMMATICA_JAR:$SNMP_JAR
export CLASSPATH

# Display variables
echo "Using environment variables:"
echo "  MIBBLE_HOME = $MIBBLE_HOME"
echo "  JAVA_HOME   = $JAVA_HOME"
echo "  CLASSPATH   = $CLASSPATH"
echo
