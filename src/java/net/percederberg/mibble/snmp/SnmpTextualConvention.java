/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import java.util.ArrayList;

import net.percederberg.mibble.MibContext;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.type.Constraint;

/**
 * The SNMP textual convention macro type. This macro type was added
 * to SMIv2 and is defined in RFC 2579.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2579.txt">RFC 2579 (SNMPv2-TC)</a>
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SnmpTextualConvention extends SnmpType implements MibContext {

    /**
     * The display hint.
     */
    private String displayHint;

    /**
     * The type status.
     */
    private SnmpStatus status;

    /**
     * The type reference.
     */
    private String reference;

    /**
     * The type syntax.
     */
    private MibType syntax;

    /**
     * Finds the first SNMP textual convention reference for a type. If the
     * type specified is a textual convention, it will be returned directly.
     *
     * @param type           the MIB type
     *
     * @return the SNMP textual convention reference, or
     *         null if none was found
     *
     * @since 2.7
     */
    public static SnmpTextualConvention findReference(MibType type) {
        MibTypeSymbol  sym;

        if (type instanceof SnmpObjectType) {
            type = ((SnmpObjectType) type).getSyntax();
        }
        if (type instanceof SnmpTextualConvention) {
            return (SnmpTextualConvention) type;
        }
        sym = type.getReferenceSymbol();
        return (sym == null) ? null : findReference(sym.getType());
    }

    /**
     * Creates a new SNMP textual convention.
     *
     * @param displayHint    the display hint, or null
     * @param status         the type status
     * @param description    the type description
     * @param reference      the type reference, or null
     * @param syntax         the type syntax
     */
    public SnmpTextualConvention(String displayHint,
                                 SnmpStatus status,
                                 String description,
                                 String reference,
                                 MibType syntax) {

        super("TEXTUAL-CONVENTION", description);
        this.displayHint = displayHint;
        this.status = status;
        this.reference = reference;
        this.syntax = syntax;
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

        syntax = syntax.initialize(symbol, log);
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
     * @throws UnsupportedOperationException if a type reference
     *             couldn't be created
     *
     * @since 2.2
     */
    public MibType createReference()
        throws UnsupportedOperationException {

        return syntax.createReference();
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

        return syntax.createReference(constraint);
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

        return syntax.createReference(values);
    }

    /**
     * Checks if the specified value is compatible with this type. No
     * value is compatible with this type, so this method always
     * returns false.
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
     * Returns the display hint.
     *
     * @return the display hint, or
     *         null if no display hint has been set
     */
    public String getDisplayHint() {
        return displayHint;
    }

    /**
     * Returns the type status.
     *
     * @return the type status
     */
    public SnmpStatus getStatus() {
        return status;
    }

    /**
     * Returns the type reference.
     *
     * @return the type reference, or
     *         null if no reference has been set
     */
    public String getReference() {
        return reference;
    }

    /**
     * Returns the type syntax.
     *
     * @return the type syntax
     */
    public MibType getSyntax() {
        return syntax;
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
        if (syntax instanceof MibContext) {
            return ((MibContext) syntax).findSymbol(name, expanded);
        } else {
            return null;
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append(" (");
        if (displayHint != null) {
            buffer.append("\n  Display-Hint: ");
            buffer.append(displayHint);
        }
        buffer.append("\n  Status: ");
        buffer.append(status);
        buffer.append("\n  Description: ");
        buffer.append(getDescription("               "));
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        buffer.append("\n  Syntax: ");
        buffer.append(syntax);
        buffer.append("\n)");
        return buffer.toString();
    }
}
