/*
 * SequenceType.java
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

package net.percederberg.mibble.type;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;

/**
 * A sequence MIB type. In some other languages this is known as a 
 * struct.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class SequenceType extends MibType {

    /**
     * The sequence elements.
     */
    private ArrayList elements;

    /**
     * Creates a new sequence MIB type.
     * 
     * @param elements       the list of element types
     */
    public SequenceType(ArrayList elements) {
        super("SEQUENCE", false);
        this.elements = elements;
    }
    
    /**
     * Initializes the MIB type. This will remove all levels of
     * indirection present, such as references to other types, and 
     * returns the basic type. No type information is lost by this 
     * operation. This method may modify this object as a 
     * side-effect, and will be called by the MIB loader.
     * 
     * @param log            the MIB loader log
     * 
     * @return the basic MIB type
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public MibType initialize(MibLoaderLog log) throws MibException {
        ElementType  elem;

        for (int i = 0; i < elements.size(); i++) {
            elem = (ElementType) elements.get(i);
            elem.initialize(log);
        }
        return this;
    }

    /**
     * Creates a type reference to this type. The type reference is
     * normally an identical type, but with the primitive flag set to 
     * false. Only certain types support being referenced, and the
     * default implementation of this method throws an exception. 
     * 
     * @return the MIB type reference
     * 
     * @since 2.2
     */
    public MibType createReference() {
        return new SequenceType(elements);
    }

    /**
     * Checks if the specified value is compatible with this type. No
     * values are considered compatible with this type, and this 
     * method therefore always returns false.
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return false;
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        return super.toString() + " " + elements.toString();
    }
}
