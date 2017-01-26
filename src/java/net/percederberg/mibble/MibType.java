/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.util.ArrayList;

import net.percederberg.mibble.type.Constraint;

/**
 * The base MIB type class. There are two categories of MIB types
 * extending this class, primitive ASN.1 type and SNMP macro types.
 * The primitive types are used in SNMP for transferring data on the
 * wire. The SNMP macro types are used in the MIB files for adding
 * additional information to the primitive types or values, such as
 * descriptions and similar. Most of the SNMP macro types only
 * support object identifier values, and can only be used at the top
 * level. The primitive types support whatever values are appropriate
 * for the specific type, and are normally used inside the SNMP macro
 * types in a MIB file.<p>
 *
 * The best way to extract the specific type information from a MIB
 * type is to check the type instance and then cast the MibType
 * object to the corresponding subtype. Each subtype have very
 * different properties, which is why the API in this class is rather
 * limited. The example below shows some skeleton code for extracting
 * type information.
 *
 * <pre>    if (type instanceof SnmpObjectType) {
 *        objectType = (SnmpObjectType) type;
 *        ...
 *    }</pre>
 *
 * Another way to check which type is at hand, is to query the type
 * tags with the hasTag() method. In this way it is possible to
 * distinguish between types using the same or a similar primitive
 * ASN.1 type representation (such as DisplayString and IpAddress).
 * This should normally be done in order to create a correct BER-
 * or DER-encoding of the type. The example below illustrates how
 * this could be done.
 *
 * <pre>    tag = type.getTag();
 *    if (tag.getCategory() == MibTypeTag.UNIVERSAL) {
 *        // Set BER and DER identifier bits 8 &amp; 7 to 00
 *    } else if (tag.getCategory() == MibTypeTag.APPLICATION) {
 *        // Set BER and DER identifier bits 8 &amp; 7 to 01
 *    }
 *    ...
 *    if (!type.isPrimitive()) {
 *        // Set BER and DER constructed bit
 *    }</pre>
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public abstract class MibType {

    /**
     * The type name.
     */
    private String name;

    /**
     * The primitive type flag.
     */
    private boolean primitive;

    /**
     * The type tag.
     */
    private MibTypeTag tag = null;

    /**
     * The type reference symbol. This is set to the referenced type
     * symbol when resolving this type.
     */
    private MibTypeSymbol reference = null;

    /**
     * The type comment.
     */
    private String comment = null;

    /**
     * Creates a new MIB type instance.
     *
     * @param name           the type name
     * @param primitive      the primitive type flag
     */
    protected MibType(String name, boolean primitive) {
        this.name = name;
        this.primitive = primitive;
    }

    /**
     * Initializes the MIB type. This will remove all levels of
     * indirection present, such as references to types or values. No
     * information is lost by this operation. This method may modify
     * this object as a side-effect, and will return the basic
     * type.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param symbol         the MIB symbol containing this type
     * @param log            the MIB loader log
     *
     * @return the basic MIB type
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     *
     * @since 2.2
     */
    public abstract MibType initialize(MibSymbol symbol, MibLoaderLog log)
        throws MibException;

    /**
     * Creates a type reference to this type. The type reference is
     * normally an identical type, but with the primitive flag set to
     * false. Only certain types support being referenced, and the
     * default implementation of this method throws an exception.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @return the MIB type reference
     *
     * @throws UnsupportedOperationException if a type reference
     *             couldn't be created
     *
     * @since 2.2
     */
    public MibType createReference()
        throws UnsupportedOperationException {

        String msg = name + " type cannot be referenced";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Creates a constrained type reference to this type. The type
     * reference is normally an identical type, but with the
     * primitive flag set to false. Only certain types support being
     * referenced, and the default implementation of this method
     * throws an exception.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param constraint     the type constraint
     *
     * @return the MIB type reference
     *
     * @throws UnsupportedOperationException if a type reference
     *             couldn't be created with constraints
     *
     * @since 2.2
     */
    public MibType createReference(Constraint constraint)
        throws UnsupportedOperationException {

        String msg = name + " type cannot be referenced with constraints";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Creates a constrained type reference to this type. The type
     * reference is normally an identical type, but with the
     * primitive flag set to false. Only certain types support being
     * referenced, and the default implementation of this method
     * throws an exception.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param values         the type value symbols
     *
     * @return the MIB type reference
     *
     * @throws UnsupportedOperationException if a type reference
     *             couldn't be created with value constraints
     *
     * @since 2.2
     */
    public MibType createReference(ArrayList<?> values)
        throws UnsupportedOperationException {

        String msg = name + " type cannot be referenced with " +
                     "defined values";
        throw new UnsupportedOperationException(msg);
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if it has a type that matches this one and
     * a value that satisfies all constraints.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public abstract boolean isCompatible(MibValue value);

    /**
     * Checks if this type represents a primitive type. The primitive
     * types are the basic building blocks of the ASN.1 type system.
     * By defining new types (that may be identical to a primitive
     * type), the new type looses it's primitive status.
     *
     * @return true if this type represents a primitive type, or
     *         false otherwise
     *
     * @since 2.2
     */
    public boolean isPrimitive() {
        return primitive;
    }

    /**
     * Checks if this type has a specific type tag. This method will
     * check the whole type tag chain.
     *
     * @param tag            the type tag to search for
     *
     * @return true if the specified type tag was present, or
     *         false otherwise
     *
     * @see #hasTag(int, int)
     *
     * @since 2.2
     */
    public boolean hasTag(MibTypeTag tag) {
        return hasTag(tag.getCategory(), tag.getValue());
    }

    /**
     * Checks if this type has a specific type tag. This method will
     * check the whole type tag chain.
     *
     * @param category       the tag category to search for
     * @param value          the tag value to search for
     *
     * @return true if the specified type tag was present, or
     *         false otherwise
     *
     * @see #hasTag(MibTypeTag)
     *
     * @since 2.2
     */
    public boolean hasTag(int category, int value) {
        MibTypeTag  iter = getTag();

        while (iter != null) {
            if (iter.equals(category, value)) {
                return true;
            }
            iter = iter.getNext();
        }
        return false;
    }

    /**
     * Checks if this type referenced the specified type symbol. This
     * method should be avoided if possible, as it is much better to
     * rely on type tags to distinguish between two types with the
     * same base type (such as DisplayString and IpAddress).
     *
     * @param name           the type symbol name
     *
     * @return true if this type was a reference to the symbol, or
     *         false otherwise
     *
     * @see #hasTag(int, int)
     * @see #hasTag(MibTypeTag)
     * @see #getReferenceSymbol()
     * @see net.percederberg.mibble.snmp.SnmpTextualConvention#findReference(MibType)
     *
     * @since 2.2
     */
    public boolean hasReferenceTo(String name) {
        if (reference == null) {
            return false;
        } else if (reference.getName().equals(name)) {
            return true;
        } else {
            return reference.getType().hasReferenceTo(name);
        }
    }

    /**
     * Checks if this type referenced the specified type symbol. This
     * method should be avoided if possible, as it is much better to
     * rely on type tags to distinguish between two types with the
     * same base type (such as DisplayString and IpAddress).
     *
     * @param module         the type symbol module (MIB) name
     * @param name           the type symbol name
     *
     * @return true if this type was a reference to the symbol, or
     *         false otherwise
     *
     * @see #hasTag(int, int)
     * @see #hasTag(MibTypeTag)
     * @see #getReferenceSymbol()
     * @see net.percederberg.mibble.snmp.SnmpTextualConvention#findReference(MibType)
     *
     * @since 2.2
     */
    public boolean hasReferenceTo(String module, String name) {
        if (reference == null) {
            return false;
        }
        Mib mib = reference.getMib();
        if (mib.getName().equals(module) && reference.getName().equals(name)) {
            return true;
        } else {
            return reference.getType().hasReferenceTo(module, name);
        }
    }

    /**
     * Returns the type name.
     *
     * @return the type name, or
     *         an empty string if not applicable
     *
     * @since 2.2
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type tag. The type tags consist of a category and
     * value number, and are used to identify a specific type
     * uniquely (such as IpAddress and similar). Most (if not all)
     * SNMP types have unique tags that are normally inherited when
     * the type is referenced. Type tags may also be chained
     * together, in which case this method returns the first tag in
     * the chain.
     *
     * @return the type tag, or
     *         null if no type tag has been set
     *
     * @since 2.2
     */
    public MibTypeTag getTag() {
        return tag;
    }

    /**
     * Sets the type tag. The old type tag is kept to some extent,
     * depending on if the implicit flag is set to true or false. For
     * implicit inheritance, the first tag in the old tag chain is
     * replaced with the new tag. For explicit inheritance, the new
     * tag is added first in the tag chain without removing any old
     * tag.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param implicit       the implicit inheritance flag
     * @param tag            the new type tag
     *
     * @since 2.2
     */
    public void setTag(boolean implicit, MibTypeTag tag) {
        MibTypeTag  next = this.tag;

        if (implicit && next != null) {
            next = next.getNext();
        }
        if (tag != null) {
            tag.setNext(next);
        }
        this.tag = tag;
    }

    /**
     * Returns the type reference symbol. A type reference is created
     * whenever a type is defined in a type assignment, and later
     * referenced by name from some other symbol. The complete chain
     * of type references is available by calling getReference()
     * recursively on the type of the returned type symbol.<p>
     *
     * In general, this method should be avoided as it is much better
     * to rely on type tags to distinguish between two types with the
     * same base type (such as DisplayString and IpAddress).
     *
     * @return the type reference symbol, or
     *         null if this type never referenced another type
     *
     * @see #getTag()
     * @see net.percederberg.mibble.snmp.SnmpTextualConvention#findReference(MibType)
     *
     * @since 2.2
     */
    public MibTypeSymbol getReferenceSymbol() {
        return reference;
    }

    /**
     * Sets the type reference symbol. The type reference is set
     * whenever a type is defined in a type assignment, and later
     * referenced by name from some other symbol.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param symbol         the referenced type symbol
     *
     * @since 2.2
     */
    public void setReferenceSymbol(MibTypeSymbol symbol) {
        this.reference = symbol;
    }

    /**
     * Returns the type comment.
     *
     * @return the type comment, or
     *         null if no comment was set
     *
     * @since 2.9
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the type comment.
     *
     * @param comment        the type comment
     *
     * @since 2.9
     */
    void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        if (tag != null) {
            return tag.toString() + " " + name;
        } else {
            return name;
        }
    }
}
