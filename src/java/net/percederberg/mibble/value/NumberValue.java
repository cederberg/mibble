/*
 * NumberValue.java
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

import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibValue;

/**
 * A numeric MIB value.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class NumberValue implements MibValue {

    /**
     * The number value.
     */
    private Number value;
    
    /**
     * Creates a new number value.
     * 
     * @param value          the number value
     */
    public NumberValue(Number value) {
        this.value = value;
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
     */
    public MibValue initialize(MibLoaderLog log) {
        return this; 
    }

    /**
     * Checks if this object equals another object. This method will
     * compare the string representations for equality.
     * 
     * @param obj            the object to compare with
     * 
     * @return true if the objects are equal, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    /**
     * Returns a hash code for this object.
     * 
     * @return a hash code for this object
     */
    public int hashCode() {
        return value.hashCode();
    }

    /**
     * Returns a Java Number representation of this value.
     * 
     * @return a Java Number representation of this value
     */
    public Object toObject() {
        return value;
    }

    /**
     * Returns a string representation of this value.
     * 
     * @return a string representation of this value
     */
    public String toString() {
        return value.toString();
    }
}
