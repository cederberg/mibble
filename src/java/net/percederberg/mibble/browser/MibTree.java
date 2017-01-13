/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JTree;
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
 * A MIB tree component. This extends the default JTree component
 * to simplify creation of a MIB tree containing only MibTreeNode
 * elements.
 *
 * @see MibTreeNode
 *
 * @author   Per Cederberg
 * @author   Watsh Rajneesh
 * @version  2.10
 * @since    2.10
 */
public class MibTree extends JTree {

    /**
     * The MIB node map. This is indexed by the MIB symbol and
     * enabled quick lookup of tree nodes by MIB symbol.
     */
    private HashMap<MibSymbol,MibTreeNode> nodes = new HashMap<>();

    /**
     * Creates a new MIB tree.
     */
    public MibTree() {
        super(new MibTreeNode("MIB Tree Root", null));
        setRootVisible(false);
        setShowsRootHandles(true);
        setToolTipText("");
        getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * Returns the (invisible) MIB tree root node.
     *
     * @return the MIB tree root node
     */
    public MibTreeNode getRootNode() {
        return (MibTreeNode) getModel().getRoot();
    }

    /**
     * Returns the MIB tree node corresponding to a symbol.
     *
     * @param symbol         the symbol to search for
     *
     * @return the MIB tree node, or
     *         null if none found
     */
    public MibTreeNode getTreeNode(MibSymbol symbol) {
        return nodes.get(symbol);
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

    /**
     * Adds tree nodes corresponding a MIB to the MIB tree.
     *
     * @param mib            the MIB to add
     */
    public void addTreeNodes(Mib mib) {
        MibTreeNode mibNode = new MibTreeNode(mib.getName(), mib);
        MibTreeNode valuesNode = new MibTreeNode("VALUES", mib);
        MibTreeNode trapsNode = new MibTreeNode("TRAPS", mib);
        for (MibSymbol symbol : mib.getAllSymbols()) {
            if (symbol instanceof MibValueSymbol) {
                addOid(valuesNode, (MibValueSymbol) symbol);
                addTrap(trapsNode, (MibValueSymbol) symbol);
            }
        }
        if (trapsNode.getChildCount() > 0) {
            mibNode.add(valuesNode);
            mibNode.add(trapsNode);
        } else {
            while (valuesNode.getChildCount() > 0) {
                mibNode.add((MibTreeNode) valuesNode.getFirstChild());
            }
        }
        getRootNode().add(mibNode);
    }

    /**
     * Removes all tree nodes from the MIB tree (except the root).
     */
    public void removeAllTreeNodes() {
        nodes.clear();
        getRootNode().removeAllChildren();
    }

    /**
     * Adds tree nodes corresponding a MIB to the MIB tree.
     *
     * @param mib            the MIB to add
     */
    public void removeTreeNodes(Mib mib) {
        MibTreeNode mibNode = getRootNode().findChildByValue(mib);
        if (mibNode != null) {
            mibNode.removeFromParent();
        }
        ArrayList<MibSymbol> symbols = new ArrayList<>();
        for (MibSymbol symbol : nodes.keySet()) {
            if (mib.equals(symbol.getMib())) {
                symbols.add(symbol);
            }
        }
        nodes.keySet().removeAll(symbols);
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
}
