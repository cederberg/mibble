/*
 * NamedNumber.java
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

package net.percederberg.mibble;

import net.percederberg.mibble.value.ValueReference;

/**
 * A named number. This class is used for storing intermediate values
 * during the parsing.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
class NamedNumber {

    /**
     * The value name.
     */
    private String name = null;

    /**
     * The numeric value.
     */
    private Number number = null;

    /**
     * The value reference.
     */
    private ValueReference reference = null;

    /**
     * Creates a new named number.
     *
     * @param number         the numeric value
     */
    public NamedNumber(Number number) {
        this(null, number);
    }

    /**
     * Creates a new named number.
     *
     * @param name           the value name
     * @param number         the numeric value
     */
    public NamedNumber(String name, Number number) {
        this.name = name;
        this.number = number;
    }

    /**
     * Creates a new named number.
     *
     * @param reference      the value reference
     */
    public NamedNumber(ValueReference reference) {
        this(null, reference);
    }

    /**
     * Creates a new named number.
     *
     * @param name           the value name
     * @param reference      the value reference
     */
    public NamedNumber(String name, ValueReference reference) {
        this.name = name;
        this.reference = reference;
    }

    /**
     * Checks if this named number has a name component.
     *
     * @return true if this named number has a name component, or
     *         false otherwise
     */
    public boolean hasName() {
        return name != null;
    }

    /**
     * Checks if this named number has a number component.
     *
     * @return true if this named number has a number component, or
     *         false otherwise
     */
    public boolean hasNumber() {
        return number != null;
    }

    /**
     * Checks if this named number has a value reference.
     *
     * @return true if this named number has a value reference, or
     *         false otherwise
     */
    public boolean hasReference() {
        return reference != null;
    }

    /**
     * Returns the value name.
     *
     * @return the value name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the numeric value.
     *
     * @return the numeric value
     */
    public Number getNumber() {
        return number;
    }

    /**
     * Returns the value reference.
     *
     * @return the value reference
     */
    public ValueReference getReference() {
        return reference;
    }
}
