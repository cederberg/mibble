/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
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
import net.percederberg.mibble.MibTypeSymbol;
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
        MibTreeNode typesNode = new MibTreeNode("TYPES", mib);
        for (MibSymbol symbol : mib.getAllSymbols()) {
            if (symbol instanceof MibValueSymbol) {
                MibValueSymbol value = (MibValueSymbol) symbol;
                addTreeNodes(valuesNode, symbol.getMib(), value.getOid());
                if (value.getType() instanceof SnmpTrapType) {
                    MibTreeNode node = new MibTreeNode(symbol.getName(), symbol);
                    trapsNode.add(node);
                    nodes.put(symbol, node);
                }
            } else if (symbol instanceof MibTypeSymbol) {
                MibTreeNode node = new MibTreeNode(symbol.getName(), symbol);
                typesNode.add(node);
                nodes.put(symbol, node);
            }
        }
        if (trapsNode.getChildCount() == 0 && typesNode.getChildCount() == 0) {
            while (valuesNode.getChildCount() > 0) {
                mibNode.add((MibTreeNode) valuesNode.getFirstChild());
            }
        } else {
            if (valuesNode.getChildCount() > 0) {
                mibNode.add(valuesNode);
            }
            if (trapsNode.getChildCount() > 0) {
                mibNode.add(trapsNode);
            }
            if (typesNode.getChildCount() >= 0) {
                mibNode.add(typesNode);
            }
        }
        getRootNode().add(mibNode);
    }

    /**
     * Adds an OID value node to a tree node parent. Additional
     * in-between OID nodes will be inserted as needed to reflect the
     * parent-child relationships between symbols. If the OID has
     * already been added to the tree, the existing node will be
     * returned instead of creating a new one.
     *
     * @param tree           the tree node parent
     * @param mib            the MIB being added
     * @param oid            the OID value
     *
     * @return the MIB tree node created or found, or
     *         null if no tree node was created
     */
    private MibTreeNode addTreeNodes(MibTreeNode tree,
                                     Mib mib,
                                     ObjectIdentifierValue oid) {

        if (oid == null) {
            return null;
        } else if (nodes.containsKey(oid)) {
            return nodes.get(oid);
        } else if (nodes.containsKey(oid.getSymbol())) {
            return nodes.get(oid.getSymbol());
        } else {
            ObjectIdentifierValue parent = oid.getParent();
            if (hasMib(mib, parent)) {
                tree = addTreeNodes(tree, mib, parent);
            }
            String name = oid.getName() + " (" + oid.getValue() + ")";
            MibValueSymbol sym = oid.getSymbol();
            MibTreeNode node = new MibTreeNode(name, (sym != null) ? sym : oid);
            tree.add(node);
            if (sym != null) {
                nodes.put(sym, node);
            }
            return node;
        }
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
     * Checks if an OID value belongs to a MIB.
     *
     * @param mib            the required MIB
     * @param oid            the object identifier to check
     *
     * @return true if the object identifier belongs to the MIB, or
     *         false otherwise
     */
    private static boolean hasMib(Mib mib, ObjectIdentifierValue oid) {
        Mib oidMib = (oid != null) ? oid.getMib() : null;
        if (oidMib != null) {
            return mib == oidMib;
        } else {
            return oid != null && hasMib(mib, oid.getParent());
        }
    }
}
