/*
 * RealType.java
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

import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeTag;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.value.NumberValue;

/**
 * A real MIB type.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class RealType extends MibType {

    /**
     * Creates a new real MIB type. 
     */
    public RealType() {
        this(true);
    }

    /**
     * Creates a new real MIB type.
     * 
     * @param primitive      the primitive type flag
     */
    private RealType(boolean primitive) {
        super("REAL", primitive);
        setTag(true, MibTypeTag.REAL);
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
     */
    public MibType initialize(MibLoaderLog log) {
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
        RealType  type = new RealType(false);

        type.setTag(true, getTag());
        return type;
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if and only if it is an numeric value
     * representing positive or negative infinity.
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        NumberValue  number;
        
        if (value instanceof NumberValue) {
            number = (NumberValue) value;
            if (number.toObject() instanceof Float) {
                return true; 
            }
        }
        return false;
    }
}
