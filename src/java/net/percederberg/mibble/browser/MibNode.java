/*
 * MibNode.java
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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import javax.swing.tree.DefaultMutableTreeNode;

import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A MIB tree node.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @author   Watsh Rajneesh
 * @version  2.5
 * @since    2.3
 */
public class MibNode extends DefaultMutableTreeNode {

    /**
     * The MIB node name.
     */
    private String name;

    /**
     * The MIB node object identifier (oid) value.
     */
    private ObjectIdentifierValue value;

    /**
     * Creates a new MIB tree node.
     *
     * @param name           the node name
     * @param value          the node object identifier value
     */
    public MibNode(String name, ObjectIdentifierValue value) {
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
     * Returns the node object identifier value.
     *
     * @return the node object identifier value, or
     *         null if no value is present
     */
    public ObjectIdentifierValue getValue() {
        return value;
    }

    /**
     * Returns the object identifier (oid) associated with the node.
     *
     * @return the node object identifier (oid), or
     *         an empty string if no object identifier is present
     */
    public String getOid() {
        if (value == null) {
            return "";
        } else {
            return value.toString();
        }
    }

    /**
     * Returns the MIB value symbol for this node.
     *
     * @return the MIB value symbol, or
     *         null for none
     */
    public MibValueSymbol getSymbol() {
        if (value == null) {
            return null;
        } else {
            return value.getSymbol();
        }
    }

    /**
     * Returns the SNMP object type of the current node symbol.
     *
     * @return the SNMP object type of the current node symbol, or
     *         null if non-existent
     */
    public SnmpObjectType getSnmpObjectType() {
        MibValueSymbol  symbol = getSymbol();
        
        if (symbol != null && symbol.getType() instanceof SnmpObjectType) {
            return (SnmpObjectType) symbol.getType();
        } else {
            return null;
        }
    }

    /**
     * Returns the detailed node description.
     *
     * @return the detailed node description
     */
    public String getDescription() {
        if (value != null  && value.getSymbol() != null) {
            return value.getSymbol().toString();
        } else {
            return "";
        }
    }

    /**
     * Returns the tool tip text for this node.
     *
     * @return the tool tip text for this node.
     */
    public String getToolTipText() {
        MibType  type;
        String   str;

        if (value != null  && value.getSymbol() != null) {
            type = value.getSymbol().getType();
            if (type instanceof SnmpType) {
                str = ((SnmpType) type).getDescription();
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
}
