/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.awt.event.MouseEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpTrapType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * Handles loading MIB files and creating a JTree representation of
 * the parsed MIB. A singleton class.
 *
 * @author   Watsh Rajneesh
 * @author   Per Cederberg
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
    private HashMap<MibSymbol,MibTreeNode> nodes = new HashMap<>();

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
        mibTree.setShowsRootHandles(true);
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
    public MibTreeNode getNode(MibSymbol symbol) {
        return nodes.get(symbol);
    }

    /**
     * Adds a MIB to the MIB tree.
     *
     * @param mib            the MIB to add
     */
    public void addMib(Mib mib) {
        MibTreeNode root = (MibTreeNode) mibTree.getModel().getRoot();
        MibTreeNode mibNode = new MibTreeNode(mib.getName(), mib);
        MibTreeNode valuesNode = new MibTreeNode("VALUES", mib);
        MibTreeNode trapsNode = new MibTreeNode("TRAPS", mib);
        for (MibSymbol symbol : mib.getAllSymbols()) {
            if (symbol instanceof MibValueSymbol) {
                addOid(valuesNode, (MibValueSymbol) symbol);
                addTrap(trapsNode, (MibValueSymbol) symbol);
            }
        }
        mibNode.add(valuesNode);
        if (trapsNode.getChildCount() > 0) {
            mibNode.add(trapsNode);
        }
        root.add(mibNode);
    }

    /**
     * Adds an object identifier value (from the symbol) to a tree
     * node parent. Additional in-between OID tree nodes will be
     * inserted as needed.
     *
     * @param parent         the tree node parent
     * @param symbol         the MIB value symbol
     */
    private void addOid(MibTreeNode parent, MibValueSymbol symbol) {
        MibValue value = symbol.getValue();
        if (value instanceof ObjectIdentifierValue) {
            addOid(parent, (ObjectIdentifierValue) value);
        }
    }

    /**
     * Adds an object identifier value to a tree node parent.
     * Additional in-between OID tree nodes will be inserted as
     * needed.
     *
     * @param parent         the tree node parent
     * @param oid            the object identifier value
     *
     * @return the tree node added (or already existing)
     */
    private MibTreeNode addOid(MibTreeNode parent, ObjectIdentifierValue oid) {
        if (hasMibParent(oid, oid.getMib())) {
            // Add parent OID to tree first
            parent = addOid(parent, oid.getParent());
        }
        MibTreeNode node = parent.findChildByValue(oid);
        if (node == null) {
            String name = oid.getName() + " (" + oid.getValue() + ")";
            node = new MibTreeNode(name, oid);
            parent.add(node);
            if (oid.getSymbol() != null) {
                nodes.put(oid.getSymbol(), node);
            }
        }
        return node;
    }

    /**
     * Adds an SMIv1 TRAP-TYPE to a tree node parent.
     *
     * @param parent         the tree node parent
     * @param symbol         the MIB value symbol
     */
    private void addTrap(MibTreeNode parent, MibValueSymbol symbol) {
        MibType type = symbol.getType();
        if (type instanceof SnmpTrapType) {
            MibTreeNode node = new MibTreeNode(symbol.getName(), symbol);
            parent.add(node);
            nodes.put(symbol, node);
        }
    }

    /**
     * Checks if the specified object identifier has a parent in the
     * same MIB.
     *
     * @param oid            the object identifier to check
     * @param mib            the MIB to check
     *
     * @return true if the object identifier has a parent, or
     *         false otherwise
     */
    private static boolean hasMibParent(ObjectIdentifierValue oid, Mib mib) {
        ObjectIdentifierValue parent = oid.getParent();
        return parent != null && mib != null && mib.equals(parent.getMib());
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
        MibTreeNode root = (MibTreeNode) model.getRoot();
        Enumeration<MibTreeNode> e = root.preorderEnumeration();
        while (e.hasMoreElements()) {
            MibTreeNode tmp = e.nextElement();
            if (tmp.getName().equals(mibName)) {
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
        ((MibTreeNode) mibTree.getModel().getRoot()).removeAllChildren();
    }

    /**
     * Removes descendant nodes from the node hash.
     *
     * @param root           the root node
     */
    private void removeNodes(MibTreeNode root) {
        Iterator<Map.Entry<MibSymbol, MibTreeNode>> iter = nodes.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<MibSymbol, MibTreeNode> entry = iter.next();
            MibTreeNode node = entry.getValue();
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
            super(new MibTreeNode("Mibble Browser", null));
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
            MibTreeNode node = (MibTreeNode) path.getLastPathComponent();
            return node.getToolTipText();
        }
    }
}
