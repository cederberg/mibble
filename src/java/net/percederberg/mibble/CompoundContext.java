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
 * As a special exception, the copyright holders of this library give
 * you permission to link this library with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is
 * not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
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
 * @version  2.0
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
     * Returns a named MIB symbol.
     *
     * @param name           the symbol name
     *
     * @return the MIB symbol, or null if not found
     */
    public MibSymbol getSymbol(String name) {
        MibSymbol  symbol;

        symbol = first.getSymbol(name);
        if (symbol == null) {
            symbol = second.getSymbol(name);
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
