/*
 * SimpleLogPrinter.java
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
 * As a special exception, the copyright holders of this library give
 * you permission to link this library with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is
 * not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 *
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * A simple log printer. This class formats and prints log entries to 
 * an output stream. The formatting includes linebreaking the string
 * so that a specified print margin is not passed unless necessary.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class SimpleLogPrinter implements LogPrinter {

    /**
     * The output stream.
     */
    private PrintWriter output;

    /**
     * The print margin.
     */
    private int margin;

    /**
     * Creates a new simple log printer.
     *
     * @param output         the output stream 
     * @param margin         the print margin
     */
    public SimpleLogPrinter(PrintStream output, int margin) {
        this(new PrintWriter(output), margin);
    }
    
    /**
     * Creates a new simple log printer.
     *
     * @param output         the output stream 
     * @param margin         the print margin
     */
    public SimpleLogPrinter(PrintWriter output, int margin) {
        this.output = output;
        this.margin = margin;
    }
    
    /**
     * Prints a log entry.
     * 
     * @param entry          the log entry to print
     */
    public void print(LogEntry entry) {
        StringBuffer  buffer = new StringBuffer();
        String        line;

        // Handle error type
        switch (entry.getType()) {
        case LogEntry.ERROR:
            buffer.append("Error: ");
            break;
        case LogEntry.WARNING:
            buffer.append("Warning: ");
            break;
        default:
            buffer.append("Internal Error: ");
            break;
        }

        // Handle file name and location
        buffer.append("in ");
        buffer.append(relativeFilename(entry.getFile()));
        if (entry.getLineNumber() > 0) {
            buffer.append(": line ");
            buffer.append(entry.getLineNumber());
        }
        buffer.append(":\n");

        // Handle message and file extract
        buffer.append(linebreakString(entry.getMessage(), "    ", margin));
        line = entry.readLine();
        if (line != null) {
            buffer.append("\n\n");
            buffer.append(line);
            buffer.append("\n");
            for (int i = 1; i < entry.getColumnNumber(); i++) {
                if (line.charAt(i - 1) == '\t') {
                    buffer.append("\t");
                } else {
                    buffer.append(" ");
                }
            }
            buffer.append("^");
        }

        // Print formatted log entry
        output.println(buffer.toString());
        output.flush();
    }

    /**
     * Creates a relative file name from a file. This method will 
     * return the absolute file name if the file unless the current
     * directory is a parent to the file. 
     * 
     * @param file           the file to calculate relative name for
     * 
     * @return the relative name if found, or
     *         the absolute name otherwise
     */
    private String relativeFilename(File file) {
        String  currentPath;
        String  filePath;
        
        try {
            currentPath = new File(".").getCanonicalPath();
            filePath = file.getCanonicalPath();
            if (filePath.startsWith(currentPath)) {
                filePath = filePath.substring(currentPath.length());
                if (filePath.charAt(0) == '/' 
                 || filePath.charAt(0) == '\\') {
                
                    return filePath.substring(1);
                } else {
                    return filePath;
                }
            }
        } catch (IOException e) {
            // Do nothing
        }
        return file.toString();
    }

    /**
     * Breaks a string into multiple lines. This method will also add
     * a prefix to each line in the resulting string. The prefix 
     * length will be taken into account when breaking the line. Line
     * breaks will only be inserted as replacements for space 
     * characters.
     * 
     * @param str            the string to line break
     * @param prefix         the prefix to add to each line
     * @param length         the maximum line length
     * 
     * @return the new formatted string
     */
    private String linebreakString(String str, String prefix, int length) {    
        StringBuffer  buffer = new StringBuffer();
        int           pos;

        while (str.length() + prefix.length() > length) {
            pos = str.lastIndexOf(' ', length - prefix.length());
            if (pos < 0) {
                pos = str.indexOf(' ');
                if (pos < 0) {
                    break;
                }
            }
            buffer.append(prefix);
            buffer.append(str.substring(0, pos));
            str = str.substring(pos + 1);
            buffer.append("\n");
        }
        buffer.append(prefix);
        buffer.append(str);
        return buffer.toString();
    }
}
