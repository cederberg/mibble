/*
 * SizeConstraint.java
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

package net.percederberg.mibble.type;

import java.util.ArrayList;

import net.percederberg.mibble.FileLocation;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.value.NumberValue;
import net.percederberg.mibble.value.StringValue;

/**
 * A MIB type size constraint.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.6
 * @since    2.0
 */
public class SizeConstraint implements Constraint {

    /**
     * The constraint location. This value is reset to null once the
     * constraint has been initialized. 
     */
    private FileLocation location;

    /**
     * The constrained size values.
     */
    private Constraint values;

    /**
     * Creates a new size constraint.
     *
     * @param location       the constraint location
     * @param values         the constrained size values
     */
    public SizeConstraint(FileLocation location, Constraint values) {
        this.location = location;
        this.values = values;
    }

    /**
     * Initializes the constraint. This will remove all levels of
     * indirection present, such as references to types or values. No
     * constraint information is lost by this operation. This method
     * may modify this object as a side-effect, and will be called by
     * the MIB loader.
     *
     * @param type           the type to constrain
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public void initialize(MibType type, MibLoaderLog log)
        throws MibException {

        String  message;

        values.initialize(new IntegerType(), log);
        if (location != null && !isCompatible(type)) {
            message = "Size constraint not compatible with this type";
            log.addWarning(location, message);
        }
        location = null;
    }

    /**
     * Checks if the specified type is compatible with this
     * constraint.
     *
     * @param type            the type to check
     *
     * @return true if the type is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibType type) {
        return type instanceof SequenceOfType
            || type instanceof StringType;
    }

    /**
     * Checks if the specified value is compatible with this
     * constraint. Only octet string values can be compatible with a
     * size constraint, and only if the string length is compatible
     * with the value range in the size constraint.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        Integer  size;

        if (value instanceof StringValue) {
            size = new Integer(value.toString().length());
            return values.isCompatible(new NumberValue(size));
        }
        return false;
    }

    /**
     * Returns a list of the value constraints on the size.
     *
     * @return a list of the value constraints
     */
    public ArrayList getValues() {
        ArrayList  list;

        if (values instanceof CompoundConstraint) {
            return ((CompoundConstraint) values).getConstraintList();
        } else {
            list = new ArrayList();
            list.add(values);
            return list;
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("SIZE (");
        buffer.append(values);
        buffer.append(")");

        return buffer.toString();
    }
}
