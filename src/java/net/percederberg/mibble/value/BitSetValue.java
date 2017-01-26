/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.value;

import java.util.ArrayList;
import java.util.BitSet;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;

/**
 * A bit set MIB value.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class BitSetValue extends MibValue {

    /**
     * The bit set value.
     */
    private BitSet value;

    /**
     * The additional value references.
     */
    private ArrayList<ValueReference> references;

    /**
     * Creates a new bit set MIB value.
     *
     * @param value          the bit set value
     */
    public BitSetValue(BitSet value) {
        this(value, null);
    }

    /**
     * Creates a new bit set MIB value.
     *
     * @param value          the bit set value
     * @param references     the additional referenced bit values
     */
    public BitSetValue(BitSet value, ArrayList<ValueReference> references) {
        super("BIT STRING");
        this.value = value;
        this.references = references;
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
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public MibValue initialize(MibLoaderLog log, MibType type)
        throws MibException {

        if (references != null) {
            for (ValueReference ref : references) {
                initialize(log, type, ref);
            }
            references = null;
        }
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
        return new BitSetValue(value, references);
    }

    /**
     * Initializes a the MIB value from a value reference. This will
     * resolve the reference, and set the bit corresponding to the
     * value.
     *
     * @param log            the MIB loader log
     * @param type           the value type
     * @param ref            the value reference to resolve
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    private void initialize(MibLoaderLog log, MibType type, ValueReference ref)
        throws MibException {

        MibValue value = ref.initialize(log, type);
        if (value instanceof NumberValue) {
            this.value.set(((Number) value.toObject()).intValue());
        } else {
            throw new MibException(ref.getFileRef(),
                                   "referenced value is not a number");
        }
    }

    /**
     * Clears and prepares this value for garbage collection. This
     * method will recursively clear any associated types or values,
     * making sure that no data structures references this object.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     */
    protected void clear() {
        super.clear();
        value = null;
        references = null;
    }

    /**
     * Returns all the bits in this bit set as individual number
     * values.
     *
     * @return the number values for all bits in this bit set
     */
    public ArrayList<NumberValue> getBits() {
        ArrayList<NumberValue> components = new ArrayList<>(value.size());
        for (int i = 0; i < value.size(); i++) {
            if (value.get(i)) {
                components.add(new NumberValue(new Integer(i)));
            }
        }
        return components;
    }

    /**
     * Compares this object with the specified object for order. This
     * method will only compare the string representations with each
     * other.
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
        return toString().compareTo(obj.toString());
    }

    /**
     * Checks if this object equals another object. This method will
     * compare the string representations for equality.
     *
     * @param obj            the object to compare with
     *
     * @return true if the objects are equal, or
     *         false otherwise
     *
     * @since 2.6
     */
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code for this object
     *
     * @since 2.6
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Returns a Java BitSet representation of this value.
     *
     * @return a Java BitSet representation of this value
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
     * Returns an ASN.1 representation of this value. The string will
     * contain named references to any values that can be found in the
     * specified list.
     *
     * @param values         the defined symbol values
     *
     * @return an ASN.1 representation of this value
     *
     * @since 2.8
     */
    public String toAsn1String(MibValueSymbol[] values) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < value.size(); i++) {
            if (value.get(i)) {
                if (buffer.length() > 0) {
                    buffer.append(", ");
                }
                buffer.append(toAsn1String(new Integer(i), values));
            }
        }
        if (buffer.length() > 0) {
            return "{ " + buffer.toString() + " }";
        } else {
            return "{}";
        }
    }

    /**
     * Returns an ASN.1 representation of a bit number. The value
     * name will be returned if found in the specified array.
     *
     * @param bit            the bit number
     * @param values         the defined bit names
     *
     * @return the ASN.1 representation of the bit number
     */
    private String toAsn1String(Integer bit, MibValueSymbol[] values) {
        for (MibValueSymbol sym : values) {
            if (sym.getValue().equals(bit)) {
                return sym.getName();
            }
        }
        return bit.toString();
    }
}
