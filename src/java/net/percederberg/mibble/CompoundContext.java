/*
 * CompoundContext.java
 *
 * This work is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version.
 *
 * This work is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA
 *
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A compound MIB context. This class attempts to resolve all symbols
 * with either one of two MIB contexts, one of which will have
 * priority.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.4
 * @since    2.0
 */
class CompoundContext implements MibContext {

    /**
     * The first MIB context.
     */
    private MibContext first;

    /**
     * The second MIB context.
     */
    private MibContext second;

    /**
     * Creates a new compound MIB context.
     *
     * @param first          the primary MIB context
     * @param second         the secondary MIB context
     */
    public CompoundContext(MibContext first, MibContext second) {
        this.first = first;
        this.second = second;
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
        MibSymbol  symbol;

        symbol = first.findSymbol(name, expanded);
        if (symbol == null) {
            symbol = second.findSymbol(name, expanded);
        }
        return symbol;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return first.toString() + ", " + second.toString();
    }
}
