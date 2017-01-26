/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.value;

import java.math.BigDecimal;
import java.math.BigInteger;

import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.type.Constraint;
import net.percederberg.mibble.type.SizeConstraint;
import net.percederberg.mibble.type.StringType;

/**
 * A numeric MIB value.
 *
 * @author   Per Cederberg
 * @version  2.9
 * @since    2.0
 */
public class NumberValue extends MibValue {

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
        super("Number");
        this.value = value;
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
        return this;
    }

    /**
     * Creates a value reference to this value. The value reference
     * is normally an identical value. Only certain values support
     * being referenced, and the default implementation of this
     * method throws an exception.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @return the MIB value reference
     *
     * @since 2.2
     */
    public MibValue createReference() {
        return new NumberValue(value);
    }

    /**
     * Compares this object with the specified object for order. This
     * method will attempt to compare by numerical value, but will
     * use a string comparison as the default comparison operation.
     *
     * @param obj            the object to compare to
     *
     * @return less than zero if this object is less than the specified,
     *         zero if the objects are equal, or
     *         greater than zero otherwise
     *
     * @since 2.6
     */
    public int compareTo(Object obj) {
        if (obj instanceof NumberValue) {
            return compareToNumber(((NumberValue) obj).value);
        } else if (obj instanceof Number) {
            return compareToNumber((Number) obj);
        } else {
            return toString().compareTo(obj.toString());
        }
    }

    /**
     * Compares this object with the specified number for order.
     *
     * @param num            the number to compare to
     *
     * @return less than zero if this number is less than the specified,
     *         zero if the numbers are equal, or
     *         greater than zero otherwise
     */
    private int compareToNumber(Number num) {
        BigDecimal  num1;
        BigDecimal  num2;

        if (value instanceof Integer && num instanceof Integer) {
            return ((Integer) value).compareTo((Integer) num);
        } else if (value instanceof Long && num instanceof Long) {
            return ((Long) value).compareTo((Long) num);
        } else if (value instanceof BigInteger
                && num instanceof BigInteger) {

            return ((BigInteger) value).compareTo((BigInteger) num);
        } else {
            num1 = new BigDecimal(value.toString());
            num2 = new BigDecimal(num.toString());
            return num1.compareTo(num2);
        }
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
        return compareTo(obj) == 0;
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

    /**
     * Returns the number of bytes required by the specified type and
     * initial value size. If the type has no size requirement
     * specified, a value of one (1) will always be returned. If the
     * type size constraint allows for zero length, a zero might also
     * be returned.
     *
     * @param type           the MIB value type
     * @param initialBytes   the initial number of bytes used
     *
     * @return the number of bytes required
     */
    protected int getByteSize(MibType type, int initialBytes) {
        Constraint  c = null;
        int         res = -1;

        if (type instanceof StringType) {
            c = ((StringType) type).getConstraint();
        }
        if (c instanceof SizeConstraint) {
            res = ((SizeConstraint) c).nextValue(initialBytes);
        }
        if (res < 0) {
            res = 1;
        }
        return res;
    }
}
