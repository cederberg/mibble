/*
 * TreeListener.java
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

import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

/**
 * A MIB tree listener.
 *
 * @author   Watsh Rajneesh
 * @version  2.3
 * @since    2.3
 */
public class TreeListener implements TreeSelectionListener {

    /**
     * The tree component.
     */
    private JTree tree;

    /**
     * The oid text field. 
     */
    private JTextField oid;

    /**
     * The MIB description text area.
     */
    private JTextArea mibDescription;

    /**
     * Creates a new tree listener.
     * 
     * @param tree           the tree component
     * @param oid            the OID text field
     * @param mibDescription the MIB description text area
     */
    public TreeListener(JTree tree,
                        JTextField oid,
                        JTextArea mibDescription) {

        this.tree = tree;
        this.oid = oid;
        this.mibDescription = mibDescription;
    }

    /**
     * Handles tree selection events.
     *
     * @param e              the tree selection event
     */
    public void valueChanged(TreeSelectionEvent e) {
        MibNode  node = (MibNode) tree.getLastSelectedPathComponent();

        if (node == null) {
            return;
        } 
        oid.setText(node.getOid());
        mibDescription.setText(node.getDescription());
    }
}
