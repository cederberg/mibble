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
 * Copyright (c) 2005-2008 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.value;

import java.math.BigInteger;

import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;

/**
 * A binary numeric MIB value.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.6
 */
public class BinaryNumberValue extends NumberValue {

    /**
     * The minimum number of bits to print.
     */
    private int minLength;

    /**
     * Creates a new binary number value. A default minimum print
     * length of one (1) will be used.
     *
     * @param value          the number value
     */
    public BinaryNumberValue(Number value) {
        this(value, 1);
    }

    /**
     * Creates a new binary number value.
     *
     * @param value          the number value
     * @param minLength      the minimum print length
     *
     * @since 2.9
     */
    public BinaryNumberValue(Number value, int minLength) {
        super(value);
        this.minLength = minLength;
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
        int bytes = minLength / 8 + ((minLength % 8 > 0) ? 1 : 0);
        int length = getByteSize(type, bytes) * 8;
        if (length > minLength) {
            minLength = length;
        }
        return this;
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
        if (value.equals("0")) {
            value = "";
        }
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
