/*
 * MibReference.java
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

import java.util.ArrayList;

/**
 * A MIB file reference. This class references a MIB file that has 
 * not yet been loaded.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
class MibReference implements MibContext {

    /**
     * The MIB loader being used.
     */
    private MibLoader loader;

    /**
     * The referenced MIB.
     */
    private Mib mib = null;

    /**
     * The reference location.
     */
    private FileLocation location;

    /**
     * The referenced MIB name.
     */
    private String name;
    
    /**
     * The referenced MIB symbol names.
     */
    private ArrayList symbols;

    /**
     * Creates a new MIB reference.
     * 
     * @param loader         the MIB loader to use
     * @param location       the reference location
     * @param name           the referenced MIB name
     * @param symbols        the referenced MIB symbol names, or
     *                       null to allow references to any symbol
     */
    public MibReference(MibLoader loader, 
                        FileLocation location, 
                        String name,
                        ArrayList symbols) {

        this.loader = loader;
        this.location = location;
        this.name = name;
        this.symbols = symbols;
    }

    /**
     * Initializes the MIB reference. This will resolve all 
     * referenced symbols in the MIB.  This method will be called by 
     * the MIB loader.
     * 
     * @param log            the MIB loader log
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public void initialize(MibLoaderLog log) throws MibException { 
        String  message;

        mib = loader.getMib(name);
        if (mib == null) {
            message = "couldn't find referenced MIB '" + name + "'";
            throw new MibException(location, message);
        }
        if (symbols != null) {
            for (int i = 0; i < symbols.size(); i++) {
                if (mib.getSymbol(symbols.get(i).toString()) == null) {
                    message = "couldn't find imported symbol '" + 
                              symbols.get(i) + "' in MIB '" + name + "'";
                    throw new MibException(location, message);
                }
            }
        }
    }

    /**
     * Returns the MIB name.
     * 
     * @return the MIB name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a named MIB symbol. This method will only return 
     * symbols from the list of referenced symbols, or any symbol in
     * the MIB if no reference list is set. 
     * 
     * @param name           the symbol name
     * 
     * @return the MIB symbol, or null if not found
     */
    public MibSymbol getSymbol(String name) {
        if (mib == null) {
            return null;
        } else if (symbols != null && !symbols.contains(name)) {
            return null;
        } else {
            return mib.getSymbol(name);
        }
    }
    
    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return name;
    }
}
