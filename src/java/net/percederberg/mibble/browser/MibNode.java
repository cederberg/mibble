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
 * Copyright (c) 2003 Watsh Rajneesh. All rights reserved.
 */

package net.percederberg.mibble.browser;

import javax.swing.tree.DefaultMutableTreeNode;

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
    private String nodeName;

    /**
     * The MIB node object identifier (oid).
     */
    private String oid = "";

    /**
     * Creates a new MIB tree node.
     *
     * @param nodeName MIB oid name.
     * @param oid Numeric OID string.
     */
    public MibNode(String nodeName, String oid) {
        super(nodeName);
        this.nodeName = nodeName;
        this.oid = oid;
    }

    /** 
     * Returns the node name.
     * 
     * @return the node name
     */
    public String getName() {
        return nodeName;
    }

    /**
     * Returns the MIB object identifier (oid) associated with the 
     * node.
     *
     * @return the node object identifier (oid) 
     */
    public String getOid() {
        return oid;
    }

    /**
     * Removes the braces (if any) from the tree node name.
     *
     * @return a clean node name
     */
    public String getCleanName() {
        if (nodeName.trim().endsWith(")")) {
            return (nodeName.substring(0, nodeName.indexOf('(')));
        }
        return nodeName;
    }
}
