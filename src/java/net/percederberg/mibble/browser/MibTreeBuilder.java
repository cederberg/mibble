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

import java.io.FileNotFoundException;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * Handles loading MIB files and creating a JTree representation of 
 * the parsed MIB. A singleton class.
 *
 * @author   Watsh Rajneesh
 * @version  2.3
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
    public static JTree mibTree = null;

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
        int  mode = TreeSelectionModel.SINGLE_TREE_SELECTION;

        mibTree = new JTree(new MibNode("Mibble Browser", null));
        mibTree.getSelectionModel().setSelectionMode(mode);
        mibTree.setRootVisible(false);
    }

    /**
     * Loads a MIB file.
     *
     * @param file           the file to load
     * 
     * @return the MIB file loaded
     * 
     * @throws FileNotFoundException if the MIB file couldn't be 
     *             found in the MIB search path
     * @throws MibLoaderException if the MIB file couldn't be loaded 
     *             correctly
     */
    public Mib loadMib(File file)
        throws FileNotFoundException, MibLoaderException {

        MibLoader  loader = new MibLoader();

        loader.addDir(file.getParentFile());
        return loader.load(file);
    }

    /**
     * Adds a MIB to the MIB tree.
     *
     * @param mib            the MIB to add
     */
    public void addMib(Mib mib) {
        Iterator   iter = mib.getAllSymbols().iterator();
        MibSymbol  symbol;
        MibNode    root;
        MibNode    node;
        JTree      valueTree;

        // Create value sub tree
        node = new MibNode("VALUES", null);
        valueTree = new JTree(node);
        while (iter.hasNext()) {
            symbol = (MibSymbol) iter.next();
            addSymbol(valueTree.getModel(), symbol);
        }
        
        // Add sub tree root to MIB tree
        root = (MibNode) mibTree.getModel().getRoot();
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
        MibValue               value;
        ObjectIdentifierValue  oid;

        if (symbol instanceof MibValueSymbol) {
            value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                oid = (ObjectIdentifierValue) value;
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
        MibNode  parent;
        MibNode  node;
        String   name;

        // Add parent node to tree (if needed)
        if (hasParent(oid)) {
            parent = addToTree(model, oid.getParent());
        } else {
            parent = (MibNode) model.getRoot();
        }

        // Check if node already added
        for (int i = 0; i < model.getChildCount(parent); i++) {
            node = (MibNode) model.getChild(parent, i);
            if (node.getValue().equals(oid)) {
                return node;
            }
        }

        // Create new node
        name = oid.getName() + " (" + oid.getValue() + ")"; 
        node = new MibNode(name, oid);
        parent.add(node);
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
        ObjectIdentifierValue  parent = oid.getParent();

        return oid.getSymbol() != null
            && parent != null
            && parent.getSymbol() != null
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
        MibNode tempNode = null;
        MibNode root = (MibNode) model.getRoot();
        Enumeration enum = root.preorderEnumeration();

        while (enum.hasMoreElements()) {
            tempNode = (MibNode) enum.nextElement();

            if (tempNode.getValue() == null && 
                tempNode.getName().equals(mibName)) {

                model.removeNodeFromParent(tempNode);
                return true;
            }
        }
        return false;
    }
}
