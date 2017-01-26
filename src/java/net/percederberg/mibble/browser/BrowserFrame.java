/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.Rectangle;
import java.awt.TextComponent;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.MibbleBrowser;

/**
 * The main MIB browser application window (frame).
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.5
 */
public class BrowserFrame extends JFrame {

    /**
     * The browser application.
     */
    private MibbleBrowser browser;

    /**
     * The menu bar.
     */
    private MenuBar menuBar = new MenuBar();

    /**
     * The SNMP version 1 menu item.
     */
    private CheckboxMenuItem snmpV1Item =
        new CheckboxMenuItem("SNMP version 1");

    /**
     * The SNMP version 2c menu item.
     */
    private CheckboxMenuItem snmpV2Item =
        new CheckboxMenuItem("SNMP version 2c");

    /**
     * The SNMP version 3 menu item.
     */
    private CheckboxMenuItem snmpV3Item =
        new CheckboxMenuItem("SNMP version 3");

    /**
     * The description text area.
     */
    private JTextArea descriptionArea = new JTextArea();

    /**
     * The status label.
     */
    private JLabel statusLabel = new JLabel("Ready");

    /**
     * The MIB tree component.
     */
    private MibTree mibTree = null;

    /**
     * The SNMP operations panel.
     */
    private SnmpPanel snmpPanel = null;

    /**
     * Creates a new Mibble browser frame.
     *
     * @param browser        the browser application
     */
    public BrowserFrame(MibbleBrowser browser) {
        this.browser = browser;
        initialize();
    }

    /**
     * Initializes the frame components.
     */
    private void initialize() {
        GridBagConstraints  c;

        // Set title, size and menus
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("Mibble MIB Browser");
        Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle bounds = new Rectangle();
        bounds.width = (int) (size.width * 0.75);
        bounds.height = (int) (size.height * 0.75);
        bounds.x = (size.width - bounds.width) / 2;
        bounds.y = (size.height - bounds.height) / 2;
        setBounds(bounds);
        setMenuBar(menuBar);
        initializeMenu();
        getContentPane().setLayout(new GridBagLayout());

        // Add horizontal split pane
        JSplitPane horizontalSplitPane = new JSplitPane();
        horizontalSplitPane.setDividerLocation((int) (bounds.width * 0.35));
        c = new GridBagConstraints();
        c.weightx = 1.0d;
        c.weighty = 1.0d;
        c.fill = GridBagConstraints.BOTH;
        getContentPane().add(horizontalSplitPane, c);

        // Add status label
        c = new GridBagConstraints();
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(2, 5, 2, 5);
        getContentPane().add(statusLabel, c);

        // Add MIB tree
        mibTree = new MibTree();
        mibTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent e) {
                updateTreeSelection();
            }
        });
        horizontalSplitPane.setLeftComponent(new JScrollPane(mibTree));

        // Add description area & SNMP panel
        JSplitPane verticalSplitPane = new JSplitPane();
        verticalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        verticalSplitPane.setDividerLocation((int) (bounds.height * 0.40));
        verticalSplitPane.setOneTouchExpandable(true);
        descriptionArea.setEditable(false);
        verticalSplitPane.setLeftComponent(new JScrollPane(descriptionArea));
        snmpPanel = new SnmpPanel(this);
        verticalSplitPane.setRightComponent(snmpPanel);
        horizontalSplitPane.setRightComponent(verticalSplitPane);
    }

    /**
     * Initializes the frame menu.
     */
    private void initializeMenu() {
        Menu      menu;
        MenuItem  item;

        // Create file menu
        menu = new Menu("File");
        item = new MenuItem("Load/Import MIB...", new MenuShortcut(KeyEvent.VK_O));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadMib();
            }
        });
        menu.add(item);
        item = new MenuItem("Unload MIB", new MenuShortcut(KeyEvent.VK_W));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unloadMib();
            }
        });
        menu.add(item);
        item = new MenuItem("Unload All");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unloadAllMibs();
            }
        });
        menu.add(item);
        if (!MacUIHelper.IS_MAC_OS) {
            menu.addSeparator();
            item = new MenuItem("Exit", new MenuShortcut(KeyEvent.VK_Q));
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    quit();
                }
            });
            menu.add(item);
        }
        menuBar.add(menu);

        // Create Edit menu
        menu = new Menu("Edit");
        item = new MenuItem("Cut", new MenuShortcut(KeyEvent.VK_X));
        item.addActionListener(new DefaultEditorKit.CutAction());
        menu.add(item);
        item = new MenuItem("Copy", new MenuShortcut(KeyEvent.VK_C));
        item.addActionListener(new DefaultEditorKit.CopyAction());
        menu.add(item);
        item = new MenuItem("Paste", new MenuShortcut(KeyEvent.VK_V));
        item.addActionListener(new DefaultEditorKit.PasteAction());
        menu.add(item);
        item = new MenuItem("Select All", new MenuShortcut(KeyEvent.VK_A));
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager kfm =
                    KeyboardFocusManager.getCurrentKeyboardFocusManager();
                Component comp = kfm.getFocusOwner();
                if (comp instanceof TextComponent) {
                    ((TextComponent) comp).selectAll();
                } else if (comp instanceof JTextComponent) {
                    ((JTextComponent) comp).selectAll();
                }
            }
        });
        menu.add(item);
        menuBar.add(menu);

        // Create SNMP menu
        menu = new Menu("SNMP");
        snmpV1Item.setState(true);
        snmpV1Item.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setSnmpVersion(1);
            }
        });
        menu.add(snmpV1Item);
        snmpV2Item.setState(false);
        snmpV2Item.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setSnmpVersion(2);
            }
        });
        menu.add(snmpV2Item);
        snmpV3Item.setState(false);
        snmpV3Item.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setSnmpVersion(3);
            }
        });
        menu.add(snmpV3Item);
        menu.addSeparator();
        CheckboxMenuItem checkBox = new CheckboxMenuItem("Show result in tree");
        checkBox.setState(true);
        checkBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                setSnmpFeedback(e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        menu.add(checkBox);
        menuBar.add(menu);

        // Create help menu
        menu = new Menu("Help");
        item = new MenuItem("License...");
        item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showLicense();
            }
        });
        menu.add(item);
        if (!MacUIHelper.IS_MAC_OS) {
            item = new MenuItem("About...");
            item.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    showAbout();
                }
            });
            menu.add(item);
        }
        menuBar.add(menu);

        // Fix Mac OS specific menus
        if (MacUIHelper.IS_MAC_OS) {
            @SuppressWarnings("unused")
            Object tmp = new MacUIHelper(this);
        }
    }

    /**
     * Blocks or unblocks GUI operations in this frame. This method
     * is used when performing long-running operations to inactivate
     * the user interface.
     *
     * @param blocked        the blocked flag
     */
    public void setBlocked(boolean blocked) {
        for (int i = 0; i < menuBar.getMenuCount(); i++) {
            menuBar.getMenu(i).setEnabled(!blocked);
        }
        snmpPanel.setBlocked(blocked);
    }

    /**
     * Opens the load MIB dialog.
     */
    protected void loadMib() {
        OpenDialog dialog = new OpenDialog(this, browser);
        dialog.setVisible(true);
        loadMibsAsync(dialog.mibs);
    }

    /**
     * Loads a MIB file from a specified source.
     *
     * @param src            the MIB file or URL
     *
     * @return true if the MIB loaded successfully, or
     *         false otherwise
     */
    protected boolean loadMib(String src) {
        String message = null;
        setStatus("Loading " + src + "...");
        try {
            for (Mib mib : browser.loadMib(src)) {
                if (mibTree.getRootNode().findChildByValue(mib) == null) {
                    mibTree.addTreeNodes(mib);
                }
            }
        } catch (FileNotFoundException e) {
            message = "Failed to load " + e.getMessage();
        } catch (IOException e) {
            message = "Failed to load " + src + ": " + e.getMessage();
        } catch (MibLoaderException e) {
            message = "Failed to load " + src;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            e.getLog().printTo(new PrintStream(output));
            descriptionArea.append(output.toString());
        }
        if (message != null) {
            JOptionPane.showMessageDialog(this,
                                          message,
                                          "MIB Loading Error",
                                          JOptionPane.ERROR_MESSAGE);
        }
        setStatus(null);
        return message == null;
    }

    /**
     * Loads one or more MIB modules or files in the background.
     *
     * @param srcs           the MIB modules, files or URLs
     */
    public void loadMibsAsync(String[] srcs) {
        if (srcs != null && srcs.length > 0) {
            descriptionArea.setText("");
            new Loader(srcs).start();
        }
    }

    /**
     * Unloads the MIB file from the currently selected symbol.
     */
    protected void unloadMib() {
        MibTreeNode node = getSelectedNode();
        Mib mib = (node != null) ? node.getMib() : null;
        if (mib != null) {
            browser.unloadMib(mib);
            mibTree.removeTreeNodes(mib);
            refreshTree(false);
        }
    }

    /**
     * Unloads all MIB files currently loaded.
     */
    protected void unloadAllMibs() {
        browser.unloadAllMibs();
        mibTree.removeAllTreeNodes();
        refreshTree(false);
    }

    /**
     * Refreshes the MIB tree.
     *
     * @param selectAdded    the select added rows flag
     */
    protected void refreshTree(boolean selectAdded) {
        for (int i = mibTree.getRowCount() - 1; i >= 0; i--) {
            mibTree.collapseRow(i);
        }
        int rows = mibTree.getRowCount();
        ((DefaultTreeModel) mibTree.getModel()).reload();
        mibTree.repaint();
        if (selectAdded && mibTree.getRowCount() > rows) {
            mibTree.setSelectionRow(rows);
        }
    }

    /**
     * Sets the SNMP version to use.
     *
     * @param version        the new version number
     */
    public void setSnmpVersion(int version) {
        snmpV1Item.setState(false);
        snmpV2Item.setState(false);
        snmpV3Item.setState(false);
        if (version == 1) {
            snmpV1Item.setState(true);
        } else if (version == 2) {
            snmpV2Item.setState(true);
        } else if (version == 3) {
            snmpV3Item.setState(true);
        }
        snmpPanel.setVersion(version);
    }

    /**
     * Sets the SNMP feedback flag.
     *
     * @param feedback       the feedback flag
     */
    public void setSnmpFeedback(boolean feedback) {
        snmpPanel.setFeedback(feedback);
    }

    /**
     * Returns the currently selected MIB node.
     *
     * @return the currently selected MIB node, or
     *         null for none
     */
    public MibTreeNode getSelectedNode() {
        return (MibTreeNode) mibTree.getLastSelectedPathComponent();
    }

    /**
     * Sets the selected node based on the specified OID. The MIB
     * that will be searched is based on the currently selected MIB
     * node.
     *
     * @param oid            the OID to select
     */
    public void setSelectedNode(String oid) {

        // Find tree node
        MibValueSymbol symbol = browser.findMibSymbol(oid);
        MibTreeNode node = mibTree.getTreeNode(symbol);
        if (node == null) {
            mibTree.clearSelection();
            return;
        }

        // Select tree node
        TreePath path = new TreePath(node.getPath());
        mibTree.expandPath(path);
        mibTree.scrollPathToVisible(path);
        mibTree.setSelectionPath(path);
        mibTree.repaint();
    }

    /**
     * Sets the status label text.
     *
     * @param text           the status label text (or null)
     */
    public void setStatus(String text) {
        if (text != null) {
            statusLabel.setText(text);
        } else {
            statusLabel.setText("Ready");
        }
    }

    /**
     * Shows the about dialog.
     */
    protected void showAbout() {
        AboutDialog dialog = new AboutDialog(this, browser.getBuildInfo());
        dialog.setVisible(true);
    }

    /**
     * Shows the license dialog.
     */
    protected void showLicense() {
        LicenseDialog dialog = new LicenseDialog(this);
        dialog.setVisible(true);
    }

    /**
     * Quits this application.
     */
    protected void quit() {
        System.exit(0);
    }

    /**
     * Updates the tree selection.
     */
    protected void updateTreeSelection() {
        MibTreeNode node = getSelectedNode();
        if (node == null) {
            descriptionArea.setText("");
        } else {
            descriptionArea.setText(node.getDescription());
            descriptionArea.setCaretPosition(0);
        }
        snmpPanel.updateOid();
    }


    /**
     * A background MIB loader. This class is needed in order to
     * implement the runnable interface to be able to load MIB
     * modules in a background thread.
     */
    private class Loader implements Runnable {

        /**
         * The MIB modules or files to load.
         */
        private String[] mibs;

        /**
         * Creates a new background MIB loader.
         *
         * @param mibs           the MIB modules or files to load
         */
        public Loader(String[] mibs) {
            this.mibs = mibs;
        }

        /**
         * Starts the background loading thread.
         */
        public void start() {
            if (mibs.length > 0) {
                new Thread(this).start();
            }
        }

        /**
         * Runs the MIB loading. This method should only be called by
         * the thread created through a call to start().
         */
        public void run() {
            boolean success = true;
            setBlocked(true);
            for (int i = 0; i < mibs.length; i++) {
                success = success && loadMib(mibs[i]);
            }
            refreshTree(success);
            setBlocked(false);
        }
    }
}
