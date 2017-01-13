/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import javax.swing.tree.DefaultMutableTreeNode;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A MIB tree node. This is an extension to the default tree nodes in
 * order to provide separate name and value for a node in the tree.
 * It also provides helpers for extracting relevant data from the node
 * value.
 *
 * @see MibTree
 *
 * @author   Per Cederberg
 * @author   Watsh Rajneesh
 * @version  2.10
 * @since    2.10
 */
public class MibTreeNode extends DefaultMutableTreeNode {

    /**
     * The tree node name.
     */
    private String name;

    /**
     * The tree node value.
     */
    private Object value;

    /**
     * Creates a new MIB tree node.
     *
     * @param name           the node name
     * @param value          the node value
     */
    public MibTreeNode(String name, Object value) {
        super(name);
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the node name.
     *
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the node value.
     *
     * @return the node value, or
     *         null if no value is present
     */
    public Object getValue() {
        return value;
    }

    /**
     * Returns the MIB for this node. The MIB is available either for
     * the MIB root nodes or via the MIB value symbol for an OID node.
     *
     * @return the MIB for this node, or
     *         null for none
     */
    public Mib getMib() {
        if (value instanceof Mib) {
            return (Mib) value;
        } else if (value instanceof MibValueSymbol) {
            return ((MibValueSymbol) value).getMib();
        } else if (value instanceof ObjectIdentifierValue) {
            return ((ObjectIdentifierValue) value).getMib();
        } else {
            return null;
        }
    }

    /**
     * Returns the MIB value symbol for this node. The symbol is only
     * available for OID nodes with a corresponding symbol set.
     *
     * @return the MIB value symbol, or
     *         null for none
     */
    public MibValueSymbol getSymbol() {
        if (value instanceof MibValueSymbol) {
            return (MibValueSymbol) value;
        } else {
            ObjectIdentifierValue oid = getOid();
            return (oid == null) ? null : oid.getSymbol();
        }
    }

    /**
     * Returns the object identifier value (OID) for this node.
     *
     * @return the object identifier value, or
     *         null for none
     */
    public ObjectIdentifierValue getOid() {
        if (value instanceof ObjectIdentifierValue) {
            return (ObjectIdentifierValue) value;
        } else {
            return null;
        }
    }

    /**
     * Returns the SNMP object type for this node. The object type is
     * only set for OID nodes with a corresponding symbol having such
     * a type.
     *
     * @return the SNMP object type for this node, or
     *         null for none
     */
    public SnmpObjectType getSnmpObjectType() {
        MibValueSymbol symbol = getSymbol();
        if (symbol != null && symbol.getType() instanceof SnmpObjectType) {
            return (SnmpObjectType) symbol.getType();
        } else {
            return null;
        }
    }

    /**
     * Returns the detailed node description.
     *
     * @return the detailed node description, or
     *         an empty string if not available
     */
    public String getDescription() {
        MibValueSymbol symbol = getSymbol();
        Mib mib = getMib();
        if (symbol != null) {
            return symbol.getText();
        } else if (mib != null) {
            return mib.getText();
        } else {
            return "";
        }
    }

    /**
     * Returns the tool tip text for this node.
     *
     * @return the tool tip text for this node, or
     *         null if not available
     */
    public String getToolTipText() {
        MibValueSymbol symbol = getSymbol();
        if (symbol != null) {
            MibType type = symbol.getType();
            if (type instanceof SnmpType) {
                String str = ((SnmpType) type).getDescription();
                if (str.indexOf('.') > 0) {
                    str = str.substring(0, str.indexOf('.') + 1);
                }
                if (str.length() > 150) {
                    str = str.substring(0, 150) + "...";
                }
                return str.replaceAll("[ \t\r\n]+", " ");
            }
        }
        return null;
    }

    /**
     * Searches for the first child with the specified value.
     *
     * @param value          the value to search for
     *
     * @return the tree node child found, or
     *         null for none
     */
    public MibTreeNode findChildByValue(Object value) {
        for (int i = 0; i < getChildCount(); i++) {
            MibTreeNode child = (MibTreeNode) getChildAt(i);
            if (child.value != null && child.value.equals(value)) {
                return child;
            }
        }
        return null;
    }
}
