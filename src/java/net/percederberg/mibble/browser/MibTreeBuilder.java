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
 * Copyright (c) 2003 Watsh Rajneesh. All rights reserved.
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
     * Temporarily used MIB tree component. 
     */
    private JTree subTree = null;

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
     * Code for extracting the symbol names and building the mib 
     * sub-tree corresponding to the current mib.
     *
     * @param mib            the MIB
     * @param subTreeRoot    the root to start the tree from
     */
    public void buildSubTree(Mib mib, MibNode subTreeRoot) {
        Iterator   iter = mib.getAllSymbols().iterator();
        MibSymbol  symbol;

        // Make a new sub tree
        subTree = new JTree(subTreeRoot);

        // Add all symbols to sub tree
        while (iter.hasNext()) {
            symbol = (MibSymbol) iter.next();
            addSymbol(symbol);
        }
        
        // Merge sub tree to mib tree
        MibNode mibTreeRoot = (MibNode) mibTree.getModel().getRoot();
        DefaultTreeModel model = (DefaultTreeModel) mibTree.getModel();
        model.insertNodeInto(subTreeRoot, mibTreeRoot, model.getChildCount(mibTreeRoot));        
    }

    /**
     * Adds a MIB symbol to current subtree.
     *
     * @param symbol         the MIB symbol
     *
     * @see #addToTree
     */
    private void addSymbol(MibSymbol symbol) {
        MibValue               value;
        ObjectIdentifierValue  oid;
        String                 name;

        if (symbol instanceof MibValueSymbol) {
            value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                oid = (ObjectIdentifierValue) value;
                name = oid.getName() + "(" + oid.getValue() + ")"; 
                addNodeToTree(new MibNode(name, oid));
            }            
        }
    }
    
    /**
     * Traverses the tree in pre-order and identifies the right place
     * to add the new node.
     *
     * @param nodeToAdd      the node to add
     *
     * @return true if the node was added, or
     *         false otherwise
     */
    private boolean addNodeToTree(MibNode nodeToAdd) {
        TreeModel model = subTree.getModel();
        MibNode tempNode = null;
        MibNode root = (MibNode) model.getRoot();
        Enumeration enum = root.preorderEnumeration();
        boolean addedNode = false;
        String objIdStr = nodeToAdd.getOid();
        
        while (enum.hasMoreElements()) {
            tempNode = (MibNode) enum.nextElement();
            String curOid = tempNode.getOid();
   
            if (objIdStr.substring(0, objIdStr.lastIndexOf('.')).equals(curOid)) {
                
                int childrenCount = tempNode.getChildCount();
                for (int i = 0; i < childrenCount; i++) {
                    MibNode node = (MibNode) tempNode.getChildAt(i);
                    if (node.getOid().equals(objIdStr)) {
                        return false;
                    }
                }
                tempNode.add(nodeToAdd);
                addedNode = true;
                break;
            }
        }
        if (!addedNode) {
            tempNode.add(nodeToAdd);
            addedNode = true;
        }        
        return true;
    }
    
    /**
     * Unloads a MIB.
     *
     * @param mibName        the name of the MIB to unload
     *
     * @return true on success, or
     *         false otherwise
     */
    public static boolean unloadMib(String mibName) {
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
