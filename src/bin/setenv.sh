#!/bin/bash
#
# setenv.sh: Sets the Mibble environment variables
#

# Set MIBBLE_HOME variable
if [[ -z "$MIBBLE_HOME" ]] ; then
    DIR=`dirname "$0"`
    if [[ "$DIR" = "." ]] ; then
        MIBBLE_HOME=".."
    else
        MIBBLE_HOME=`dirname "$DIR"`
    fi
    export MIBBLE_HOME
fi

# Outputs the Java version for the specified directory
function java_version {
    DIR=$1
    "$DIR/bin/java" -version 2> tmp.ver 1> /dev/null
    VERSION=`cat tmp.ver | grep "java version" | awk '{ print substr($3, 2, length($3)-2); }'`
    rm tmp.ver
    echo $VERSION
}

# Verifies that a directory can serve as JAVA_HOME
function is_java_dir {
    DIR=$1
    if [[ ! -x "$DIR/bin/java" ]] ; then
        return 1
    fi
    if [[ (! -d "$DIR/jre") && (! -d "$DIR/lib") ]] ; then
        return 2 
    fi
    VERSION=`java_version "$DIR" | awk '{ print substr($1, 1, 3); }' | sed -e 's;\.;0;g'`
    if [[ "$VERSION" == "" ]] ; then
        return 3
    elif [[ "$VERSION" -le "104" ]] ; then
        return 4
    fi
    return 0
}

# Setup JAVA_HOME variable
if is_java_dir $JAVA_HOME ; then
    :
else
    JAVA_HOME=
    for JAVA_EXE in `locate bin/java | grep java$ | xargs echo` ; do
        JAVA_HOME=`dirname "$JAVA_EXE"`
        JAVA_HOME=`dirname "$JAVA_HOME"`
        if is_java_dir $JAVA_HOME ; then
            break
        else
            JAVA_HOME=
        fi
    done
fi
if [[ "$JAVA_HOME" == "" ]] ; then
    echo "ERROR: Failed to find java version 1.4 or higher." >&2
    exit 1
fi
export JAVA_HOME

# Set CLASSPATH variable
if [[ ! -x "$MIBBLE_HOME/lib" ]] ; then
    echo "Error: $MIBBLE_HOME/lib does not exist"
    exit 1
fi
MIBBLE_PARSER_JAR=$MIBBLE_HOME/lib/@NAME@-parser-@VERSION@.jar
MIBBLE_MIBS_JAR=$MIBBLE_HOME/lib/@NAME@-mibs-@VERSION@.jar
SNMP_JAR=$MIBBLE_HOME/lib/snmp6_0.jar
CLASSPATH=$MIBBLE_PARSER_JAR:$MIBBLE_MIBS_JAR:$SNMP_JAR
export CLASSPATH

# Display variables
echo "Using environment variables:"
echo "  MIBBLE_HOME = $MIBBLE_HOME"
echo "  JAVA_HOME   = $JAVA_HOME"
echo "  CLASSPATH   = $CLASSPATH"
echo
