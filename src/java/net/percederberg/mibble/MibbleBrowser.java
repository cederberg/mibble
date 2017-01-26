/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.UIManager;

import net.percederberg.mibble.browser.BrowserFrame;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A program for browsing MIB files in a GUI.
 *
 * @author   Per Cederberg
 * @version  2.10
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
     * The application build information properties.
     */
    private Properties buildInfo;

    /**
     * The preferences for this application.
     */
    private Preferences prefs;

    /**
     * The MIB loader to use.
     */
    public MibLoader loader = new MibLoader();

    /**
     * The application main entry point.
     *
     * @param args           the command-line parameters
     */
    public static void main(String args[]) {
        new MibbleBrowser().start(args);
    }

    /**
     * Creates a new browser application.
     */
    public MibbleBrowser() {
        prefs = Preferences.userNodeForPackage(getClass());
    }

    /**
     * Starts this application.
     *
     * @param args           the command-line arguments
     */
    public void start(String[] args) {

        // Check command-line arguments
        for (String arg : args) {
            if (arg.startsWith("-")) {
                printHelp("No option '" + arg + "' exist");
                System.exit(1);
            }
        }

        // Load application build information
        buildInfo = new Properties();
        try {
            buildInfo.load(getClass().getResourceAsStream("build.properties"));
        } catch (IOException ignore) {
            buildInfo.setProperty("build.title", "Mibble");
        }

        // Open browser frame
        try {
            String str = "com.apple.mrj.application.apple.menu.about.name";
            System.setProperty(str, buildInfo.getProperty("build.title"));
            str = UIManager.getSystemLookAndFeelClassName();
            UIManager.setLookAndFeel(str);
        } catch (Exception e) {
            printInternalError(e);
        }
        BrowserFrame frame = new BrowserFrame(this);
        frame.setVisible(true);

        // Load command-line & preference MIBs
        ArrayList<String> list = getFilePrefs();
        removeFilePrefs();
        for (String arg : args) {
            list.add(arg);
        }
        if (list.size() > 0) {
            frame.loadMibsAsync(list.toArray(new String[list.size()]));
        } else {
            frame.loadMibsAsync(new String[] {
                "SNMPv2-SMI",
                "SNMPv2-TC",
                "SNMPv2-MIB",
                "HOST-RESOURCES-MIB"
            });
        }
    }

    /**
     * Prints command-line help information.
     *
     * @param error          an optional error message, or null
     */
    private void printHelp(String error) {
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
    private void printInternalError(Exception e) {
        System.err.println(INTERNAL_ERROR);
        e.printStackTrace();
    }

    /**
     * Prints an error message.
     *
     * @param message        the error message
     */
    private void printError(String message) {
        System.err.print("Error: ");
        System.err.println(message);
    }

    /**
     * Returns the application build information.
     *
     * @return the application build information
     */
    public Properties getBuildInfo() {
        return buildInfo;
    }

    /**
     * Loads MIB file or URL. If the MIB file was already loaded, the
     * previously loaded MIB modules will be returned.
     *
     * @param src            the MIB file or URL
     *
     * @return a collection of the MIB modules found
     *
     * @throws IOException if the MIB file couldn't be found in the
     *             MIB search path
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     */
    public Collection<Mib> loadMib(String src) throws IOException, MibLoaderException {
        Mib mib = null;
        File file = new File(src);
        if (loader.getMib(src) != null) {
            mib = loader.getMib(src);
            addFilePref(src);
        } else if (loader.getMib(file) != null) {
            mib = loader.getMib(file);
            addFilePref(file.getAbsolutePath());
        } else if (file.exists()) {
            if (!loader.hasDir(file.getParentFile())) {
                loader.removeAllDirs();
                loader.addDir(file.getParentFile());
            }
            mib = loader.load(file);
            addFilePref(file.getAbsolutePath());
        } else {
            mib = loader.load(src);
            addFilePref(src);
        }
        return loader.getMibs(mib.getFile()).values();
    }

    /**
     * Unloads a loaded MIB module.
     *
     * @param mib            the MIB module
     */
    public void unloadMib(Mib mib) {
        File file = mib.getFile();
        removeFilePref(file.getAbsolutePath());
        if (!file.exists()) {
            removeFilePref(mib.getName());
        }
        try {
            loader.unload(mib);
        } catch (MibLoaderException ignore) {
            // MIB loader unloading is best-attempt only
        }
    }

    /**
     * Unloads all loaded MIB files.
     *
     * @since 2.9
     */
    public void unloadAllMibs() {
        removeFilePrefs();
        loader.unloadAll();
    }

    /**
     * Searches the OID tree from the loaded MIB files for the best
     * matching value. The returned OID symbol will have the longest
     * matching OID value, but doesn't have to be an exact match. The
     * search requires the full numeric OID value (from the root).
     *
     * @param oid            the numeric OID string to search for
     *
     * @return the best matching OID symbol, or
     *         null if no partial match was found
     *
     * @see MibLoader#getOid(String)
     * @since 2.10
     */
    public MibValueSymbol findMibSymbol(String oid) {
        ObjectIdentifierValue match = loader.getOid(oid);
        return (match == null) ? null : match.getSymbol();
    }

    /**
     * Adds a specified MIB file preference. The file may be either a built-in
     * MIB name or an absolute MIB file path.
     *
     * @param file           the MIB file or name to add
     */
    private void addFilePref(String file) {
        ArrayList<String> list = getFilePrefs();
        if (!list.contains(file)) {
            prefs.put("file" + list.size(), file);
        }
    }

    /**
     * Removes a specified MIB file preference. The file may be either a
     * built-in MIB name or an absolute MIB file path.
     *
     * @param file           the MIB file or name to remove
     */
    private void removeFilePref(String file) {
        ArrayList<String> list = getFilePrefs();
        removeFilePrefs();
        list.remove(file);
        for (int i = 0; i < list.size(); i++) {
            prefs.put("file" + i, list.get(i));
        }
    }

    /**
     * Returns the application MIB file preferences.
     *
     * @return the list of MIB files to load
     */
    private ArrayList<String> getFilePrefs() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String str = prefs.get("file" + i, null);
            if (str != null) {
                list.add(str);
            }
        }
        return list;
    }

    /**
     * Removes all application MIB file preferences.
     */
    private void removeFilePrefs() {
        for (int i = 0; i < 1000; i++) {
            prefs.remove("file" + i);
        }
    }
}
