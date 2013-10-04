/*
 * MibTreeBuilder.java
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
 * Copyright (c) 2004-2013 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * Handles loading MIB files and creating a JTree representation of
 * the parsed MIB. A singleton class.
 *
 * @author   Watsh Rajneesh
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.10
 * @since    2.3
 */
public class MibTreeBuilder {

    /**
     * The single class instance.
     */
    private static MibTreeBuilder instance = null;

    /**
     * The root tree component. This acts as a placeholder for
     * attaching multiple MIB sub-trees.
     */
    private JTree mibTree = null;

    /**
     * The MIB node map. This is indexed by the MIB symbol.
     */
    private HashMap<MibSymbol,MibNode> nodes = new HashMap<MibSymbol,MibNode>();

    /**
     * Returns the single instance of this class.
     *
     * @return the one and only instance
     */
    public static MibTreeBuilder getInstance() {
        if (instance == null) {
            instance = new MibTreeBuilder();
        }
        return instance;
    }

    /**
     * Creates a new MIB tree builder.
     */
    private MibTreeBuilder() {
        mibTree = new MibTree();
        mibTree.setToolTipText("");
        int mode = TreeSelectionModel.SINGLE_TREE_SELECTION;
        mibTree.getSelectionModel().setSelectionMode(mode);
        mibTree.setRootVisible(false);
    }

    /**
     * Returns the MIB tree component.
     *
     * @return the MIB tree component
     */
    public JTree getTree() {
        return mibTree;
    }

    /**
     * Returns the MIB node corresponding to the specified symbol.
     *
     * @param symbol         the symbol to search for
     *
     * @return the MIB node, or
     *         null if none found
     */
    public MibNode getNode(MibSymbol symbol) {
        return nodes.get(symbol);
    }

    /**
     * Adds a MIB to the MIB tree.
     *
     * @param mib            the MIB to add
     */
    public void addMib(Mib mib) {

        // Create value sub tree
        MibNode node = new MibNode("VALUES", null);
        JTree valueTree = new JTree(node);
        Iterator<MibSymbol> iter = mib.getAllSymbols().iterator();
        while (iter.hasNext()) {
            MibSymbol symbol = iter.next();
            addSymbol(valueTree.getModel(), symbol);
        }

        // TODO: create TYPES sub tree

        // Add sub tree root to MIB tree
        MibNode root = (MibNode) mibTree.getModel().getRoot();
        node = new MibNode(mib.getName(), null);
        node.add((MibNode) valueTree.getModel().getRoot());
        root.add(node);
    }

    /**
     * Adds a MIB symbol to a MIB tree model.
     *
     * @param model          the MIB tree model
     * @param symbol         the MIB symbol
     *
     * @see #addToTree
     */
    private void addSymbol(TreeModel model, MibSymbol symbol) {
        if (symbol instanceof MibValueSymbol) {
            MibValue value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                ObjectIdentifierValue oid = (ObjectIdentifierValue) value;
                addToTree(model, oid);
            }
        }
    }

    /**
     * Adds an object identifier value to a MIB tree model.
     *
     * @param model          the MIB tree model
     * @param oid            the object identifier value
     *
     * @return the MIB tree node added
     */
    private MibNode addToTree(TreeModel model, ObjectIdentifierValue oid) {

        // Add parent node to tree (if needed)
        MibNode parent = (MibNode) model.getRoot();
        if (hasParent(oid)) {
            parent = addToTree(model, oid.getParent());
        }

        // Check if node already added
        for (int i = 0; i < model.getChildCount(parent); i++) {
            MibNode node = (MibNode) model.getChild(parent, i);
            if (node.getValue().equals(oid)) {
                return node;
            }
        }

        // Create new node
        String name = oid.getName() + " (" + oid.getValue() + ")";
        MibNode node = new MibNode(name, oid);
        parent.add(node);
        nodes.put(oid.getSymbol(), node);
        return node;
    }

    /**
     * Checks if the specified object identifier has a parent.
     *
     * @param oid            the object identifier to check
     *
     * @return true if the object identifier has a parent, or
     *         false otherwise
     */
    private boolean hasParent(ObjectIdentifierValue oid) {
        ObjectIdentifierValue parent = oid.getParent();
        return oid.getSymbol() != null
            && oid.getSymbol().getMib() != null
            && parent != null
            && parent.getSymbol() != null
            && parent.getSymbol().getMib() != null
            && parent.getSymbol().getMib().equals(oid.getSymbol().getMib());
    }

    /**
     * Unloads a MIB.
     *
     * @param mibName        the name of the MIB to unload
     *
     * @return true on success, or
     *         false otherwise
     */
    public boolean unloadMib(String mibName) {
        DefaultTreeModel model = (DefaultTreeModel) mibTree.getModel();
        MibNode root = (MibNode) model.getRoot();
        Enumeration<MibNode> e = root.preorderEnumeration();
        while (e.hasMoreElements()) {
            MibNode tmp = e.nextElement();
            if (tmp.getValue() == null && tmp.getName().equals(mibName)) {
                removeNodes(tmp);
                model.removeNodeFromParent(tmp);
                return true;
            }
        }
        return false;
    }

    /**
     * Unloads all loaded MIB files.
     *
     * @since 2.9
     */
    public void unloadAllMibs() {
        nodes.clear();
        ((MibNode) mibTree.getModel().getRoot()).removeAllChildren();
    }

    /**
     * Removes descendant nodes from the node hash.
     *
     * @param root           the root node
     */
    private void removeNodes(MibNode root) {
        Iterator<Map.Entry<MibSymbol, MibNode>> iter = nodes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<MibSymbol, MibNode> entry = iter.next();
            MibNode node = entry.getValue();
            if (node.isNodeDescendant(root)) {
                iter.remove();
            }
        }
    }


    /**
     * A MIB tree component.
     */
    private class MibTree extends JTree {

        /**
         * Creates a new MIB tree.
         */
        public MibTree() {
            super(new MibNode("Mibble Browser", null));
        }

        /**
         * Returns the tool tip text for a specified mouse event.
         *
         * @param e              the mouse event
         *
         * @return the tool tipe text, or
         *         null for none
         */
        public String getToolTipText(MouseEvent e) {
            if (getRowForLocation(e.getX(), e.getY()) == -1) {
                return null;
            }
            TreePath path = getPathForLocation(e.getX(), e.getY());
            MibNode node = (MibNode) path.getLastPathComponent();
            return node.getToolTipText();
        }
    }
}
