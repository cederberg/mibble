/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.percederberg.mibble.MibDirectory;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibbleBrowser;

/**
 * The MIB browser open MIB dialog.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.10
 */
public class OpenDialog extends JDialog {

    /**
     * The last directory used for opening MIB files.
     */
    public static String lastDir = System.getProperty("user.dir", ".");

    /**
     * The list of MIB modules or files selected for opening.
     */
    public String[] mibs = null;

    /**
     * The MIB tree.
     */
    private JTree tree;

    /**
     * The MIB tree root node.
     */
    private DefaultMutableTreeNode root = new DefaultMutableTreeNode();

    /**
     * Creates new open MIB dialog.
     *
     * @param parent         the parent frame
     * @param browser        the MIB browser app
     */
    public OpenDialog(JFrame parent, MibbleBrowser browser) {
        super(parent, true);
        initialize();
        buildTree(browser);
        setLocationRelativeTo(parent);
    }

    /**
     * Initializes the dialog components.
     */
    private void initialize() {
        JButton             button;
        GridBagConstraints  c;

        // Set dialog title
        setTitle("Load MIB");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        // Add filter label
        JLabel label = new JLabel("Filter:");
        c = new GridBagConstraints();
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(4, 5, 4, 5);
        getContentPane().add(label, c);

        // Add filter field
        final JTextField textField = new JTextField(15);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterTree(textField.getText());
            }
            public void removeUpdate(DocumentEvent e) {
                filterTree(textField.getText());
            }
            public void changedUpdate(DocumentEvent e) {
                filterTree(textField.getText());
            }
        });
        c = new GridBagConstraints();
        c.gridx = 1;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 5, 4, 5);
        getContentPane().add(textField, c);

        // Add tree view
        tree = new JTree();
        ((DefaultTreeModel) tree.getModel()).setRoot(root);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    loadMibs();
                }
            }
        });
        c = new GridBagConstraints();
        c.gridy = 1;
        c.gridwidth = 4;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4, 5, 4, 5);
        getContentPane().add(new JScrollPane(tree), c);

        // Add open file button
        button = new JButton("Import MIB File...");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                openFile();
            }
        });
        c = new GridBagConstraints();
        c.gridy = 2;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(button, c);

        // Add cancel button
        button = new JButton("Cancel");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(button, c);

        // Add load button
        button = new JButton("Load MIB");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadMibs();
            }
        });
        SwingUtilities.getRootPane(this).setDefaultButton(button);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 2;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(button, c);

        // Layout components
        pack();
    }

    /**
     * Adds all MIB modules found to the tree root.
     *
     * @param browser        the MIB browser app
     */
    private void buildTree(MibbleBrowser browser) {
        String[] dirs = browser.loader.getResourceDirs();
        for (int i = 0; i < dirs.length; i++) {
            ArrayList<String> mibs = findResourceFiles(dirs[i] + "/");
            if (mibs != null && mibs.size() > 0) {
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(dirs[i]);
                for (int j = 0; j < mibs.size(); j++) {
                    node.add(new DefaultMutableTreeNode(mibs.get(j)));
                }
                root.add(node);
            }
        }
        MibDirectory mibDir = new MibDirectory(new File(lastDir));
        ArrayList<File> files = new ArrayList<>(mibDir.getContentMap().values());
        if (files.size() > 0) {
            Collections.sort(files);
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(lastDir);
            for (int i = 0; i < files.size(); i++) {
                node.add(new FileTreeNode(files.get(i)));
            }
            root.add(node);
        }
        ((DefaultTreeModel) tree.getModel()).reload();
    }

    /**
     * Modifies the tree to only include matching MIB modules.
     *
     * @param text           the partial MIB name to match
     */
    protected void filterTree(String text) {
        text = text.toUpperCase().trim();
        if (text.isEmpty()) {
            ((DefaultTreeModel) tree.getModel()).setRoot(root);
        } else {
            TreeNode newRoot = filterTreeNode(text, root);
            ((DefaultTreeModel) tree.getModel()).setRoot(newRoot);
            for (int i = 0; i < tree.getRowCount(); i++) {
                tree.expandRow(i);
            }
        }
    }

    /**
     * Creates a copy of a tree node with only matching child nodes.
     *
     * @param text           the partial MIB name to match
     * @param parent         the parent tree node
     *
     * @return a new tree node with only matching children
     */
    private DefaultMutableTreeNode filterTreeNode(String text, TreeNode parent) {
        DefaultMutableTreeNode newParent = new DefaultMutableTreeNode(parent.toString());
        for (int i = 0; i < parent.getChildCount(); i++) {
            TreeNode child = parent.getChildAt(i);
            if (child.getChildCount() > 0) {
                DefaultMutableTreeNode newChild = filterTreeNode(text, child);
                if (newChild.getChildCount() > 0) {
                    newParent.add(newChild);
                }
            } else if (child.toString().toUpperCase().contains(text)) {
                if (child instanceof DefaultMutableTreeNode) {
                    Object obj = ((DefaultMutableTreeNode) child).clone();
                    newParent.add((DefaultMutableTreeNode) obj);
                } else {
                    newParent.add(new DefaultMutableTreeNode(child.toString()));
                }
            }
        }
        return newParent;
    }

    /**
     * Returns a list of resource files (on the classpath).
     *
     * @param prefix         the classpath prefix (path)
     *
     * @return the list of resource files found
     */
    private ArrayList<String> findResourceFiles(String prefix) {
        ArrayList<String> res = new ArrayList<>();
        URL url = MibLoader.class.getClassLoader().getResource(prefix);
        if (url != null) {
            String file = url.toString();
            if (file.startsWith("jar:file:") && file.contains("!")) {
                file = file.substring(9, file.indexOf('!'));
                try {
                    JarFile jar = new JarFile(file);
                    Enumeration<JarEntry> e = jar.entries();
                    while (e.hasMoreElements()) {
                        JarEntry entry = e.nextElement();
                        String name = entry.getName();
                        if (name.startsWith(prefix) && name.length() > prefix.length()) {
                            res.add(name.substring(prefix.length()));
                        }
                    }
                    jar.close();
                } catch (Exception ignore) {
                    // Do nothing
                }
            }
        }
        return res;
    }

    /**
     * Loads the selected MIB modules in the tree.
     */
    protected void loadMibs() {
        ArrayList<String> res = new ArrayList<>();
        TreePath[] paths = tree.getSelectionPaths();
        for (int i = 0; paths != null && i < paths.length; i++) {
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            if (node.isLeaf()) {
                res.add(node.getUserObject().toString());
            }
        }
        if (!res.isEmpty()) {
            this.mibs = res.toArray(new String[res.size()]);
            this.dispose();
        }
    }

    /**
     * Opens the MIB file dialog and returns its result for this
     * dialog as well.
     */
    protected void openFile() {
        FileDialog dialog = new FileDialog(this, "Select MIB File");
        dialog.setDirectory(lastDir);
        dialog.setVisible(true);
        if (dialog.getFile() != null) {
            File file = new File(dialog.getDirectory(), dialog.getFile());
            lastDir = dialog.getDirectory();
            mibs = new String[] { file.getAbsolutePath() };
        }
        this.dispose();
    }


    /**
     * A simple file tree node that only prints the file name.
     */
    private class FileTreeNode extends DefaultMutableTreeNode {

        /**
         * Creates a new file tree node.
         *
         * @param file       the file to show
         */
        public FileTreeNode(File file) {
            super(file);
        }

        /**
         * Clones this object.
         *
         * @return a clone of this object
         */
        public Object clone() {
            return new FileTreeNode(getFile());
        }

        /**
         * Returns the file name (no path).
         *
         * @return the file name
         */
        public String toString() {
            return getFile().getName();
        }

        /**
         * Returns the file.
         *
         * @return the file
         */
        public File getFile() {
            return (File) getUserObject();
        }
    }
}
