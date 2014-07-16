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
 * Copyright (c) 2004-2013 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.percederberg.mibble.snmp.SnmpObjectType;

/**
 * The SNMP operations panel.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @author   Watsh Rajneesh
 * @version  2.10
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
     * The SNMP version to use.
     */
    private int version = 1;

    /**
     * The feedback flag. When this is set, the frame tree will be
     * updated with the results of the SNMP operations.
     */
    private boolean feedback = true;

    /**
     * The blocked flag.
     */
    private boolean blocked = false;

    /**
     * The currently ongoing SNMP operation.
     */
    private Operation operation = null;

    /**
     * The SNMP field panel.
     */
    private JPanel fieldPanel = new JPanel();

    /**
     * The host IP address label.
     */
    private JLabel hostLabel = new JLabel("Host IP Address;");

    /**
     * The host IP address field.
     */
    private JTextField hostField = new JTextField("127.0.0.1");

    /**
     * The host port number label.
     */
    private JLabel portLabel = new JLabel("Port Number:");

    /**
     * The host port number field.
     */
    private JTextField portField = new JTextField();

    /**
     * The read community name label.
     */
    private JLabel readCommunityLabel = new JLabel("Read Community:");

    /**
     * The read community name field.
     */
    private JPasswordField readCommunityField =
        new JPasswordField("public");

    /**
     * The write community name label.
     */
    private JLabel writeCommunityLabel =
        new JLabel("Write Community:");

    /**
     * The write community name field.
     */
    private JPasswordField writeCommunityField =
        new JPasswordField("public");

    /**
     * The context name label.
     */
    private JLabel contextNameLabel = new JLabel("Context Name:");

    /**
     * The context name field.
     */
    private JTextField contextNameField = new JTextField("");

    /**
     * The context engine label.
     */
    private JLabel contextEngineLabel = new JLabel("Context Engine:");

    /**
     * The context engine field.
     */
    private JTextField contextEngineField = new JTextField("");

    /**
     * The user name label.
     */
    private JLabel userNameLabel = new JLabel("User Name:");

    /**
     * The user name field.
     */
    private JTextField userNameField = new JTextField("initial");

    /**
     * The authentication type label.
     */
    private JLabel authTypeLabel = new JLabel("Authentication:");

    /**
     * The authentication type combo box.
     */
    private JComboBox authTypeCombo = new JComboBox();

    /**
     * The authentication password label.
     */
    private JLabel authPasswordLabel = new JLabel("Auth. Password:");

    /**
     * The authentication password field.
     */
    private JPasswordField authPasswordField =
        new JPasswordField("public");

    /**
     * The privacy type label.
     */
    private JLabel privacyTypeLabel = new JLabel("Privacy:");

    /**
     * The privacy type combo box.
     */
    private JComboBox privacyTypeCombo = new JComboBox();

    /**
     * The privacy password label.
     */
    private JLabel privacyPasswordLabel =
        new JLabel("Privacy Password:");

    /**
     * The privacy password field.
     */
    private JPasswordField privacyPasswordField =
        new JPasswordField("public");

    /**
     * The OID label.
     */
    private JLabel oidLabel = new JLabel("OID:");

    /**
     * The OID field.
     */
    private JTextField oidField = new JTextField();

    /**
     * The value label.
     */
    private JLabel valueLabel = new JLabel("Value:");

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
     * The get all button.
     */
    private JButton getAllButton = new JButton("Get All");

    /**
     * The set button.
     */
    private JButton setButton = new JButton("Set");

    /**
     * The stop button.
     */
    private JButton stopButton = new JButton("Stop");

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
        GridBagConstraints  c;

        // Component initialization
        setLayout(new GridBagLayout());
        fieldPanel.setLayout(new GridBagLayout());
        portField.setText(String.valueOf(SnmpManager.DEFAULT_PORT));
        authTypeCombo.addItem("None");
        authTypeCombo.addItem(SnmpAuthentication.MD5_TYPE);
        authTypeCombo.addItem(SnmpAuthentication.SHA1_TYPE);
        privacyTypeCombo.addItem("None");
        privacyTypeCombo.addItem(SnmpPrivacy.CBC_DES_TYPE);

        // Associate labels
        hostLabel.setLabelFor(hostField);
        portLabel.setLabelFor(portField);
        readCommunityLabel.setLabelFor(readCommunityField);
        writeCommunityLabel.setLabelFor(writeCommunityField);
        contextNameLabel.setLabelFor(contextNameField);
        contextEngineLabel.setLabelFor(contextEngineField);
        userNameLabel.setLabelFor(userNameField);
        authTypeLabel.setLabelFor(authTypeCombo);
        authPasswordLabel.setLabelFor(authPasswordField);
        privacyTypeLabel.setLabelFor(privacyTypeCombo);
        privacyPasswordLabel.setLabelFor(privacyPasswordField);
        oidLabel.setLabelFor(oidField);
        valueLabel.setLabelFor(valueField);

        // Add SNMP fields
        initializeSnmpV1FieldPanel();
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        add(fieldPanel, c);

        // Add buttons
        c = new GridBagConstraints();
        c.gridy = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        add(initializeButtons(), c);

        // Add results area
        resultsArea.setEditable(false);
        c = new GridBagConstraints();
        c.gridy = 2;
        c.weightx = 1.0d;
        c.weighty = 1.0d;
        c.fill = GridBagConstraints.BOTH;
        add(new JScrollPane(resultsArea), c);

        // Add authentication and privacy listeners
        authTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateAuthentication();
            }
        });
        privacyTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updatePrivacy();
            }
        });
        updateAuthentication();

        // Add text change listeners
        DocumentListener l = new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                updateStatus();
            }
            public void insertUpdate(DocumentEvent e) {
                updateStatus();
            }
            public void removeUpdate(DocumentEvent e) {
                updateStatus();
            }
        };
        hostField.getDocument().addDocumentListener(l);
        portField.getDocument().addDocumentListener(l);
        oidField.getDocument().addDocumentListener(l);
    }

    /**
     * Initializes the field panel for SNMP version 1.
     */
    private void initializeSnmpV1FieldPanel() {
        GridBagConstraints  c;

        // Clear panel
        fieldPanel.removeAll();
        fieldPanel.invalidate();

        // Add host IP address field
        c = new GridBagConstraints();
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(hostLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(hostField, c);

        // Add read community field
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(readCommunityLabel, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(readCommunityField, c);

        // Add host port number field
        c = new GridBagConstraints();
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(portLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(portField, c);

        // Add write community field
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(writeCommunityLabel, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 2;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(writeCommunityField, c);

        // Add separator
        c = new GridBagConstraints();
        c.gridy = 3;
        c.gridwidth = 4;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(new JSeparator(), c);

        // Add OID field
        c = new GridBagConstraints();
        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(oidLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.gridwidth = 3;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(oidField, c);

        // Add value field
        c = new GridBagConstraints();
        c.gridy = 5;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(valueLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        c.gridwidth = 3;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(valueField, c);
    }

    /**
     * Initializes the field panel for SNMP version 3.
     */
    private void initializeSnmpV3FieldPanel() {
        GridBagConstraints  c;

        // Clear panel
        fieldPanel.removeAll();

        // Add host IP address field
        c = new GridBagConstraints();
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(hostLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(hostField, c);

        // Add context name field
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 1;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(contextNameLabel, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 1;
        c.weightx = 0.2d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(contextNameField, c);

        // Add host port number field
        c = new GridBagConstraints();
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(portLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(portField, c);

        // Add context engine field
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 2;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(contextEngineLabel, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 2;
        c.weightx = 0.2d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(contextEngineField, c);

        // Add user name field
        c = new GridBagConstraints();
        c.gridy = 3;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(userNameLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 3;
        c.gridwidth = 3;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(userNameField, c);

        // Add authentication type field
        c = new GridBagConstraints();
        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(authTypeLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(authTypeCombo, c);

        // Add authentication password field
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 4;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(authPasswordLabel, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 4;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(authPasswordField, c);

        // Add privacy type field
        c = new GridBagConstraints();
        c.gridy = 5;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(privacyTypeLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 5;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(privacyTypeCombo, c);

        // Add privacy password field
        c = new GridBagConstraints();
        c.gridx = 2;
        c.gridy = 5;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(privacyPasswordLabel, c);
        c = new GridBagConstraints();
        c.gridx = 3;
        c.gridy = 5;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(privacyPasswordField, c);

        // Add separator
        c = new GridBagConstraints();
        c.gridy = 6;
        c.gridwidth = 4;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(new JSeparator(), c);

        // Add OID field
        c = new GridBagConstraints();
        c.gridy = 7;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(oidLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 7;
        c.gridwidth = 3;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(oidField, c);

        // Add value field
        c = new GridBagConstraints();
        c.gridy = 8;
        c.fill = GridBagConstraints.BOTH;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(valueLabel, c);
        c = new GridBagConstraints();
        c.gridx = 1;
        c.gridy = 8;
        c.gridwidth = 3;
        c.weightx = 0.1d;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = DEFAULT_INSETS;
        fieldPanel.add(valueField, c);
    }

    /**
     * Creates and initializes the operation buttons.
     *
     * @return the panel containing the buttons
     */
    private JPanel initializeButtons() {
        JPanel panel = new JPanel();
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
        getAllButton.setToolTipText("Walk an OID branch and " +
                                       "retrieve all values");
        getAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performGetAll();
            }
        });
        panel.add(getAllButton);
        setButton.setToolTipText("Perform SNMP set operation");
        setButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performSet();
            }
        });
        panel.add(setButton);
        stopButton.setToolTipText("Stops the SNMP operation");
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                performStop();
            }
        });
        panel.add(stopButton);
        clearButton.setToolTipText("Clear SNMP result area");
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearResults();
            }
        });
        panel.add(clearButton);
        updateStatus();
        return panel;
    }

    /**
     * Sets the SNMP version to use.
     *
     * @param version        the new version number
     */
    public void setVersion(int version) {
        this.version = version;
        if (version == 1 || version == 2) {
            initializeSnmpV1FieldPanel();
        } else if (version == 3) {
            initializeSnmpV3FieldPanel();
        }
        validate();
    }

    /**
     * Sets the SNMP operation feedback flag. When this flag is set,
     * the result of the SNMP operation will update the MIB tree
     * selection.
     *
     * @param feedback       the feedback flag
     */
    public void setFeedback(boolean feedback) {
        this.feedback = feedback;
    }

    /**
     * Blocks or unblocks GUI operations in this panel. This method
     * is used when performing long-running operations to inactivate
     * the user interface.
     *
     * @param blocked        the blocked flag
     */
    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
        updateStatus();
    }

    /**
     * Updates the OID field based on the node selected in the frame
     * tree.
     */
    public void updateOid() {
        MibNode node = frame.getSelectedNode();
        if (node == null) {
            oidField.setText("");
        } else if (node.getSymbol() != null
                && node.getSymbol().isScalar()) {

            oidField.setText(node.getOid() + ".0");
        } else {
            oidField.setText(node.getOid());
        }
    }

    /**
     * Updates the OID field with the specified OID. Also updates the
     * frame selection to the closest matching node.
     *
     * @param text           the new OID text
     */
    public void updateOid(String text) {
        frame.setSelectedNode(text);
        oidField.setText(text);
    }

    /**
     * Updates the value field with the specified value.
     *
     * @param value          the new value
     */
    public void updateValue(String value) {
        valueField.setText(value);
    }

    /**
     * Updates various panel components, such as text fields and
     * buttons. This method should be called when a new MIB node is
     * selected or when the UI has been blocked or unblocked.
     */
    public void updateStatus() {
        SnmpObjectType type = null;
        MibNode node = frame.getSelectedNode();
        if (node != null) {
            type = node.getSnmpObjectType();
        }
        boolean allowOperation = !blocked
                                 && hostField.getText().length() > 0
                                 && portField.getText().length() > 0;
        boolean allowGet = allowOperation
                           && oidField.getText().length() > 0;
        boolean allowSet = allowOperation
                           && type != null
                           && type.getAccess().canWrite();
        oidLabel.setEnabled(allowOperation);
        oidField.setEnabled(allowOperation);
        valueLabel.setEnabled(allowSet);
        valueField.setEnabled(allowSet);
        getButton.setEnabled(allowGet);
        getNextButton.setEnabled(allowGet);
        getAllButton.setEnabled(allowGet);
        setButton.setEnabled(allowSet);
        stopButton.setEnabled(operation != null);
    }

    /**
     * Updates the authentication UI components on change.
     */
    protected void updateAuthentication() {
        boolean useAuth = authTypeCombo.getSelectedIndex() != 0;
        authPasswordLabel.setEnabled(useAuth);
        authPasswordField.setEnabled(useAuth);
        privacyTypeLabel.setEnabled(useAuth);
        privacyTypeCombo.setEnabled(useAuth);
        if (!useAuth) {
            privacyTypeCombo.setSelectedIndex(0);
        }
    }

    /**
     * Updates the privacy UI components on change.
     */
    protected void updatePrivacy() {
        boolean  usePrivacy;

        usePrivacy = privacyTypeCombo.getSelectedIndex() != 0;
        privacyPasswordLabel.setEnabled(usePrivacy);
        privacyPasswordField.setEnabled(usePrivacy);
    }

    /**
     * Sets the operation status text. If the status text is null,
     * the operation is assumed to have finished. This method also
     * affects all input controls, making them disabled during an
     * operation.
     *
     * @param text           the status text (or null)
     */
    protected void setOperationStatus(String text) {
        synchronized (this) {
            if (text == null) {
                operation = null;
            }
        }
        frame.setBlocked(text != null);
        frame.setStatus(text);
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
     * Performs a get all operation.
     */
    protected void performGetAll() {
        operation = createOperation(true);
        if (operation != null) {
            operation.startGetAll();
        }
    }

    /**
     * Performs a set operation.
     */
    protected void performSet() {
        Operation  operation = createOperation(false);

        if (operation != null) {
            operation.startSet();
        }
    }

    /**
     * Stops the current operation.
     */
    protected void performStop() {
        synchronized (this) {
            if (operation != null) {
                operation.stop();
            }
        }
    }

    /**
     * Clears the result area.
     */
    protected void clearResults() {
        synchronized (this) {
            resultsArea.setText("");
        }
    }

    /**
     * Appends a text to the results area.
     *
     * @param text           the text to append
     */
    protected void appendResults(String text) {
        synchronized (this) {
            resultsArea.append(text);
            resultsArea.setCaretPosition(resultsArea.getText().length());
        }
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

        // Validate port number
        String host = hostField.getText();
        int port = 0;
        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException ignore) {
            // Do nothing here
        }
        if (port <= 0 || port >= 65536) {
            portField.requestFocus();
            String msg = "Provide valid (numeric) port number in the " +
                         "range [1..65535].";
            JOptionPane.showMessageDialog(frame,
                                          msg,
                                          "Port Number Error",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Create SNMP manager
        SnmpManager manager = null;
        try {
            if (version < 3) {
                String community = null;
                if (read) {
                    community = new String(readCommunityField.getPassword());
                } else {
                    community = new String(writeCommunityField.getPassword());
                }
                if (version == 1) {
                    manager = SnmpManager.createSNMPv1(host, port, community);
                } else {
                    manager = SnmpManager.createSNMPv2c(host, port, community);
                }
            } else {
                String contextName = contextNameField.getText();
                String contextEngine = contextEngineField.getText();
                String userName = userNameField.getText();
                SnmpAuthentication auth = null;
                if (authTypeCombo.getSelectedIndex() > 0) {
                    String type = authTypeCombo.getSelectedItem().toString();
                    String password = new String(authPasswordField.getPassword());
                    auth = new SnmpAuthentication(type, password);
                }
                SnmpPrivacy privacy = null;
                if (privacyTypeCombo.getSelectedIndex() > 0) {
                    String type = privacyTypeCombo.getSelectedItem().toString();
                    String password = new String(privacyPasswordField.getPassword());
                    privacy = new SnmpPrivacy(type, password);
                }
                manager = SnmpManager.createSNMPv3(host,
                                                   port,
                                                   contextName,
                                                   contextEngine,
                                                   userName,
                                                   auth,
                                                   privacy);
            }
        } catch (SnmpException e) {
            JOptionPane.showMessageDialog(frame,
                                          e.getMessage(),
                                          "SNMP Error",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // Create request
        SnmpRequest request = null;
        String oid = oidField.getText();
        if (read) {
            request = new SnmpRequest(oid);
        } else {
            SnmpObjectType type = frame.getSelectedNode().getSnmpObjectType();
            String value = valueField.getText();
            request = new SnmpRequest(oid, type.getSyntax(), value);
        }

        return new Operation(manager, request, feedback);
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
         * The SNMP request object.
         */
        private SnmpRequest request;

        /**
         * The operation to perform.
         */
        private String operation;

        /**
         * The result feedback flag.
         */
        private boolean feedback;

        /**
         * The thread stopped flag.
         */
        private boolean stopped = false;

        /**
         * Creates a new background SNMP operation.
         *
         * @param manager        the SNMP manager to use
         * @param request        the request OID, type and value
         * @param feedback       the feedback flag
         */
        public Operation(SnmpManager manager,
                         SnmpRequest request,
                         boolean feedback) {

            this.manager = manager;
            this.request = request;
            this.feedback = feedback;
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
         * Starts a GET ALL operation in a background thread.
         */
        public void startGetAll() {
            this.operation = "GET ALL";
            start();
        }

        /**
         * Starts a SET operation in a background thread.
         */
        public void startSet() {
            this.operation = "SET";
            start();
        }

        /**
         * Starts the background thread.
         */
        private void start() {
            new Thread(this).start();
        }

        /**
         * Stops the background thread.
         */
        public void stop() {
            stopped = true;
        }

        /**
         * Runs the operation. This method should only be called by
         * the thread created through a call to start().
         */
        public void run() {
            String msg = "Performing " + operation + " on " +
                         request.getOid() + "...";
            setOperationStatus(msg);
            try {
                if (operation.equals("GET ALL")) {
                    runGetAll();
                } else {
                    appendResults(operation + ": ");
                    SnmpResponse response = null;
                    if (operation.equals("GET")) {
                        response = manager.get(request.getOid());
                    } else if (operation.equals("GET NEXT")) {
                        response = manager.getNext(request.getOid());
                    } else if (operation.equals("SET")) {
                        response = manager.set(request);
                    } else {
                        throw new SnmpException("Unknown operation: " +
                                                operation);
                    }
                    appendResults(response.getOidsAndValues());
                    if (feedback) {
                        updateOid(response.getOid(0));
                        updateValue(response.getValue(0));
                    }
                }
            } catch (SnmpException e) {
                appendResults("Error: ");
                appendResults(e.getMessage());
                appendResults("\n");
            }
            manager.destroy();
            setOperationStatus(null);
        }

        /**
         * Runs the get all operation.
         *
         * @throws SnmpException if an error occurred during the
         *             operation
         */
        private void runGetAll() throws SnmpException {
            String oid = request.getOid();
            do {
                SnmpResponse response = manager.getNext(oid);
                oid = response.getOid(0);
                if (oid.startsWith(request.getOid())) {
                    appendResults("GET NEXT: ");
                    appendResults(response.getOidsAndValues());
                    if (feedback) {
                        updateOid(response.getOid(0));
                        updateValue(response.getValue(0));
                        try {
                            Thread.sleep(250);
                        } catch (InterruptedException ignore) {
                            // Do nothing if interrupted
                        }
                    }
                } else {
                    appendResults("DONE: no more values for " +
                                  request.getOid() + "\n");
                    stopped = true;
                }
            } while (!stopped);
        }
    }
}
