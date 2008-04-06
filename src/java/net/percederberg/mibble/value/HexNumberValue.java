/*
 * HexNumberValue.java
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
 * A hexadecimal numeric MIB value.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.6
 */
public class HexNumberValue extends NumberValue {

    /**
     * The minimum number of hexadecimal characters to print.
     */
    private int minLength;

    /**
     * Creates a new hexadecimal number value. A default minimum
     * print length of one (1) will be used.
     *
     * @param value          the number value
     */
    public HexNumberValue(Number value) {
        this(value, 1);
    }

    /**
     * Creates a new hexadecimal number value.
     *
     * @param value          the number value
     * @param minLength      the minimum print length
     *
     * @since 2.9
     */
    public HexNumberValue(Number value, int minLength) {
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
        int bytes = minLength / 2 + ((minLength % 2 > 0) ? 1 : 0);
        int length = getByteSize(type, bytes) * 2;
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
        value = toHexString();
        if (value.equals("0")) {
            value = "";
        }
        for (int i = value.length(); i < minLength; i++) {
            buffer.append("0");
        }
        buffer.append(value);
        buffer.append("'H");
        return buffer.toString();
    }

    /**
     * Returns a hexadecimal representation of this value.
     *
     * @return a hexadecimal representation of this value
     */
    private String toHexString() {
        Number  num = (Number) toObject();

        if (num instanceof BigInteger) {
            return ((BigInteger) num).toString(16).toUpperCase();
        } else if (num instanceof Long) {
            return Long.toHexString(num.longValue()).toUpperCase();
        } else {
            return Integer.toHexString(num.intValue()).toUpperCase();
        }
    }
}
