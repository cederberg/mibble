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

import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeTag;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

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
     * The SNMP version to use.
     */
    private int version = 1;

    /**
     * The blocked flag.
     */
    public boolean blocked = false;

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
        GridBagConstraints  c;
        DocumentListener    l;

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
        l = new DocumentListener() {
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
     * Sets the OID field text.
     *
     * @param text           the new OID text
     */
    public void setOidText(String text) {
        oidField.setText(text);
    }

    /**
     * Updates the OID field based on the node selected in the frame
     * tree.
     */
    public void updateOidText() {
        MibNode                node = frame.getSelectedNode();
        MibValueSymbol         symbol;
        MibType                type;
        ObjectIdentifierValue  oid;

        if (node == null) {
            setOidText("");
        } else if (node.getSnmpObjectType() == null) {
            setOidText(node.getOid());
        } else {
            symbol = node.getSymbol();
            type = ((SnmpObjectType) symbol.getType()).getSyntax();
            oid = (ObjectIdentifierValue) symbol.getValue();
            symbol = oid.getParent().getSymbol();
            if (type.hasTag(MibTypeTag.UNIVERSAL_CATEGORY, 16)
             || symbol.getType() instanceof SnmpObjectType) {

                setOidText(node.getOid());
            } else {
                setOidText(node.getOid() + ".0");
            }
        }
    }

    /**
     * Updates various panel components, such as text fields and
     * buttons. This method should be called when a new MIB node is
     * selected or when the UI has been blocked or unblocked.
     */
    public void updateStatus() {
        MibNode         node = frame.getSelectedNode();
        SnmpObjectType  type = null;
        boolean         allowOperation;
        boolean         allowGet;
        boolean         allowGetNext;
        boolean         allowSet;

        if (node != null) {
            type = node.getSnmpObjectType();
        }
        allowOperation = !blocked
                      && hostField.getText().length() > 0
                      && portField.getText().length() > 0;
        allowGet = allowOperation
                && type != null
                && type.getAccess().canRead();
        allowGetNext = allowOperation
                    && oidField.getText().length() > 0;
        allowSet = allowOperation
                && type != null
                && type.getAccess().canWrite();
        oidLabel.setEnabled(allowOperation);
        oidField.setEnabled(allowOperation);
        valueLabel.setEnabled(allowSet);
        valueField.setEnabled(allowSet);
        getButton.setEnabled(allowGet);
        getNextButton.setEnabled(allowGetNext);
        setButton.setEnabled(allowSet);
    }

    /**
     * Updates the authentication UI components on change.
     */
    protected void updateAuthentication() {
        boolean  useAuth;
        
        useAuth = authTypeCombo.getSelectedIndex() != 0;
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
     * Performs a set operation.
     */
    protected void performSet() {
        Operation  operation = createOperation(false);

        if (operation != null) {
            operation.startSet();
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
        resultsArea.setCaretPosition(resultsArea.getText().length());
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
        String              host = hostField.getText();
        int                 port;
        String              community = null;
        String              contextName = null;
        String              contextEngine = null;
        String              userName = null;
        String              type;
        String              password;
        SnmpAuthentication  auth = null;
        SnmpPrivacy         privacy = null;
        String              oid = oidField.getText();
        SnmpManager         manager;
        SnmpRequest         request;
        SnmpObjectType      objectType;
        String              value;
        String              message;

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

        // Get SNMP manager parameters
        if (version < 3) {
            if (read) {
                community = new String(readCommunityField.getPassword());
            } else {
                community = new String(writeCommunityField.getPassword());
            }
        } else {
            contextName = contextNameField.getText();
            contextEngine = contextEngineField.getText();
            userName = userNameField.getText();
            if (authTypeCombo.getSelectedIndex() > 0) {
                type = authTypeCombo.getSelectedItem().toString();
                password = new String(authPasswordField.getPassword());
                auth = new SnmpAuthentication(type, password);
            }
            if (privacyTypeCombo.getSelectedIndex() > 0) {
                type = privacyTypeCombo.getSelectedItem().toString();
                password = new String(privacyPasswordField.getPassword());
                privacy = new SnmpPrivacy(type, password);
            }
        }

        // Create SNMP manager
        try {
            if (version == 1) {
                manager = SnmpManager.createSNMPv1(host, port, community);
            } else if (version == 2) {
                manager = SnmpManager.createSNMPv2c(host, port, community);
            } else {
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
        if (read) {
            request = new SnmpRequest(oid);
        } else {
            objectType = frame.getSelectedNode().getSnmpObjectType();
            value = valueField.getText();
            request = new SnmpRequest(oid, objectType.getSyntax(), value);
        }

        return new Operation(manager, request);
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
         * Creates a new background SNMP operation.
         *
         * @param manager        the SNMP manager to use
         * @param request        the request OID, type and value
         */
        public Operation(SnmpManager manager, SnmpRequest request) {
            this.manager = manager;
            this.request = request;
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
         */
        public void startSet() {
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
            SnmpResponse  response = null;
            String        message;

            message = "Performing " + operation + " on " +
                      request.getOid() + "...";
            setOperationStatus(message);
            appendResults(operation + ": ");
            try {
                if (operation.equals("GET")) {
                    response = manager.get(request.getOid());
                } else if (operation.equals("GET NEXT")) {
                    response = manager.getNext(request.getOid());
                } else if (operation.equals("SET")) {
                    response = manager.set(request);
                }
                appendResults(response.getOidsAndValues());
                // TODO: select returned OID in MIB tree
                setOidText(response.getOid(0));
            } catch (SnmpException e) {
                appendResults("Error: ");
                appendResults(e.getMessage());
                appendResults("\n");
            }
            manager.destroy();
            setOperationStatus(null);
        }
    }
}
