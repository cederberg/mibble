/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.type;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeTag;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.value.StringValue;

/**
 * A string MIB type.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class StringType extends MibType {

    /**
     * The additional type constraint.
     */
    private Constraint constraint = null;

    /**
     * Creates a new string MIB type.
     */
    public StringType() {
        this(true, null);
    }

    /**
     * Creates a new string MIB type.
     *
     * @param constraint     the additional type constraint
     */
    public StringType(Constraint constraint) {
        this(true, constraint);
    }

    /**
     * Creates a new string MIB type.
     *
     * @param primitive      the primitive type flag
     * @param constraint     the type constraint, or null
     */
    private StringType(boolean primitive, Constraint constraint) {
        super("OCTET STRING", primitive);
        this.constraint = constraint;
        setTag(true, MibTypeTag.OCTET_STRING);
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
    public MibType initialize(MibSymbol symbol, MibLoaderLog log)
        throws MibException {

        if (constraint != null) {
            constraint.initialize(this, log);
        }
        return this;
    }

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
     * @since 2.2
     */
    public MibType createReference() {
        StringType type = new StringType(false, constraint);
        type.setTag(true, getTag());
        return type;
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
     * @since 2.2
     */
    public MibType createReference(Constraint constraint) {
        StringType type = new StringType(false, constraint);
        type.setTag(true, getTag());
        return type;
    }

    /**
     * Checks if this type has any constraint.
     *
     * @return true if this type has any constraint, or
     *         false otherwise
     */
    public boolean hasConstraint() {
        return constraint != null;
    }

    /**
     * Checks if the specified value is compatible with this type.  A
     * value is compatible if it is a string value that is compatible
     * with the constraints.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return isCompatibleType(value)
            && (constraint == null || constraint.isCompatible(value));
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if it is a string value.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    private boolean isCompatibleType(MibValue value) {
        return value instanceof StringValue;
    }

    /**
     * Returns the optional type constraint. The type constraint for
     * a string will typically be a size constraint.
     *
     * @return the type constraint, or
     *         null if no constraint has been set
     *
     * @since 2.2
     */
    public Constraint getConstraint() {
        return constraint;
    }

    /**
     * Returns a string representation of this type.
     *
     * @return a string representation of this type
     */
    public String toString() {
        StringBuilder  buffer = new StringBuilder();
        buffer.append(super.toString());
        if (constraint != null) {
            buffer.append(" (");
            buffer.append(constraint.toString());
            buffer.append(")");
        }
        return buffer.toString();
    }
}
