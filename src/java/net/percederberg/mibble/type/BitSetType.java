/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.type;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

import net.percederberg.mibble.MibContext;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeTag;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.BitSetValue;
import net.percederberg.mibble.value.NumberValue;

/**
 * A bit set MIB type. This is the equivalent of a bit string, i.e.
 * a set of bit values
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class BitSetType extends MibType implements MibContext {

    /**
     * The additional type constraint.
     */
    private Constraint constraint = null;

    /**
     * The additional defined symbols.
     */
    private LinkedHashMap<String,MibValueSymbol> symbols = new LinkedHashMap<>();

    /**
     * Creates a new bit set MIB type.
     */
    public BitSetType() {
        this(true, null, null);
    }

    /**
     * Creates a new bit set MIB type.
     *
     * @param constraint     the additional type constraint
     */
    public BitSetType(Constraint constraint) {
        this(true, constraint, null);
    }

    /**
     * Creates a new bit set MIB type.
     *
     * @param values         the additional defined symbols
     */
    public BitSetType(ArrayList<?> values) {
        this(true, null, null);
        createValueConstraints(values);
    }

    /**
     * Creates a new bit set MIB type.
     *
     * @param primitive      the primitive type flag
     * @param constraint     the type constraint, or null
     * @param symbols        the defined symbols, or null
     */
    private BitSetType(boolean primitive,
                       Constraint constraint,
                       LinkedHashMap<String,MibValueSymbol> symbols) {

        super("BITS", primitive);
        if (constraint != null) {
            this.constraint = constraint;
        }
        if (symbols != null) {
            this.symbols = symbols;
        }
        setTag(true, MibTypeTag.BIT_STRING);
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
        Iterator<MibValueSymbol> iter = symbols.values().iterator();
        while (iter.hasNext()) {
            MibValueSymbol sym = iter.next();
            sym.initialize(log);
            if (!(sym.getValue() instanceof NumberValue)) {
                String message = "value is not compatible with type";
                throw new MibException(sym.getFileRef(), message);
            }
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
        BitSetType type =  new BitSetType(false, constraint, symbols);
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
        BitSetType type =  new BitSetType(false, constraint, null);
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
     * @param values         the type value symbols
     *
     * @return the MIB type reference
     *
     * @since 2.2
     */
    public MibType createReference(ArrayList<?> values) {
        BitSetType type = new BitSetType(false, null, null);
        type.createValueConstraints(values);
        type.setTag(false, getTag());
        return type;
    }

    /**
     * Creates the constraints and symbol map from a list of value
     * symbols.
     *
     * @param values         the list of value symbols
     */
    private void createValueConstraints(ArrayList<?> values) {
        for (Object val : values) {
            if (val instanceof MibValueSymbol) {
                MibValueSymbol sym = (MibValueSymbol) val;
                symbols.put(sym.getName(), sym);
                // TODO: check value constraint compability
                ValueConstraint c = new ValueConstraint(null, sym.getValue());
                if (constraint == null) {
                    constraint = c;
                } else {
                    constraint = new CompoundConstraint(constraint, c);
                }
            }
        }
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
     * Checks if this type has any defined value symbols.
     *
     * @return true if this type has any defined value symbols, or
     *         false otherwise
     */
    public boolean hasSymbols() {
        return symbols.size() > 0;
    }

    /**
     * Checks if the specified value is compatible with this type.  A
     * value is compatible if it is a bit set value with all
     * components compatible with the constraints.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return value instanceof BitSetValue
            && isCompatible((BitSetValue) value);
    }

    /**
     * Checks if the specified bit set value is compatible with this
     * type.  A value is compatible if all the bits are compatible
     * with the constraints.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(BitSetValue value) {
        if (constraint == null) {
            return true;
        }
        for (NumberValue bit : value.getBits()) {
            if (!constraint.isCompatible(bit)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the optional type constraint. The type constraints for
     * a bit set will typically be value constraints or compound
     * value constraints.
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
     * Returns a named bit value. The value will be returned as a
     * value symbol, containing a numeric MIB value. The symbol
     * returned is not a normal MIB symbol, i.e. it is not present in
     * any MIB file and only the name and value components are
     * valid.<p>
     *
     * <strong>Note:</strong> As of version 2.4 the method signature
     * was changed to return a MibValueSymbol instead of a MibSymbol.
     *
     * @param name           the symbol name
     *
     * @return the MIB value symbol, or
     *         null if not found
     */
    public MibValueSymbol getSymbol(String name) {
        return symbols.get(name);
    }

    /**
     * Returns all named bit values. Note that a bit string may allow
     * unnamed values, depending on the constraints. Use the
     * constraint object or the isCompatible() method to check if a
     * value is compatible with this type. Also note that the value
     * symbols returned by this method are not normal MIB symbols,
     * i.e. only the name and value components are valid.
     *
     * @return an array of all named values (as MIB value symbols)
     *
     * @since 2.2
     */
    public MibValueSymbol[] getAllSymbols() {
        MibValueSymbol[] res = new MibValueSymbol[symbols.size()];
        return symbols.values().toArray(res);
    }

    /**
     * Searches for a named MIB symbol. This method may search outside
     * the normal (or strict) scope, thereby allowing a form of
     * relaxed search. Note that the results from the normal and
     * expanded search may not be identical, due to the context
     * chaining and the same symbol name appearing in various
     * contexts.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param name           the symbol name
     * @param expanded       the expanded scope flag
     *
     * @return the MIB symbol, or null if not found
     *
     * @since 2.4
     */
    public MibSymbol findSymbol(String name, boolean expanded) {
        return getSymbol(name);
    }

    /**
     * Returns a string representation of this type.
     *
     * @return a string representation of this type
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        if (symbols.size() > 0) {
            buffer.append(" { ");
            Iterator<MibValueSymbol> iter = symbols.values().iterator();
            while (iter.hasNext()) {
                MibValueSymbol symbol = iter.next();
                buffer.append(symbol.getName());
                buffer.append("(");
                buffer.append(symbol.getValue());
                buffer.append(")");
                if (iter.hasNext()) {
                    buffer.append(", ");
                }
            }
            buffer.append(" }");
        } else if (constraint != null) {
            buffer.append(" (");
            buffer.append(constraint.toString());
            buffer.append(")");
        }
        return buffer.toString();
    }
}
