/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2006-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.type.TypeReference;
import net.percederberg.mibble.value.ValueReference;

/**
 * A MIB type context. This class attempts to resolve all symbols as
 * defined enumeration values in the contained MIB type.
 *
 * @author   Per Cederberg
 * @version  2.8
 * @since    2.8
 */
class MibTypeContext implements MibContext {

    /**
     * The MIB symbol, value or type.
     */
    private Object context;

    /**
     * Creates a new MIB type context.
     *
     * @param context        the MIB symbol, value or type
     */
    public MibTypeContext(Object context) {
        this.context = context;
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
        MibContext ctx = null;

        if (context instanceof ValueReference) {
            context = ((ValueReference) context).getSymbol();
        }
        if (context instanceof MibTypeSymbol) {
            context = ((MibTypeSymbol) context).getType();
        }
        if (context instanceof MibValueSymbol) {
            context = ((MibValueSymbol) context).getType();
        }
        if (context instanceof SnmpObjectType) {
            context = ((SnmpObjectType) context).getSyntax();
        }
        if (context instanceof TypeReference) {
            context = ((TypeReference) context).getSymbol();
            return findSymbol(name, expanded);
        }
        if (context instanceof MibContext) {
            ctx = (MibContext) context;
        }
        return (ctx == null) ? null : ctx.findSymbol(name, expanded);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return "<type context>";
    }
}
