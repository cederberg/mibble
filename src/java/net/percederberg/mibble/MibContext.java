/*
 * MibContext.java
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
 * A MIB symbol context. This interface is implemented by all objects
 * that contains multiple named references to MIB symbols.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  1.0
 */
public interface MibContext {

    /**
     * Returns a named MIB symbol.
     *
     * @param name           the symbol name
     *
     * @return the MIB symbol, or null if not found
     */
    MibSymbol getSymbol(String name);
}
