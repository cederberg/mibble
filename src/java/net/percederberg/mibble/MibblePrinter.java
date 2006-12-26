/*
 * MibblePrinter.java
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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
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
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.7
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
     * The MIB oid tree printing mode.
     */
    private static final int OID_PRINT_MODE = 1;

    /**
     * The MIB debug printing mode.
     */
    private static final int DEBUG_PRINT_MODE = 2;

    /**
     * The application main entry point.
     *
     * @param args           the command-line parameters
     */
    public static void main(String[] args) {
        MibLoader  loader = new MibLoader();
        int        printMode = MIB_PRINT_MODE;
        Mib        mib = null;
        int        pos = 0;
        File       file;
        URL        url;

        // Check command-line arguments
        if (args.length < 1) {
            printHelp("No MIB file or URL specified");
            System.exit(1);
        } else if (args[0].startsWith("--") && args.length < 2) {
            printHelp("No MIB file or URL specified");
            System.exit(1);
        }
        if (args[0].equals("--mib")) {
            printMode = MIB_PRINT_MODE;
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
        try {
            for (; pos < args.length; pos++) {
                try {
                    url = new URL(args[pos]);
                } catch (MalformedURLException e) {
                    url = null;
                }
                if (url == null) {
                    file = new File(args[pos]);
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
        Mib[]  mibs = loader.getAllMibs();

        for (int i = 0; i < mibs.length; i++) {
            if (mibs[i].isLoaded()) {
                if (printMode == MIB_PRINT_MODE) {
                    printMib(mibs[i]);
                } else {
                    printDebug(mibs[i]);
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
        MibWriter  os = new MibWriter(System.out);

        os.print(mib);
        System.out.println();
        System.out.println();
    }

    /**
     * Prints the contents of a single MIB in debug mode.
     *
     * @param mib            the MIB to print
     */
    private static void printDebug(Mib mib) {
        Iterator  iter;

        iter = mib.getAllSymbols().iterator();
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
        Mib                    mib;
        ObjectIdentifierValue  root = null;
        Iterator               iter;
        MibSymbol              symbol;
        MibValue               value;

        if (loader.getAllMibs().length <= 0) {
            printError("no MIB modules have been loaded");
            return;
        }
        mib = loader.getAllMibs()[0];
        iter = mib.getAllSymbols().iterator();
        while (root == null && iter.hasNext()) {
            symbol = (MibSymbol) iter.next();
            if (symbol instanceof MibValueSymbol) {
                value = ((MibValueSymbol) symbol).getValue();
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
        StringBuffer  buffer = new StringBuffer();

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
        StringBuffer  buffer = new StringBuffer();

        buffer.append("couldn't open URL:\n    ");
        buffer.append(url);
        printError(buffer.toString());
    }
}
