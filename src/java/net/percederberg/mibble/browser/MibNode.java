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
 * As a special exception, the copyright holders of this library give
 * you permission to link this library with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is
 * not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 *
 * Copyright (c) 2004 Watsh Rajneesh. All rights reserved.
 */

package net.percederberg.mibble.browser;

import javax.swing.tree.DefaultMutableTreeNode;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A MIB tree node.
 *
 * @author   Watsh Rajneesh
 * @version  2.3
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
     * Returns the full node description.
     *
     * @return the full node description
     */
    public String getDescription() {
        if (value == null) {
            return name;
        } else if (value.getSymbol() != null) {
            return value.getSymbol().toString();
        } else {
            return value.toDetailString();
        }
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
}
