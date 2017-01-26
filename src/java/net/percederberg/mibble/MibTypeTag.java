/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A MIB type tag. The type tag consists of a category and value.
 * Together these two numbers normally identifies a type uniquely, as
 * all primitive and most (if not all) SNMP types (such as IpAddress
 * and similar) have type tags assigned to them. Type tags may also
 * be chained together in a list, in order to not loose information.
 * Whether to replace or to chain a type tag is determined by the
 * EXPLICIT or IMPLICIT keywords in the MIB file.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.2
 */
public class MibTypeTag {

    /**
     * The universal type tag category. This is the type tag category
     * used for the ASN.1 primitive types.
     */
    public static final int UNIVERSAL_CATEGORY = 0;

    /**
     * The application type tag category.
     */
    public static final int APPLICATION_CATEGORY = 1;

    /**
     * The context specific type tag category. This is the default
     * type tag category if no other category was specified.
     */
    public static final int CONTEXT_SPECIFIC_CATEGORY = 2;

    /**
     * The private type tag category.
     */
    public static final int PRIVATE_CATEGORY = 3;

    /**
     * The universal boolean type tag.
     */
    public static final MibTypeTag BOOLEAN =
        new MibTypeTag(UNIVERSAL_CATEGORY, 1);

    /**
     * The universal integer type tag.
     */
    public static final MibTypeTag INTEGER =
        new MibTypeTag(UNIVERSAL_CATEGORY, 2);

    /**
     * The universal bit string type tag.
     */
    public static final MibTypeTag BIT_STRING =
        new MibTypeTag(UNIVERSAL_CATEGORY, 3);

    /**
     * The universal octet string type tag.
     */
    public static final MibTypeTag OCTET_STRING =
        new MibTypeTag(UNIVERSAL_CATEGORY, 4);

    /**
     * The universal null type tag.
     */
    public static final MibTypeTag NULL =
        new MibTypeTag(UNIVERSAL_CATEGORY, 5);

    /**
     * The universal object identifier type tag.
     */
    public static final MibTypeTag OBJECT_IDENTIFIER =
        new MibTypeTag(UNIVERSAL_CATEGORY, 6);

    /**
     * The universal real type tag.
     */
    public static final MibTypeTag REAL =
        new MibTypeTag(UNIVERSAL_CATEGORY, 9);

    /**
     * The universal sequence and sequence of type tag.
     */
    public static final MibTypeTag SEQUENCE =
        new MibTypeTag(UNIVERSAL_CATEGORY, 16);

    /**
     * The universal sequence and sequence of type tag.
     */
    public static final MibTypeTag SET =
        new MibTypeTag(UNIVERSAL_CATEGORY, 17);

    /**
     * The tag category.
     */
    private int category;

    /**
     * The tag value.
     */
    private int value;

    /**
     * The next type tag in the type tag chain.
     */
    private MibTypeTag next = null;

    /**
     * Creates a new MIB type tag.
     *
     * @param category       the type tag category
     * @param value          the type tag value
     */
    public MibTypeTag(int category, int value) {
        this.category = category;
        this.value = value;
    }

    /**
     * Checks if this type tag equals another object. This method
     * will only return true if the other object is a type tag with
     * the same category and value numbers.
     *
     * @param obj            the object to compare to
     *
     * @return true if the objects are equal, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        if (obj instanceof MibTypeTag) {
            MibTypeTag tag = (MibTypeTag) obj;
            return equals(tag.category, tag.value);
        } else {
            return false;
        }
    }

    /**
     * Checks if this type tag has the specified category and
     * value numbers.
     *
     * @param category       the category number
     * @param value          the value number
     *
     * @return true if the category and value numbers match, or
     *         false otherwise
     */
    public boolean equals(int category, int value) {
        return this.category == category && this.value == value;
    }

    /**
     * Returns the hash code value for the object. This method is
     * reimplemented to fulfil the contract of returning the same
     * hash code for objects that are considered equal.
     *
     * @return the hash code value for the object
     *
     * @since 2.6
     */
    public int hashCode() {
        return (category << 8) + value;
    }

    /**
     * Returns the type tag category. The category value corresponds
     * to one of the defined category constants.
     *
     * @return the type tag category
     *
     * @see #UNIVERSAL_CATEGORY
     * @see #APPLICATION_CATEGORY
     * @see #CONTEXT_SPECIFIC_CATEGORY
     * @see #PRIVATE_CATEGORY
     */
    public int getCategory() {
        return category;
    }

    /**
     * Returns the type tag value. The tag category and value
     * normally identifies a type uniquely.
     *
     * @return the type tag value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the next type tag in the tag chain.
     *
     * @return the next type tag in the tag chain, or
     *         null if there is no next tag
     */
    public MibTypeTag getNext() {
        return next;
    }

    /**
     * Sets the next type tag in the tag chain.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param next           the next type tag
     */
    public void setNext(MibTypeTag next) {
        this.next = next;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder  buffer = new StringBuilder();
        buffer.append("[");
        if (category == UNIVERSAL_CATEGORY) {
            buffer.append("UNIVERSAL ");
        } else if (category == APPLICATION_CATEGORY) {
            buffer.append("APPLICATION ");
        } else if (category == PRIVATE_CATEGORY) {
            buffer.append("PRIVATE ");
        }
        buffer.append(value);
        buffer.append("]");
        if (next != null) {
            buffer.append(" ");
            buffer.append(next.toString());
        }
        return buffer.toString();
    }
}
