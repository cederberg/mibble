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
 * Copyright (c) 2005 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.value;

import java.math.BigInteger;

/**
 * A hexadecimal numeric MIB value.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.6
 * @since    2.6
 */
public class HexNumberValue extends NumberValue {

    /**
     * Creates a new hexadecimal number value.
     *
     * @param value          the number value
     */
    public HexNumberValue(Number value) {
        super(value);
    }

    /**
     * Returns a string representation of this value.
     *
     * @return a string representation of this value
     */
    public String toString() {
        Number        num = (Number) toObject();
        StringBuffer  buffer = new StringBuffer();

        buffer.append("'");
        if (num instanceof BigInteger) {
            buffer.append(((BigInteger) num).toString(16));
        } else if (num instanceof Long) {
            buffer.append(Long.toHexString(num.longValue()));
        } else {
            buffer.append(Integer.toHexString(num.intValue()));
        }
        buffer.append("'H");
        return buffer.toString().toUpperCase();
    }
}
