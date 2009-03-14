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
 * Copyright (c) 2004-2008 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;

/**
 * A program that parses and validates a MIB file. If the MIB file(s)
 * specified on the command-line uses constructs or syntax that are
 * not supported, an error message will be printed to the standard
 * output. The program will also return the number of validation
 * failures as its exit code.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
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
        MibLoader  loader = new MibLoader();
        Mib        mib;
        ArrayList  queue = new ArrayList();
        File       file;
        Object     src;
        int        errors = 0;
        int        warnings = 0;

        // Check command-line arguments
        if (args.length < 1) {
            printHelp("No file(s) specified");
            System.exit(1);
        }
        for (int i = 0; i < args.length; i++) {
            try {
                if (args[0].contains(":")) {
                    queue.add(new URL(args[0]));
                } else {
                    file = new File(args[i]);
                    if (!file.exists()) {
                        System.out.println("Warning: Skipping " + args[i] +
                                           ": file not found");
                    } else if (file.isDirectory()) {
                        addMibs(file, queue);
                    } else {
                        queue.add(file);
                    }
                }
            } catch (MalformedURLException e) {
                System.out.println("Warning: Skipping " + args[i] +
                                   ": " + e.getMessage());
            }
        }

        // Parse MIB files
        for (int i = 0; i < queue.size(); i++) {
            src = queue.get(i);
            System.out.print(i);
            System.out.print("/");
            System.out.print(queue.size());
            System.out.print(": Reading " + src + "... ");
            System.out.flush();
            try {
                loader.unloadAll();
                if (src instanceof URL) {
                    loader.removeAllDirs();
                    mib = loader.load((URL) src);
                } else {
                    file = (File) src;
                    if (!loader.hasDir(file.getParentFile())) {
                        loader.removeAllDirs();
                        loader.addDir(file.getParentFile());
                    }
                    mib = loader.load(file);
                }
                System.out.println("[OK]");
                if (mib.getLog().warningCount() > 0) {
                    mib.getLog().printTo(System.out);
                    warnings++;
                }
            } catch (FileNotFoundException e) {
                System.out.println("[FAILED]");
                printError(src.toString(), e);
                errors++;
            } catch (IOException e) {
                System.out.println("[FAILED]");
                printError(src.toString(), e);
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
        System.out.println("Files processed:  " + queue.size());
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

    /**
     * Adds all MIB files from a directory to the specified queue.
     *
     * @param dir            the directory to check
     * @param queue          the queue to add files to
     *
     * @since 2.9
     */
    private static void addMibs(File dir, ArrayList queue) {
        File[]  files = dir.listFiles();

        for (int i = 0; i < files.length; i++) {
            if (files[i].isHidden()) {
                // Hidden file or directories are ignored
            } else if (files[i].isDirectory()) {
                addMibs(files[i], queue);
            } else if (isMib(files[i])) {
                queue.add(files[i]);
            }
        }
    }

    /**
     * Checks if the first lines of a text files looks like a MIB.
     *
     * @param file           the file to check
     *
     * @return true if the file is probably a MIB file, or
     *         false otherwise
     *
     * @since 2.9
     */
    private static boolean isMib(File file) {
        BufferedReader  in = null;
        StringBuffer    buffer = new StringBuffer();
        String          str;
        int             line = 0;

        if (!file.canRead() || !file.isFile()) {
            return false;
        }
        try {
            in = new BufferedReader(new FileReader(file));
            while (line++ < 100 && (str = in.readLine()) != null) {
                buffer.append(str);
            }
        } catch (FileNotFoundException ignore) {
            // Do nothing
        } catch (IOException ignore) {
            // Do nothing
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                    // Do nothing
                }
            }
        }
        return buffer.indexOf("DEFINITIONS") > 0 &&
               buffer.indexOf("::=") > 0 &&
               buffer.indexOf("BEGIN") > 0;
    }
}
