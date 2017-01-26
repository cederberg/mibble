/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2005-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;

/**
 * An SNMP index object. This declaration is used inside an object
 * type index declaration. An index contains either a type or a
 * value. Indices based on values may be implied.
 *
 * @see SnmpObjectType
 *
 * @author   Per Cederberg
 * @version  2.8
 * @since    2.6
 */
public class SnmpIndex {

    /**
     * The implied flag.
     */
    private boolean implied;

    /**
     * The index value, or null.
     */
    private MibValue value;

    /**
     * The index type, or null.
     */
    private MibType type;

    /**
     * Creates a new SNMP index. Exactly one of the value or type
     * arguments are supposed to be non-null.
     *
     * @param implied        the implied flag
     * @param value          the index value, or null
     * @param type           the index type, or null
     */
    public SnmpIndex(boolean implied, MibValue value, MibType type) {
        this.implied = implied;
        this.value = value;
        this.type = type;
    }

    /**
     * Initializes the object. This will remove all levels of
     * indirection present, such as references to other types and
     * values. No information is lost by this operation. This method
     * may modify this object as a side-effect, and will be called by
     * the MIB loader.
     *
     * @param symbol         the MIB symbol containing this object
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    void initialize(MibSymbol symbol, MibLoaderLog log) throws MibException {
        if (value != null) {
            value = value.initialize(log, null);
        }
        if (type != null) {
            type = type.initialize(symbol, log);
        }
    }

    /**
     * Checks if this index is an implied value. If this is true, the
     * index also represents a value index.
     *
     * @return true if the index is an implied value, or
     *         false otherwise
     */
    public boolean isImplied() {
        return implied;
    }

    /**
     * Returns the index value if present.
     *
     * @return the index value, or null if not applicable
     *
     * @see #getType()
     */
    public MibValue getValue() {
        return value;
    }

    /**
     * Returns the index type if present.
     *
     * @return the index type, or null if not applicable
     *
     * @see #getValue()
     */
    public MibType getType() {
        return type;
    }

    /**
     * Returns the index type or value.
     *
     * @return the index type or value
     *
     * @see net.percederberg.mibble.MibValue
     * @see net.percederberg.mibble.MibType
     */
    public Object getTypeOrValue() {
        if (value != null) {
            return value;
        } else {
            return type;
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (implied) {
            buffer.append("IMPLIED ");
        }
        buffer.append(getTypeOrValue());
        return buffer.toString();
    }
}
