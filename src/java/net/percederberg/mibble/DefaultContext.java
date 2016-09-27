/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.util.HashMap;

import net.percederberg.mibble.type.ObjectIdentifierType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A default MIB context.
 *
 * @author   Per Cederberg
 * @version  2.8
 * @since    2.0
 */
class DefaultContext implements MibContext {

    /**
     * The root "ccitt" symbol name.
     */
    public static final String CCITT = "ccitt";

    /**
     * The root "iso" symbol name.
     */
    public static final String ISO = "iso";

    /**
     * The root "joint-iso-ccitt" symbol name.
     */
    public static final String JOINT_ISO_CCITT = "joint-iso-ccitt";

    /**
     * The map of default symbols.
     */
    private HashMap symbols = new HashMap();

    /**
     * Creates a new default context.
     */
    DefaultContext() {
        initialize();
    }

    /**
     * Initializes this context by creating all default symbols.
     */
    private void initialize() {
        MibSymbol              symbol;
        ObjectIdentifierValue  oid;

        // Add the ccitt symbol
        oid = new ObjectIdentifierValue(CCITT, 0);
        symbol = new MibValueSymbol(new FileLocation(null, -1, -1),
                                    null,
                                    CCITT,
                                    new ObjectIdentifierType(),
                                    oid);
        oid.setSymbol((MibValueSymbol) symbol);
        symbols.put(CCITT, symbol);

        // Add the iso symbol
        oid = new ObjectIdentifierValue(ISO, 1);
        symbol = new MibValueSymbol(new FileLocation(null, -1, -1),
                                    null,
                                    ISO,
                                    new ObjectIdentifierType(),
                                    oid);
        oid.setSymbol((MibValueSymbol) symbol);
        symbols.put(ISO, symbol);

        // Add the joint-iso-ccitt symbol
        oid = new ObjectIdentifierValue(JOINT_ISO_CCITT, 2);
        symbol = new MibValueSymbol(new FileLocation(null, -1, -1),
                                    null,
                                    JOINT_ISO_CCITT,
                                    new ObjectIdentifierType(),
                                    oid);
        oid.setSymbol((MibValueSymbol) symbol);
        symbols.put(JOINT_ISO_CCITT, symbol);
    }

    /**
     * Searches for a named MIB symbol. This method may search outside
     * the normal (or strict) scope, thereby allowing a form of
     * relaxed search. Note that the results from the normal and
     * expanded search may not be identical, due to the context
     * chaining and the same symbol name appearing in various
     * contexts.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param name           the symbol name
     * @param expanded       the expanded scope flag
     *
     * @return the MIB symbol, or null if not found
     *
     * @since 2.4
     */
    public MibSymbol findSymbol(String name, boolean expanded) {
        return (MibSymbol) symbols.get(name);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return "<defaults>";
    }
}
