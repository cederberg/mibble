/*
 * MibbleBrowser.java
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

package net.percederberg.mibble;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.UIManager;

import net.percederberg.mibble.browser.BrowserFrame;
import net.percederberg.mibble.browser.MibTreeBuilder;

/**
 * A program for browsing MIB files in a GUI.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.3
 */
public class MibbleBrowser {

    /**
     * The command-line help output.
     */
    private static final String COMMAND_HELP =
        "A graphical SNMP MIB file browser. This program comes with\n" +
        "ABSOLUTELY NO WARRANTY; for details see the LICENSE.txt file.\n" +
        "\n" +
        "Syntax: MibbleBrowser [<file(s) or URL(s)>]";

    /**
     * The internal error message.
     */
    private static final String INTERNAL_ERROR =
        "INTERNAL ERROR: An internal error has been found. Please report\n" +
        "    this error to the maintainers (see the web site for\n" +
        "    instructions). Be sure to include the version number, as\n" +
        "    well as the text below:\n";

    /**
     * The list of loaded MIB names.
     */
    // TODO: ultimately remove this list of MIB names, use a
    //       MibLoader instead
    private ArrayList loadedMibs;

    /**
     * The application main entry point.
     *
     * @param args           the command-line parameters
     */
    public static void main(String args[]) {
        MibbleBrowser  browser = new MibbleBrowser();
        BrowserFrame   frame;
        String         str;

        // Check command-line arguments
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                printHelp("No option '" + args[i] + "' exist");
                System.exit(1);
            }
        }

        // Open browser frame
        try {
            str = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(str);
        } catch (Exception e) {
            printInternalError(e);
        }
        frame = new BrowserFrame(browser);
        frame.setVisible(true);

        // Load default MIBs
        if (args.length > 0) {
            frame.setBlocked(true);
            for (int i = 0; i < args.length; i++) {
                frame.loadMib(args[i]);
            }
            frame.refreshTree();
            frame.setBlocked(false);
        }
    }

    /**
     * Prints command-line help information.
     *
     * @param error          an optional error message, or null
     */
    private static void printHelp(String error) {
        System.err.println(COMMAND_HELP);
        System.err.println();
        if (error != null) {
            printError(error);
        }
    }

    /**
     * Prints an internal error message. This type of error should
     * only be reported when run-time exceptions occur, such as null
     * pointer and the likes. All these error should be reported as
     * bugs to the program maintainers.
     *
     * @param e              the exception to be reported
     */
    private static void printInternalError(Exception e) {
        System.err.println(INTERNAL_ERROR);
        e.printStackTrace();
    }

    /**
     * Prints an error message.
     *
     * @param message        the error message
     */
    private static void printError(String message) {
        System.err.print("Error: ");
        System.err.println(message);
    }

    /**
     * Creates a new browser application.
     */
    public MibbleBrowser() {
        loadedMibs = new ArrayList();
    }

    /**
     * Loads MIB file or URL.
     *
     * @param src            the MIB file or URL
     *
     * @throws IOException if the MIB file couldn't be found in the
     *             MIB search path
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     */
    public void loadMib(String src)
        throws IOException, MibLoaderException {

        MibTreeBuilder  mb = MibTreeBuilder.getInstance();
        Mib             mib = null;

        // TODO: handle URLs

        // Loading the specified file
        mib = mb.loadMib(new File(src));

        // Check for already loaded MIB
        for (int i = 0; i < loadedMibs.size(); i++) {
            if (mib.getName().equals(loadedMibs.get(i))) {
                return;
            }
        }

        // Add MIB to tree model
        mb.addMib(mib);
        loadedMibs.add(mib.getName());
    }

    /**
     * Unloads a named MIB.
     *
     * @param name           the MIB name
     */
    public void unloadMib(String name) {
        for (int i = 0; i < loadedMibs.size(); i++) {
            if (name.equals(loadedMibs.get(i))) {
                if (MibTreeBuilder.getInstance().unloadMib(name)) {
                    loadedMibs.remove(i);
                }
                return;
            }
        }
    }
}
