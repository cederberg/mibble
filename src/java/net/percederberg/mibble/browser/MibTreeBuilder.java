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
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;

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
     * The tree tree component.
     */
    public static JTree tree = null;

    /**
     * The root tree node.
     */
    public static MibNode top = null;

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
        // Add default nodes.
        top = new MibNode("iso(1)", ".1");
        tree = new JTree(top);

        MibNode temp = new MibNode("org(3)", "1.3");
        addNodeToTree(temp);
        temp = new MibNode("dod(6)", "1.3.6");
        addNodeToTree(temp);
        temp = new MibNode("internet(1)", "1.3.6.1");
        addNodeToTree(temp);
        temp = new MibNode("private(4)", "1.3.6.1.4");
        addNodeToTree(temp);
        temp = new MibNode("enterprises(1)", "1.3.6.1.4.1");
        addNodeToTree(temp);
        temp = new MibNode("mgmt(2)", "1.3.6.1.2");
        addNodeToTree(temp);
        temp = new MibNode("mib-2(1)", "1.3.6.1.2.1");
        addNodeToTree(temp);
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
     * Code for extracting a map of the symbol names.
     *
     * @param mib            the MIB
     * 
     * @return a map of symbol oid:s to names 
     */
    public HashMap extractObjectIdentifiers(Mib mib) {
        HashMap    map = new HashMap();
        Iterator   iter = mib.getAllSymbols().iterator();
        MibSymbol  symbol;
        MibValue   value;

        while (iter.hasNext()) {
            symbol = (MibSymbol) iter.next();
            value = extractObjectIdentifier(symbol);
            if (value != null) {
                map.put(value.toString(), symbol.getName());
                // TODO: attach to the right parent.
                //pNode = new MibNode(symbol.getName(),value.toString());
                //top.add(pNode);
            }
        }
        return map;
    }

    /**
     * Code for extracting values from symbol.
     *
     * @param symbol         the MIB symbol
     *
     * @return the object identifier (oid) value, or 
     *         null if not present
     */
    public ObjectIdentifierValue extractObjectIdentifier(MibSymbol symbol) {
        MibValue  value;

        if (symbol instanceof MibValueSymbol) {
            value = ((MibValueSymbol) symbol).getValue();
            if (value instanceof ObjectIdentifierValue) {
                return (ObjectIdentifierValue) value;
            }
        }
        return null;
    }

    /**
     * Builds the JTree. The properties are given to a TreeMap, which
     * automatically sorts them. The keys from the TreeMap are used 
     * to create the JTree nodes.
     *
     * @param map            the hash map of oid-name pairs
     * @param mib            the MIB
     */
    public void createSortedTree(HashMap map, Mib mib) {
        TreeMap treeMap = new TreeMap(map);
        
        while (treeMap.size() > 0) {
            String objIdStr = (String) treeMap.firstKey();
            String objName = (String) treeMap.get(objIdStr);
            String nameWithLastOidNumeral = objName + "(" + 
                objIdStr.substring(objIdStr.lastIndexOf('.') + 1,
                                   objIdStr.length()) + 
                ")";
            MibNode nodeToAdd = new MibNode(nameWithLastOidNumeral, objIdStr);
            
            if (!addNodeToTree(nodeToAdd)) {
                System.err.println("Node add failed: " + nodeToAdd.getName());
            }            
            treeMap.remove(objIdStr);
        }
    }
    
    /**
     * Traverses the tree in pre-order and identifies the right place
     * to add a new node.
     *
     * @param nodeToAdd      the node to add
     *
     * @return true if the node was added, or
     *         false otherwise
     */
    private boolean addNodeToTree(MibNode nodeToAdd) {
        TreeModel model = tree.getModel();
        MibNode tempNode = null;
        MibNode root = (MibNode) model.getRoot();
        Enumeration enum = root.preorderEnumeration();
        boolean addedNode = false;
        String objIdStr = nodeToAdd.getOid();
        
        while (enum.hasMoreElements()) {
            tempNode = (MibNode) enum.nextElement();
            String curOid = tempNode.getOid();
   
            if ((objIdStr.substring(0, (objIdStr.lastIndexOf('.')))).equals(curOid)) {
                
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
}
