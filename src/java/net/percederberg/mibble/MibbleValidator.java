/*
 * MibbleValidator.java
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * A program that parses and validates a MIB file. If the MIB file(s)
 * specified on the command-line uses constructs or syntax that are
 * not supported, an error message will be printed to the standard
 * output. The program will also return the number of validation
 * failures as its exit code.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.3
 * @since    2.0
 */
public class MibbleValidator {

    /**
     * The command-line help output.
     */
    private static final String COMMAND_HELP =
        "Validates a set of SNMP MIB files. This program comes with\n" +
        "ABSOLUTELY NO WARRANTY; for details see the LICENSE.txt file.\n" +
        "\n" +
        "Syntax: MibbleValidator <file(s) or URL(s)>";

    /**
     * The internal error message.
     */
    private static final String INTERNAL_ERROR =
        "INTERNAL ERROR: An internal error has been found. Please report\n" +
        "    this error to the maintainers (see the web site for\n" +
        "    instructions). Be sure to include the version number, as\n" +
        "    well as the text below:\n";

    /**
     * The application main entry point.
     *
     * @param args           the command-line parameters
     */
    public static void main(String[] args) {
        MibLoader  loader;
        Mib        mib;
        File       file;
        URL        url;
        int        errors = 0;
        int        warnings = 0;

        // Check command-line arguments
        if (args.length < 1) {
            printHelp("No file(s) specified");
            System.exit(1);
        }

        // Parse MIB files
        for (int i = 0; i < args.length; i++) {
            try {
                System.out.print("Reading " + args[i] + "... ");
                System.out.flush();
                try {
                    url = new URL(args[0]);
                } catch (MalformedURLException e) {
                    url = null;
                }
                loader = new MibLoader();
                if (url == null) {
                    file = new File(args[i]);
                    loader.addDir(file.getParentFile());
                    mib = loader.load(file);
                } else {
                    mib = loader.load(url);
                }
                System.out.println("[OK]");
                if (mib.getLog().warningCount() > 0) {
                    mib.getLog().printTo(System.out);
                    warnings++;
                }
            } catch (FileNotFoundException e) {
                System.out.println("[FAILED]");
                printError(args[i], e);
                errors++;
            } catch (IOException e) {
                System.out.println("[FAILED]");
                printError(args[i], e);
                errors++;
            } catch (MibLoaderException e) {
                System.out.println("[FAILED]");
                e.getLog().printTo(System.out);
                errors++;
            } catch (RuntimeException e) {
                System.out.println();
                printInternalError(e);
                System.exit(1);
            }
        }

        // Print error count
        System.out.println();
        System.out.println("Files processed:  " + args.length);
        System.out.println("  with errors:    " + errors);
        System.out.println("  with warnings:  " + warnings);
        if (errors > 0) {
            System.err.println("Error: validation errors were encountered");
        }

        // Return error count
        System.exit(errors);
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
            System.err.print("Error: ");
            System.err.println(error);
            System.err.println();
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
     * Prints a file not found error message.
     *
     * @param file           the file name not found
     * @param e              the detailed exception
     */
    private static void printError(String file, FileNotFoundException e) {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("Error: couldn't open file:");
        buffer.append("\n    ");
        buffer.append(file);
        System.out.println(buffer.toString());
    }

    /**
     * Prints a URL not found error message.
     *
     * @param url            the URL not found
     * @param e              the detailed exception
     */
    private static void printError(String url, IOException e) {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("Error: couldn't open URL:");
        buffer.append("\n    ");
        buffer.append(url);
        System.out.println(buffer.toString());
    }
}
