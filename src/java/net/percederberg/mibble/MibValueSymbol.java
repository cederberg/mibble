/*
 * MibValueSymbol.java
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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
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
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.8
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
     * @param location       the symbol location
     * @param mib            the symbol MIB file
     * @param name           the symbol name
     * @param type           the symbol type
     * @param value          the symbol value
     *
     * @since 2.2
     */
    public MibValueSymbol(FileLocation location,
                          Mib mib,
                          String name,
                          MibType type,
                          MibValue value) {

        super(location, mib, name);
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
        ObjectIdentifierValue  oid;

        if (type != null) {
            try {
                type = type.initialize(this, log);
            } catch (MibException e) {
                log.addError(e.getLocation(), e.getMessage());
                type = null;
            }
        }
        if (value != null) {
            try {
                value = value.initialize(log, type);
            } catch (MibException e) {
                log.addError(e.getLocation(), e.getMessage());
                value = null;
            }
        }
        if (type != null && value != null && !type.isCompatible(value)) {
            log.addError(getLocation(),
                         "value is not compatible with type");
        }
        if (value instanceof ObjectIdentifierValue) {
            oid = (ObjectIdentifierValue) value;
            if (oid.getSymbol() == null) {
                oid.setSymbol(this);
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
        MibType  syntax;

        if (type instanceof SnmpObjectType) {
            syntax = ((SnmpObjectType) type).getSyntax();
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
        MibType  syntax;

        if (type instanceof SnmpObjectType) {
            syntax = ((SnmpObjectType) type).getSyntax();
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
        MibValueSymbol  parent = getParent();

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
        ObjectIdentifierValue  oid;

        if (value instanceof ObjectIdentifierValue) {
            oid = ((ObjectIdentifierValue) value).getParent();
            if (oid != null) {
                return oid.getSymbol();
            }
        }
        return null;
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
        if (value instanceof ObjectIdentifierValue) {
            return ((ObjectIdentifierValue) value).getChildCount();
        }
        return 0;
    }

    /**
     * Returns a specific child symbol in the OID tree. This is a
     * convenience method for value symbols that have object
     * identifier values. 
     *
     * @param index          the child position, 0 <= index < count
     *
     * @return the child symbol in the OID tree, or
     *         null if not found or not applicable
     *
     * @see net.percederberg.mibble.value.ObjectIdentifierValue
     *
     * @since 2.6
     */
    public MibValueSymbol getChild(int index) {
        ObjectIdentifierValue  oid;

        if (value instanceof ObjectIdentifierValue) {
            oid = ((ObjectIdentifierValue) value).getChild(index);
            if (oid != null) {
                return oid.getSymbol();
            }
        }
        return null;
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
        ObjectIdentifierValue  oid;
        MibValueSymbol         children[];

        if (value instanceof ObjectIdentifierValue) {
            oid = (ObjectIdentifierValue) value;
            children = new MibValueSymbol[oid.getChildCount()];
            for (int i = 0; i < oid.getChildCount(); i++) {
                children[i] = oid.getChild(i).getSymbol();
            }
        } else {
            children = new MibValueSymbol[0];
        }
        return children;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("VALUE ");
        buffer.append(getName());
        buffer.append(" ");
        buffer.append(getType());
        buffer.append("\n    ::= ");
        buffer.append(getValue());
        return buffer.toString();
    }
}
