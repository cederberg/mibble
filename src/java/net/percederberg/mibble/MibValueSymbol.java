/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.type.SequenceOfType;
import net.percederberg.mibble.type.SequenceType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A MIB value symbol. This class holds information relevant to a MIB
 * value assignment, i.e. a type and a value. Normally the value is
 * an object identifier.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class MibValueSymbol extends MibSymbol {

    /**
     * The symbol type.
     */
    private MibType type;

    /**
     * The symbol value.
     */
    private MibValue value;

    /**
     * Creates a new value symbol.<p>
     *
     * <strong>NOTE:</strong> This is an internal constructor that
     * should only be called by the MIB loader.
     *
     * @param fileRef        the MIB file reference
     * @param mib            the symbol MIB file
     * @param name           the symbol name
     * @param type           the symbol type
     * @param value          the symbol value
     *
     * @since 2.2
     */
    public MibValueSymbol(MibFileRef fileRef,
                          Mib mib,
                          String name,
                          MibType type,
                          MibValue value) {

        super(fileRef, mib, name);
        this.type = type;
        this.value = value;
    }

    /**
     * Initializes the MIB symbol. This will remove all levels of
     * indirection present, such as references to types or values. No
     * information is lost by this operation. This method may modify
     * this object as a side-effect.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public void initialize(MibLoaderLog log) throws MibException {
        if (type != null) {
            try {
                type = type.initialize(this, log);
            } catch (MibException e) {
                log.addError(e);
                type = null;
            }
        }
        if (value != null) {
            try {
                value = value.initialize(log, type);
            } catch (MibException e) {
                log.addError(e);
                value = null;
            }
        }
        if (type != null && value != null && !type.isCompatible(value)) {
            log.addError(getFileRef(),
                         "value is not compatible with type");
        }
        if (value instanceof ObjectIdentifierValue) {
            ObjectIdentifierValue oid = (ObjectIdentifierValue) value;
            if (oid.getSymbol() == null) {
                oid.setSymbol(this);
            } else {
                boolean loaded = getMib().isLoaded() || oid.getMib().isLoaded();
                if (oid.getSymbol() != this && loaded) {
                    log.addWarning(getFileRef(),
                                   "duplicate definition of " + oid +
                                   ", previously defined as '" +
                                   oid.getSymbol().getName() + "' in " +
                                   oid.getSymbol().getMib().getName());
                }
            }
        }
    }

    /**
     * Clears and prepares this MIB symbol for garbage collection.
     * This method will recursively clear any associated types or
     * values, making sure that no data structures references this
     * symbol.
     */
    void clear() {
        type = null;
        if (value != null) {
            value.clear();
        }
        value = null;
    }

    /**
     * Checks if this symbol corresponds to a scalar. A symbol is
     * considered a scalar if it has an SnmpObjectType type and does
     * not represent or reside within a table.
     *
     * @return true if this symbol is a scalar, or
     *         false otherwise
     *
     * @see #isTable()
     * @see #isTableRow()
     * @see #isTableColumn()
     * @see net.percederberg.mibble.snmp.SnmpObjectType
     *
     * @since 2.5
     */
    public boolean isScalar() {
        return type instanceof SnmpObjectType
            && !isTable()
            && !isTableRow()
            && !isTableColumn();
    }

    /**
     * Checks if this symbol corresponds to a table. A symbol is
     * considered a table if it has an SnmpObjectType type with
     * SEQUENCE OF syntax.
     *
     * @return true if this symbol is a table, or
     *         false otherwise
     *
     * @see #isScalar()
     * @see #isTableRow()
     * @see #isTableColumn()
     * @see net.percederberg.mibble.snmp.SnmpObjectType
     *
     * @since 2.5
     */
    public boolean isTable() {
        if (type instanceof SnmpObjectType) {
            MibType syntax = ((SnmpObjectType) type).getSyntax();
            return syntax instanceof SequenceOfType;
        } else {
            return false;
        }
    }

    /**
     * Checks if this symbol corresponds to a table row (or entry). A
     * symbol is considered a table row if it has an SnmpObjectType
     * type with SEQUENCE syntax.
     *
     * @return true if this symbol is a table row, or
     *         false otherwise
     *
     * @see #isScalar()
     * @see #isTable()
     * @see #isTableColumn()
     * @see net.percederberg.mibble.snmp.SnmpObjectType
     *
     * @since 2.5
     */
    public boolean isTableRow() {
        if (type instanceof SnmpObjectType) {
            MibType syntax = ((SnmpObjectType) type).getSyntax();
            return syntax instanceof SequenceType;
        } else {
            return false;
        }
    }

    /**
     * Checks if this symbol corresponds to a table column. A symbol
     * is considered a table column if it has an SnmpObjectType type
     * and a parent symbol that is a table row.
     *
     * @return true if this symbol is a table column, or
     *         false otherwise
     *
     * @see #isScalar()
     * @see #isTable()
     * @see #isTableRow()
     * @see net.percederberg.mibble.snmp.SnmpObjectType
     *
     * @since 2.5
     */
    public boolean isTableColumn() {
        MibValueSymbol parent = getParent();
        return type instanceof SnmpObjectType
            && parent != null
            && parent.isTableRow();
    }

    /**
     * Returns the symbol type.
     *
     * @return the symbol type
     */
    public MibType getType() {
        return type;
    }

    /**
     * Returns the symbol value.
     *
     * @return the symbol value
     */
    public MibValue getValue() {
        return value;
    }

    /**
     * Returns the symbol object identifier value (if set). This is a
     * convenience method for getValue() with an additional type
     * check and cast.
     *
     * @return the symbol OID value
     *
     * @since 2.10
     */
    public ObjectIdentifierValue getOid() {
        if (value instanceof ObjectIdentifierValue) {
            return (ObjectIdentifierValue) value;
        } else {
            return null;
        }
    }

    /**
     * Returns the parent symbol in the OID tree. This is a
     * convenience method for value symbols that have object
     * identifier values.
     *
     * @return the parent symbol in the OID tree, or
     *         null for none or if not applicable
     *
     * @see net.percederberg.mibble.value.ObjectIdentifierValue
     *
     * @since 2.5
     */
    public MibValueSymbol getParent() {
        ObjectIdentifierValue oid = getOid();
        ObjectIdentifierValue parent = (oid != null) ? oid.getParent() : null;
        return (parent != null) ? parent.getSymbol() : null;
    }

    /**
     * Returns the number of child symbols in the OID tree. This is a
     * convenience method for value symbols that have object
     * identifier values.
     *
     * @return the number of child symbols in the OID tree, or
     *         zero (0) if not applicable
     *
     * @see net.percederberg.mibble.value.ObjectIdentifierValue
     *
     * @since 2.6
     */
    public int getChildCount() {
        ObjectIdentifierValue oid = getOid();
        return (oid != null) ? oid.getChildCount() : 0;
    }

    /**
     * Returns a specific child symbol in the OID tree. This is a
     * convenience method for value symbols that have object
     * identifier values.
     *
     * @param index          the child position, starting from 0
     *
     * @return the child symbol in the OID tree, or
     *         null if not found or not applicable
     *
     * @see net.percederberg.mibble.value.ObjectIdentifierValue
     *
     * @since 2.6
     */
    public MibValueSymbol getChild(int index) {
        ObjectIdentifierValue oid = getOid();
        ObjectIdentifierValue child = (oid != null) ? oid.getChild(index) : null;
        return (child != null) ? child.getSymbol() : null;
    }

    /**
     * Returns all child symbols in the OID tree. This is a
     * convenience method for value symbols that have object
     * identifier values.
     *
     * @return the array of child symbols in the OID tree, or
     *         an empty array if not applicable
     *
     * @see net.percederberg.mibble.value.ObjectIdentifierValue
     *
     * @since 2.6
     */
    public MibValueSymbol[] getChildren() {
        ObjectIdentifierValue oid = getOid();
        int count = (oid != null) ? oid.getChildCount() : 0;
        MibValueSymbol[] children = new MibValueSymbol[count];
        for (int i = 0; oid != null && i < count; i++) {
            children[i] = oid.getChild(i).getSymbol();
        }
        return children;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("VALUE ");
        buffer.append(getName());
        buffer.append(" ");
        buffer.append(getType());
        buffer.append("\n    ::= ");
        buffer.append(getValue());
        return buffer.toString();
    }
}
