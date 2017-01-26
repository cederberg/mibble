/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;

import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A program that parses and prints a MIB file. If the MIB file(s)
 * specified on the command-line uses constructs or syntax that are
 * not supported, an error message will be printed to the standard
 * output.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class MibblePrinter {

    /**
     * The command-line help output.
     */
    private static final String COMMAND_HELP =
        "Prints the contents of an SNMP MIB file. This program comes with\n" +
        "ABSOLUTELY NO WARRANTY; for details see the LICENSE.txt file.\n" +
        "\n" +
        "Syntax: MibblePrinter [--mib|--oid|--debug] <file(s) or URL(s)>\n" +
        "\n" +
        "    --mib     Prints a formatted and indented version of the MIB.\n" +
        "              This is the default printing mode.\n" +
        "    --mibtree Prints a tree of all loaded MIBs, showing every MIB\n" +
        "              import statement\n" +
        "    --oid     Prints the complete OID tree, including all nodes\n" +
        "              in imported MIB files\n" +
        "    --debug   Prints the MIB contents in debug format, which will\n" +
        "              display all values completely resolved.";

    /**
     * The internal error message.
     */
    private static final String INTERNAL_ERROR =
        "INTERNAL ERROR: An internal error has been found. Please report\n" +
        "    this error to the maintainers (see the web site for\n" +
        "    instructions). Be sure to include the version number, as\n" +
        "    well as the text below:\n";

    /**
     * The MIB pretty printing mode.
     */
    private static final int MIB_PRINT_MODE = 0;

    /**
     * The MIB tree printing mode.
     */
    private static final int MIBTREE_PRINT_MODE = 1;

    /**
     * The MIB oid tree printing mode.
     */
    private static final int OID_PRINT_MODE = 2;

    /**
     * The MIB debug printing mode.
     */
    private static final int DEBUG_PRINT_MODE = 3;

    /**
     * The application main entry point.
     *
     * @param args           the command-line parameters
     */
    public static void main(String[] args) {

        // Check command-line arguments
        if (args.length < 1) {
            printHelp("No MIB file or URL specified");
            System.exit(1);
        } else if (args[0].startsWith("--") && args.length < 2) {
            printHelp("No MIB file or URL specified");
            System.exit(1);
        }
        int printMode = MIB_PRINT_MODE;
        int pos = 0;
        if (args[0].equals("--mib")) {
            printMode = MIB_PRINT_MODE;
            pos++;
        } else if (args[0].equals("--mibtree")) {
            printMode = MIBTREE_PRINT_MODE;
            pos++;
        } else if (args[0].equals("--oid")) {
            printMode = OID_PRINT_MODE;
            pos++;
        } else if (args[0].equals("--debug")) {
            printMode = DEBUG_PRINT_MODE;
            pos++;
        } else if (args[0].startsWith("--")) {
            printHelp("No option '" + args[0] + "' exist");
            System.exit(1);
        }

        // Parse the MIB files
        MibLoader loader = new MibLoader();
        try {
            for (; pos < args.length; pos++) {
                URL url = null;
                try {
                    url = new URL(args[pos]);
                } catch (MalformedURLException e) {
                    // Ignore error
                }
                Mib mib = null;
                if (url == null) {
                    File file = new File(args[pos]);
                    loader.addDir(file.getParentFile());
                    mib = loader.load(file);
                } else {
                    mib = loader.load(url);
                }
                if (mib.getLog().warningCount() > 0) {
                    mib.getLog().printTo(System.err);
                }
            }
        } catch (FileNotFoundException e) {
            printError(args[pos], e);
            System.exit(1);
        } catch (IOException e) {
            printError(args[pos], e);
            System.exit(1);
        } catch (MibLoaderException e) {
            e.getLog().printTo(System.err);
            System.exit(1);
        } catch (RuntimeException e) {
            printInternalError(e);
            System.exit(1);
        }

        // Print loaded MIBs
        if (printMode == OID_PRINT_MODE) {
            printOidTree(loader);
        } else {
            printMibs(loader, printMode);
        }
    }

    /**
     * Prints the contents of all MIBs in a MIB loader.
     *
     * @param loader         the MIB loader
     * @param printMode      the print mode to use
     */
    private static void printMibs(MibLoader loader, int printMode) {
        for (Mib mib : loader.getAllMibs()) {
            if (mib.isLoaded()) {
                if (printMode == MIB_PRINT_MODE) {
                    printMib(mib);
                } if (printMode == MIBTREE_PRINT_MODE) {
                    printImports(mib, "");
                } else {
                    printDebug(mib);
                }
            }
        }
    }

    /**
     * Prints the contents of a single MIB in pretty printing mode.
     *
     * @param mib            the MIB to print
     */
    private static void printMib(Mib mib) {
        MibWriter os = new MibWriter(System.out);
        os.print(mib);
        System.out.println();
        System.out.println();
    }

    /**
     * Prints the MIB import tree.
     *
     * @param mib            the MIB to print
     * @param prefix         the indent prefix
     */
    private static void printImports(Mib mib, String prefix) {
        System.out.print(prefix);
        System.out.println(mib.getName());
        if (prefix.length() >= 4) {
            boolean isLast = prefix.endsWith(" \u2517\u2501 ");
            prefix = prefix.substring(0, prefix.length() - 4);
            prefix += isLast ? "    " : " \u2503  ";
        }
        Iterator<MibImport> iter = mib.getAllImports().iterator();
        while (iter.hasNext()) {
            MibImport imp = iter.next();
            String branch = iter.hasNext() ? " \u2523\u2501 " : " \u2517\u2501 ";
            printImports(imp.getMib(), prefix + branch);
        }
    }

    /**
     * Prints the contents of a single MIB in debug mode.
     *
     * @param mib            the MIB to print
     */
    private static void printDebug(Mib mib) {
        Iterator<MibSymbol> iter = mib.getAllSymbols().iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
            System.out.println();
        }
        System.out.println();
        System.out.println();
    }

    /**
     * Prints the complete OID tree. All MIB modules loaded with the
     * specified MIB loader will be printed.
     *
     * @param loader            the MIB loader
     */
    private static void printOidTree(MibLoader loader) {
        if (loader.getAllMibs().length <= 0) {
            printError("no MIB modules have been loaded");
            return;
        }
        Mib mib = loader.getAllMibs()[0];
        ObjectIdentifierValue root = null;
        Iterator<MibSymbol> iter = mib.getAllSymbols().iterator();
        while (root == null && iter.hasNext()) {
            MibSymbol symbol = iter.next();
            if (symbol instanceof MibValueSymbol) {
                MibValue value = ((MibValueSymbol) symbol).getValue();
                if (value instanceof ObjectIdentifierValue) {
                    root = (ObjectIdentifierValue) value;
                }
            }
        }
        if (root == null) {
            printError("no OID value could be found in " + mib.getName());
        } else {
            while (root.getParent() != null) {
                root = root.getParent();
            }
            printOid(root);
        }
    }

    /**
     * Prints the detailed OID tree starting in the specified OID.
     *
     * @param oid            the OID node to print
     */
    private static void printOid(ObjectIdentifierValue oid) {
        System.out.println(oid.toDetailString());
        for (int i = 0; i < oid.getChildCount(); i++) {
            printOid(oid.getChild(i));
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
     * Prints a file not found error message.
     *
     * @param file           the file name not found
     * @param e              the detailed exception
     */
    private static void printError(String file, FileNotFoundException e) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("couldn't open file:\n    ");
        buffer.append(file);
        printError(buffer.toString());
    }

    /**
     * Prints a URL not found error message.
     *
     * @param url            the URL not found
     * @param e              the detailed exception
     */
    private static void printError(String url, IOException e) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("couldn't open URL:\n    ");
        buffer.append(url);
        printError(buffer.toString());
    }
}
