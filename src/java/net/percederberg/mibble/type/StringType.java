/*
 * StringType.java
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

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.value.StringValue;

/**
 * A string MIB type.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class StringType implements MibType {

    /**
     * The additional type constraint.
     */
    private Constraint constraint = null;

    /**
     * Creates a new string MIB type. 
     */
    public StringType() {
    }

    /**
     * Creates a new string MIB type.
     * 
     * @param constraint     the additional type constraint 
     */
    public StringType(Constraint constraint) {
        this.constraint = constraint;
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
        if (constraint != null) {
            constraint.initialize(log);
        }
        return this;
    }

    /**
     * Checks if this type has any constraint.
     * 
     * @return true if this type has any constraint, or
     *         false otherwise
     */
    public boolean hasConstraint() {
        return constraint != null;
    }
    
    /**
     * Checks if the specified value is compatible with this type.  A
     * value is compatible if it is a string value that is compatible
     * with the constraints. 
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return isCompatibleType(value)
            && (constraint == null || constraint.isCompatible(value));
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if it is a string value.
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    private boolean isCompatibleType(MibValue value) {
        return value instanceof StringValue;
    }

    /**
     * Returns a string representation of this type.
     * 
     * @return a string representation of this type
     */
    public String toString() {
        if (constraint == null) {
            return "OCTET STRING";
        } else {
            return "OCTET STRING (" + constraint.toString() + ")";
        }
    }
}
