/*
 * ValueReference.java
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

package net.percederberg.mibble.value;

import net.percederberg.mibble.FileLocation;
import net.percederberg.mibble.MibContext;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;

/**
 * A reference to a value symbol.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class ValueReference implements MibValue {

    /**
     * The reference location.
     */
    private FileLocation location;

    /**
     * The reference context.
     */
    private MibContext context;

    /**
     * The referenced name.
     */
    private String name;

    /**
     * Creates a new value reference.
     *
     * @param location       the reference location
     * @param context        the reference context 
     * @param name           the reference name
     */
    public ValueReference(FileLocation location, 
                          MibContext context,
                          String name) {

        this.location = location;
        this.context = context;
        this.name = name;
    }

    /**
     * Initializes the MIB value. This will remove all levels of
     * indirection present, such as references to other values, and 
     * returns the basic value. No value information is lost by this 
     * operation. This method may modify this object as a 
     * side-effect, and will be called by the MIB loader.
     * 
     * @param log            the MIB loader log
     * 
     * @return the basic MIB value
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public MibValue initialize(MibLoaderLog log) throws MibException { 
        MibSymbol  symbol;
        String     message;

        symbol = context.getSymbol(name);
        if (symbol instanceof MibValueSymbol) {
            return ((MibValueSymbol) symbol).getValue().initialize(log);
        } else if (symbol == null) {
            message = "undefined symbol '" + name + "'";
            throw new MibException(location, message);
        } else {
            message = "referenced symbol '" + name + "' is not a value";
            throw new MibException(location, message);
        }
    }

    /**
     * Returns the reference location.
     * 
     * @return the reference location
     */
    public FileLocation getLocation() {
        return location;
    }

    /**
     * Returns a Java object representation of this value. This 
     * method will always return null.
     * 
     * @return a Java object representation of this value
     */
    public Object toObject() {
        return null;
    }
    
    /**
     * Returns a string representation of this value.
     * 
     * @return a string representation of this value
     */
    public String toString() {
        return "ReferenceToValue(" + name + ")";
    }
}
