/*
 * MibTypeSymbol.java
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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A MIB type symbol. This class holds information relevant to a MIB 
 * type assignment, i.e. a defined type name.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class MibTypeSymbol extends MibSymbol {

    /**
     * The symbol type.
     */
    private MibType type;

    /**
     * Creates a new type symbol
     * 
     * @param location       the symbol location
     * @param mib            the symbol MIB file
     * @param name           the symbol name
     * @param type           the symbol type
     * 
     * @since 2.2
     */
    MibTypeSymbol(FileLocation location,
                  Mib mib, 
                  String name, 
                  MibType type) {

        super(location, mib, name);
        this.type = type;
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
        if (type != null) {
            type = type.initialize(log);
        }
    }

    /**
     * Returns the symbol type.
     * 
     * @return the symbol type
     */
    public MibType getType() {
        return type;
    }
    
    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("TYPE ");
        buffer.append(getName());
        buffer.append(" ::= ");
        buffer.append(getType());
        return buffer.toString();
    }
}
