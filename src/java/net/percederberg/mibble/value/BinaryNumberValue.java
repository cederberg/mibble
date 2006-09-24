/*
 * BinaryNumberValue.java
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

package net.percederberg.mibble.value;

import java.math.BigInteger;
import java.util.ArrayList;

import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.type.CompoundConstraint;
import net.percederberg.mibble.type.Constraint;
import net.percederberg.mibble.type.IntegerType;
import net.percederberg.mibble.type.SizeConstraint;
import net.percederberg.mibble.type.StringType;
import net.percederberg.mibble.type.ValueConstraint;

/**
 * A binary numeric MIB value.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.8
 * @since    2.6
 */
public class BinaryNumberValue extends NumberValue {

    /**
     * The minimum number of bits to print.
     */
    private int minLength = 0;

    /**
     * Creates a new binary number value.
     *
     * @param value          the number value
     */
    public BinaryNumberValue(Number value) {
        super(value);
    }

    /**
     * Initializes the MIB value. This will remove all levels of
     * indirection present, such as references to other values. No
     * value information is lost by this operation. This method may
     * modify this object as a side-effect, and will return the basic
     * value.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param log            the MIB loader log
     * @param type           the value type
     *
     * @return the basic MIB value
     */
    public MibValue initialize(MibLoaderLog log, MibType type) {
        Constraint  c = null;

        // TODO: this code should be moved to a utility function
        if (type instanceof IntegerType) {
            c = ((IntegerType) type).getConstraint();
        } else if (type instanceof StringType) {
            c = ((StringType) type).getConstraint();
        }
        minLength = getByteSize(c) * 8;
        return this;
    }

    // TODO: this method shold be moved to a utility class or similar
    private int getByteSize(Constraint c) {
        ArrayList        list;
        ValueConstraint  value;
        int              size;

        if (c instanceof CompoundConstraint) {
            list = ((CompoundConstraint) c).getConstraintList();
            for (int i = 0; i < list.size(); i++) {
                size = getByteSize((Constraint) list.get(i));
                if (size > 0) {
                    return size;
                }
            }
        } else if (c instanceof SizeConstraint) {
            value = (ValueConstraint) ((SizeConstraint) c).getValues().get(0);
            if (value.getValue().toObject() instanceof Number) {
                return ((Number) value.getValue().toObject()).intValue();
            }
        }
        return 0;
    }

    /**
     * Returns a string representation of this value.
     *
     * @return a string representation of this value
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        String        value;

        buffer.append("'");
        value = toBinaryString();
        for (int i = value.length(); i < minLength; i++) {
            buffer.append("0");
        }
        buffer.append(value);
        buffer.append("'B");
        return buffer.toString();
    }

    /**
     * Returns a binary representation of this value.
     *
     * @return a binary representation of this value
     */
    private String toBinaryString() {
        Number  num = (Number) toObject();

        if (num instanceof BigInteger) {
            return ((BigInteger) num).toString(2);
        } else if (num instanceof Long) {
            return Long.toBinaryString(num.longValue());
        } else {
            return Integer.toBinaryString(num.intValue());
        }
    }
}
