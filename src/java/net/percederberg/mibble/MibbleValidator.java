/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
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
 * @author   Per Cederberg
 * @version  2.10
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

        // Check command-line arguments
        if (args.length < 1) {
            printHelp("No file(s) specified");
            System.exit(1);
        }
        ArrayList<Object> queue = new ArrayList<>();
        for (String arg : args) {
            try {
                if (arg.contains("://")) {
                    queue.add(new URL(arg));
                } else {
                    File file = new File(arg);
                    if (!file.exists()) {
                        System.out.println("Warning: Skipping " + arg +
                                           ": file not found");
                    } else if (file.isDirectory()) {
                        addMibs(file, queue);
                    } else {
                        queue.add(file);
                    }
                }
            } catch (MalformedURLException e) {
                System.out.println("Warning: Skipping " + arg +
                                   ": " + e.getMessage());
            }
        }

        // Parse MIB files
        MibLoader loader = new MibLoader();
        int errors = 0;
        int warnings = 0;
        for (int i = 0; i < queue.size(); i++) {
            Object src = queue.get(i);
            System.out.print(i + 1);
            System.out.print("/");
            System.out.print(queue.size());
            System.out.print(": Reading " + src + "... ");
            System.out.flush();
            try {
                loader.unloadAll();
                Mib mib = null;
                if (src instanceof URL) {
                    loader.removeAllDirs();
                    mib = loader.load((URL) src);
                } else {
                    File file = (File) src;
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
        StringBuilder buffer = new StringBuilder();
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
        StringBuilder buffer = new StringBuilder();
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
    private static void addMibs(File dir, ArrayList<Object> queue) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isHidden()) {
                    // Hidden file or directories are ignored
                } else if (file.isDirectory()) {
                    addMibs(file, queue);
                } else if (isMib(file)) {
                    queue.add(file);
                }
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
        if (!file.canRead() || !file.isFile()) {
            return false;
        }
        try (
            BufferedReader in = new BufferedReader(new FileReader(file));
        ) {
            while (true) {
                String str = in.readLine();
                if (str == null) {
                    break;
                }
                str = str.trim();
                if (!str.equals("") && !str.startsWith("--")) {
                    return str.contains("DEFINITIONS");
                }
            }
        } catch (Exception ignore) {
            // Do nothing
        }
        return false;
    }
}
