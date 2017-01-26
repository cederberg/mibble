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

/**
 * An sequence of a MIB type. In some other languages this is known
 * as an array.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SequenceOfType extends MibType {

    /**
     * The base type.
     */
    private MibType base;

    /**
     * The additional type constraint.
     */
    private Constraint constraint = null;

    /**
     * Creates a new sequence of a MIB type.
     *
     * @param base           the sequence element type
     */
    public SequenceOfType(MibType base) {
        this(true, base, null);
    }

    /**
     * Creates a new sequence of a MIB type.
     *
     * @param base           the sequence element type
     * @param constraint     the sequence constraint
     */
    public SequenceOfType(MibType base, Constraint constraint) {
        this(true, base, constraint);
    }

    /**
     * Creates a new sequence of a MIB type.
     *
     * @param primitive      the primitive type flag
     * @param base           the sequence element type
     * @param constraint     the sequence constraint
     */
    private SequenceOfType(boolean primitive,
                           MibType base,
                           Constraint constraint) {

        super("SEQUENCE", primitive);
        this.base = base;
        this.constraint = constraint;
        setTag(true, MibTypeTag.SEQUENCE);
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

        base = base.initialize(symbol, log);
        if (base != null && constraint != null) {
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
        SequenceOfType type = new SequenceOfType(false, base, constraint);
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
        SequenceOfType type = new SequenceOfType(false, base, constraint);
        type.setTag(true, getTag());
        return type;
    }

    /**
     * Checks if this type has any constraint.
     *
     * @return true if this type has any constraint, or
     *         false otherwise
     *
     * @since 2.2
     */
    public boolean hasConstraint() {
        return constraint != null;
    }

    /**
     * Checks if the specified value is compatible with this type. No
     * values are considered compatible with this type, and this
     * method will therefore always return false.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return false;
    }

    /**
     * Returns the optional type constraint. The type constraint for
     * a sequence of type will typically be a size constraint.
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
     * Returns the sequence element type. This is the type of each
     * individual element in the sequence.
     *
     * @return the sequence element type
     *
     * @since 2.2
     */
    public MibType getElementType() {
        return base;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder  buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append(" ");
        if (constraint != null) {
             buffer.append("(");
             buffer.append(constraint.toString());
             buffer.append(") ");
        }
        buffer.append("OF ");
        buffer.append(base);
        return buffer.toString();
    }
}
