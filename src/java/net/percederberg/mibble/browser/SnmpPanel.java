/*
 * SnmpPanel.java
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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * The SNMP operations panel.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @author   Watsh Rajneesh
 * @version  2.5
 * @since    2.5
 */
public class SnmpPanel extends JPanel {

    /**
     * The default component insets.
     */
    private static final Insets DEFAULT_INSETS = new Insets(2, 5, 2, 5);

    /**
     * The browser frame containing this panel.
     */
    private BrowserFrame frame;

    /**
     * The host IP address field.
     */
    private JTextField ipField = new JTextField("127.0.0.1");

    /**
     * The host port number field.
     */
    private JTextField portField = new JTextField("161");

    /**
     * The read community name field.
     */
    private JPasswordField readCommunityField =
        new JPasswordField("public");

    /**
     * The write community name field.
     */
    private JPasswordField writeCommunityField =
        new JPasswordField("public");

    /**
     * The OID field.
     */
    private JTextField oidField = new JTextField();

    /**
     * The value field.
     */
    private JTextField valueField = new JTextField();

    /**
     * The results text area.
     */
    private JTextArea resultsArea = new JTextArea();

    /**
     * The get button.
     */
    private JButton getButton = new JButton("Get");

    /**
     * The get next button.
     */
    private JButton getNextButton = new JButton("Get Next");

    /**
     * The set button.
     */
    private JButton setButton = new JButton("Set");

    /**
     * The clear button.
     */
    private JButton clearButton = new JButton("Clear");

    /**
     * Creates a new SNMP panel.
     * 
     * @param frame          the frame containing this panel
     */
    public SnmpPanel(BrowserFrame frame) {
        super();
        this.frame = frame;
        initialize();
    }

    /**
     * Initializes the panel components.
     */
    private void initialize() {
        JLabel              label;
        DocumentListener    l;
        GridBagConstraints  c;

        // Adjust panel
        setLayout(new GridBagLayout());

        // Add host IP address field
        label = new JLabel("Host IP Address:");
        label.setLabelFor(ipField);
        c = new GridBagConstraints();
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        add(label, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        add(ipField, c);

        // Add read community field
        label = new JLabel("Read Community:");
        label.setLabelFor(readCommunityField);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        add(label, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        add(readCommunityField, c);

        // Add host port number field
        label = new JLabel("Port Number:");
        label.setLabelFor(portField);
        c = new GridBagConstraints();
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        add(label, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        add(portField, c);

        // Add write community field
        label = new JLabel("Write Community:");
        label.setLabelFor(writeCommunityField);
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        add(label, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 2;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        add(writeCommunityField, c);

        // Add OID field
        label = new JLabel("OID:");
        label.setLabelFor(oidField);
        c = new GridBagConstraints();
        c.gridy = 3;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        add(label, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        add(oidField, c);

        // Add value field
        label = new JLabel("Value:");
        label.setLabelFor(valueField);
        c = new GridBagConstraints();
        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        add(label, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 3;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        add(valueField, c);

        // Add buttons
        c = new GridBagConstraints();
        c.gridy = 5;
        c.gridwidth = 4;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        c.insets = DEFAULT_INSETS;
        add(initializeButtons(), c);

        // Add results area
        c = new GridBagConstraints();
        c.gridy = 6;
        c.gridwidth = 4;
        c.weightx = 0.5d;
        c.weighty = 0.5d;
        c.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(resultsArea), c);

        // Add change listeners
        l = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateButtonStatus();
            }
            public void insertUpdate(DocumentEvent e) {
                updateButtonStatus();
            }
            public void removeUpdate(DocumentEvent e) {
                updateButtonStatus();
            }
        };
        ipField.getDocument().addDocumentListener(l);
        portField.getDocument().addDocumentListener(l);
        oidField.getDocument().addDocumentListener(l);
        valueField.getDocument().addDocumentListener(l);
    }

    /**
     * Creates and initializes the operation buttons.
     *
     * @return the panel containing the buttons
     */
    private JPanel initializeButtons() {
        JPanel  panel = new JPanel();

        panel.setLayout(new FlowLayout());
        getButton.setToolTipText("Perform SNMP get operation");
        getButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performGet();
            }
        });
        panel.add(getButton);
        getNextButton.setToolTipText("Perform SNMP get next operation");
        getNextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performGetNext();
            }
        });
        panel.add(getNextButton);
        setButton.setToolTipText("Perform SNMP set operation");
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSet();
            }
        });
        panel.add(setButton);
        clearButton.setToolTipText("Clear SNMP result area");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearResults();
            }
        });
        panel.add(clearButton);
        updateButtonStatus();
        return panel;
    }

    /**
     * Enables or disables the SNMP operations. This method can be
     * used when performing operations with a long delay to
     * inactivate the user interface.
     *
     * @param enabled        the enabled flag
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        updateButtonStatus();
    }

    /**
     * Sets the OID field text.
     *
     * @param text           the new OID field text
     */
    public void setOidText(String text) {
        oidField.setText(text);
    }

    /**
     * Sets the operation status text. If the status text is null,
     * the operation is assumed to have finished. This method also
     * affects all input controls, making them disabled during an
     * operation.
     *
     * @param text           the status text (or null)
     */
    public void setOperationStatus(String text) {
        frame.setEnabled(text == null);
        frame.setStatus(text);
    }

    /**
     * Enables or disables the buttons. This method can be used while
     * selecting MIB elements that allow or disallows operations.
     */
    protected void updateButtonStatus() {
        boolean  allowGet;
        boolean  allowSet;

        allowGet = ipField.getText().length() > 0
                && portField.getText().length() > 0
                && oidField.getText().length() > 0
                && isEnabled();
        allowSet = allowGet && valueField.getText().length() > 0;
        getButton.setEnabled(allowGet);
        getNextButton.setEnabled(allowGet);
        setButton.setEnabled(allowSet);
    }

    /**
     * Performs a get operation.
     */
    protected void performGet() {
        Operation  operation = createOperation(true);

        if (operation != null) {
            operation.startGet();
        }
    }

    /**
     * Performs a get next operation.
     */
    protected void performGetNext() {
        Operation  operation = createOperation(true);

        if (operation != null) {
            operation.startGetNext();
        }
    }

    /**
     * Performs a set operation.
     */
    protected void performSet() {
        Operation  operation = createOperation(false);

        if (operation != null) {
            operation.startSet(valueField.getText());
        }
    }

    /**
     * Clears the result area.
     */
    protected void clearResults() {
        resultsArea.setText("");
    }

    /**
     * Appends a text to the results area.
     *
     * @param text           the text to append
     */
    protected void appendResults(String text) {
        resultsArea.append(text);
    }

    /**
     * Creates a new operation object. The values in the panel fields
     * will be used.
     *
     * @param read           the read flag (set to use read community)
     *
     * @return the operation object, or
     *         null if none could be created
     */
    private Operation createOperation(boolean read) {
        String       host = ipField.getText();
        int          port;
        String       community;
        String       oid = oidField.getText();
        SnmpManager  manager;
        String       message;

        // Validate port number
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException ignore) {
            port = 0;
        }
        if (port <= 0 || port >= 65536) {
            portField.requestFocus();
            message = "Provide valid (numeric) port number in the " +
                      "range [1..65535].";
            JOptionPane.showMessageDialog(frame,
                                          message,
                                          "Port Number Error",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Get community password
        if (read) {
            community = new String(readCommunityField.getPassword());
        } else {
            community = new String(writeCommunityField.getPassword());
        }

        // Create SNMP manager
        try {
            manager = new SnmpManager(host, port, community);
        } catch (IOException e) {
            message = "SNMP communication error: " + e.getMessage();
            JOptionPane.showMessageDialog(frame,
                                          message,
                                          "SNMP Communication Error",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Return operation
        return new Operation(manager, oid);
    }


    /**
     * A background SNMP operation. This class is needed in order to
     * implement the runnable interface to be able to run SNMP
     * operations in a background thread.
     */
    private class Operation implements Runnable {

        /**
         * The SNMP manager to use.
         */
        private SnmpManager manager;

        /**
         * The object identifier.
         */
        private String oid;

        /**
         * The optional value to set.
         */
        private String value;

        /**
         * The operation to perform.
         */
        private String operation;

        /**
         * Creates a new background SNMP operation.
         *
         * @param manager        the SNMP manager to use
         * @param oid            the object identifier to use
         */
        public Operation(SnmpManager manager, String oid) {
            this.manager = manager;
            this.oid = oid;
        }

        /**
         * Starts a GET operation in a background thread.
         */
        public void startGet() {
            this.operation = "GET";
            start();
        }

        /**
         * Starts a GET NEXT operation in a background thread.
         */
        public void startGetNext() {
            this.operation = "GET NEXT";
            start();
        }

        /**
         * Starts a SET operation in a background thread.
         *
         * @param value          the value to set
         */
        public void startSet(String value) {
            this.value = value;
            this.operation = "SET";
            start();
        }

        /**
         * Starts the background thread.
         */
        private void start() {
            Thread  thread = new Thread(this);

            thread.start();
        }

        /**
         * Runs the operation. This method should only be called by
         * the thread created through a call to start().
         */
        public void run() {
            setOperationStatus("Performing SNMP " + operation +
                               " operation...");
            if (operation.equals("GET")) {
                appendResults("Get: ");
                appendResults(manager.sendGetRequest(oid));
                appendResults("\n");
            } else if (operation.equals("GET NEXT")) {
                appendResults("Get Next: ");
                appendResults(manager.sendGetNextRequest(oid));
                appendResults("\n");
            } else if (operation.equals("SET")) {
                appendResults("Set: ");
                appendResults(manager.sendSetRequest(oid, value));
                appendResults("\n");
            }
            setOperationStatus(null);
        }
    }
}
