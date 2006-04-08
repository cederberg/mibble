/*
 * MibMacroSymbol.java
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
 * Copyright (c) 2005-2006 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A MIB macro symbol. This class holds information relevant to a MIB
 * macro definition, i.e. a defined macro name.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.7
 * @since    2.6
 */
public class MibMacroSymbol extends MibSymbol {

    /**
     * Creates a new macro symbol
     *
     * @param location       the symbol location
     * @param mib            the symbol MIB file
     * @param name           the symbol name
     */
    MibMacroSymbol(FileLocation location, Mib mib, String name) {
        super(location, mib, name);
    }

    /**
     * Initializes the MIB symbol. This will remove all levels of
     * indirection present, such as references to types or values. No
     * information is lost by this operation. This method may modify
     * this object as a side-effect.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public void initialize(MibLoaderLog log) throws MibException {
        // Nothing to be initialized
    }

    /**
     * Clears and prepares this MIB symbol for garbage collection.
     * This method will recursively clear any associated types or
     * values, making sure that no data structures references this
     * symbol.
     */
    void clear() {
        // Nothing to clear
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("MACRO ");
        buffer.append(getName());
        return buffer.toString();
    }
}
