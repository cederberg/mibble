/*
 * LicenseDialog.java
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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * The MIB browser license dialog.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.5
 */
public class LicenseDialog extends JDialog {

    /**
     * Creates new about dialog.
     *
     * @param parent         the parent frame
     */
    public LicenseDialog(JFrame parent) {
        super(parent, true);
        initialize();
        setLocationRelativeTo(parent);
    }

    /**
     * Initializes the dialog components.
     */
    private void initialize() {
        StringBuffer        license = new StringBuffer();
        URL                 url;
        InputStreamReader   in;
        BufferedReader      reader;
        String              str;
        JTextArea           textArea;
        JButton             button;
        GridBagConstraints  c;

        // Set dialog title
        setTitle("Mibble License");
        setSize(600, 600);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new GridBagLayout());

        // Read license text
        url = getClass().getClassLoader().getResource("LICENSE.txt");
        if (url == null) {
            license.append("Couldn't locate license file (LICENSE.txt).");
        } else {
            try {
                in = new InputStreamReader(url.openStream());
                reader = new BufferedReader(in);
                while ((str = reader.readLine()) != null) {
                    if (!str.equals("\u000C")) {
                        license.append(str);
                    }
                    license.append("\n");
                }
                reader.close();
            } catch (IOException e) {
                license.append("Error reading license file ");
                license.append("(LICENSE.txt):\n\n");
                license.append(e.getMessage());
            }
        }

        // Add license text
        textArea = new JTextArea(license.toString());
        textArea.setEditable(false);
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0d;
        c.weighty = 1.0d;
        c.insets = new Insets(4, 5, 4, 5);
        getContentPane().add(new JScrollPane(textArea), c);

        // Add close button
        button = new JButton("Close");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        c = new GridBagConstraints();
        c.gridy = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 10, 10, 10);
        getContentPane().add(button, c);
    }
}
