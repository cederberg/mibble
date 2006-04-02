/*
 * MibLoaderLog.java
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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

import net.percederberg.grammatica.parser.ParseException;
import net.percederberg.grammatica.parser.ParserLogException;

/**
 * A MIB loader log. This class contains error and warning messages
 * from loading a MIB file and all imports not previously loaded.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.6
 * @since    2.0
 */
public class MibLoaderLog {

    /**
     * The log entries.
     */
    private ArrayList entries = new ArrayList();

    /**
     * The log error count.
     */
    private int errors = 0;

    /**
     * The log warning count.
     */
    private int warnings = 0;

    /**
     * Creates a new loader log without entries.
     */
    public MibLoaderLog() {
        // Nothing initialization needed
    }

    /**
     * Returns the number of errors in the log.
     *
     * @return the number of errors in the log
     */
    public int errorCount() {
        return errors;
    }

    /**
     * Returns the number of warnings in the log.
     *
     * @return the number of warnings in the log
     */
    public int warningCount() {
        return warnings;
    }

    /**
     * Adds a log entry to this log.
     *
     * @param entry          the log entry to add
     */
    public void add(LogEntry entry) {
        if (entry.isError()) {
            errors++;
        }
        if (entry.isWarning()) {
            warnings++;
        }
        entries.add(entry);
    }

    /**
     * Adds an internal error message to the log. Internal errors are
     * only issued when possible bugs are encountered. They are
     * counted as errors.
     *
     * @param location       the file location
     * @param message        the error message
     */
    public void addInternalError(FileLocation location, String message) {
        add(new LogEntry(LogEntry.INTERNAL_ERROR, location, message));
    }

    /**
     * Adds an internal error message to the log. Internal errors are
     * only issued when possible bugs are encountered. They are
     * counted as errors.
     *
     * @param file           the file affected
     * @param message        the error message
     */
    public void addInternalError(File file, String message) {
        addInternalError(new FileLocation(file), message);
    }

    /**
     * Adds an error message to the log.
     *
     * @param location       the file location
     * @param message        the error message
     */
    public void addError(FileLocation location, String message) {
        add(new LogEntry(LogEntry.ERROR, location, message));
    }

    /**
     * Adds an error message to the log.
     *
     * @param file           the file affected
     * @param line           the line number
     * @param column         the column number
     * @param message        the error message
     */
    public void addError(File file, int line, int column, String message) {
        addError(new FileLocation(file, line, column), message);
    }

    /**
     * Adds a warning message to the log.
     *
     * @param location       the file location
     * @param message        the warning message
     */
    public void addWarning(FileLocation location, String message) {
        add(new LogEntry(LogEntry.WARNING, location, message));
    }

    /**
     * Adds a warning message to the log.
     *
     * @param file           the file affected
     * @param line           the line number
     * @param column         the column number
     * @param message        the warning message
     */
    public void addWarning(File file, int line, int column, String message) {
        addWarning(new FileLocation(file, line, column), message);
    }

    /**
     * Adds all log entries from another log.
     *
     * @param log            the MIB loader log
     */
    public void addAll(MibLoaderLog log) {
        for (int i = 0; i < log.entries.size(); i++) {
            add((LogEntry) log.entries.get(i));
        }
    }

    /**
     * Adds all errors from a parser log exception.
     *
     * @param file           the file affected
     * @param log            the parser log exception
     */
    void addAll(File file, ParserLogException log) {
        ParseException  e;

        for (int i = 0; i < log.getErrorCount(); i++) {
            e = log.getError(i);
            addError(file, e.getLine(), e.getColumn(), e.getErrorMessage());
        }
    }

    /**
     * Returns an iterator with all the log entries. The iterator
     * will only return LogEntry instances.
     *
     * @return an iterator with all the log entries
     *
     * @see LogEntry
     *
     * @since 2.2
     */
    public Iterator entries() {
        return entries.iterator();
    }

    /**
     * Prints all log entries to the specified output stream.
     *
     * @param output         the output stream to use
     */
    public void printTo(PrintStream output) {
        printTo(new PrintWriter(output));
    }

    /**
     * Prints all log entries to the specified output stream.
     *
     * @param output         the output stream to use
     */
    public void printTo(PrintWriter output) {
        printTo(output, 70);
    }

    /**
     * Prints all log entries to the specified output stream.
     *
     * @param output         the output stream to use
     * @param margin         the print margin
     *
     * @since 2.2
     */
    public void printTo(PrintWriter output, int margin) {
        StringBuffer  buffer = new StringBuffer();
        LogEntry      entry;
        String        str;

        for (int i = 0; i < entries.size(); i++) {
            entry = (LogEntry) entries.get(i);
            buffer.setLength(0);
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
            buffer.append("in ");
            buffer.append(relativeFilename(entry.getFile()));
            if (entry.getLineNumber() > 0) {
                buffer.append(": line ");
                buffer.append(entry.getLineNumber());
            }
            buffer.append(":\n");
            str = linebreakString(entry.getMessage(), "    ", margin);
            buffer.append(str);
            str = entry.readLine();
            if (str != null && str.length() >= entry.getColumnNumber()) {
                buffer.append("\n\n");
                buffer.append(str);
                buffer.append("\n");
                for (int j = 1; j < entry.getColumnNumber(); j++) {
                    if (str.charAt(j - 1) == '\t') {
                        buffer.append("\t");
                    } else {
                        buffer.append(" ");
                    }
                }
                buffer.append("^");
            }
            output.println(buffer.toString());
        }
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

        if (file == null) {
            return "<unknown file>";
        }
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



    /**
     * A log entry. This class holds all the details in an error or a
     * warning log entry.
     *
     * @author   Per Cederberg, <per at percederberg dot net>
     * @version  2.2
     * @since    2.2
     */
    public class LogEntry {

        /**
         * The internal error log entry type constant.
         */
        public static final int INTERNAL_ERROR = 1;

        /**
         * The error log entry type constant.
         */
        public static final int ERROR = 2;

        /**
         * The warning log entry type constant.
         */
        public static final int WARNING = 3;

        /**
         * The log entry type.
         */
        private int type;

        /**
         * The log entry file reference.
         */
        private FileLocation location;

        /**
         * The log entry message.
         */
        private String message;

        /**
         * Creates a new log entry.
         *
         * @param type           the log entry type
         * @param location       the log entry file reference
         * @param message        the log entry message
         */
        public LogEntry(int type, FileLocation location, String message) {
            this.type = type;
            if (location == null || location.getFile() == null) {
                this.location = new FileLocation(new File("<unknown file>"));
            } else {
                this.location = location;
            }
            this.message = message;
        }

        /**
         * Checks if this is an error log entry.
         *
         * @return true if this is an error log entry, or
         *         false otherwise
         */
        public boolean isError() {
            return type == INTERNAL_ERROR || type == ERROR;
        }

        /**
         * Checks if this is a warning log entry.
         *
         * @return true if this is a warning log entry, or
         *         false otherwise
         */
        public boolean isWarning() {
            return type == WARNING;
        }

        /**
         * Returns the log entry type.
         *
         * @return the log entry type
         *
         * @see #INTERNAL_ERROR
         * @see #ERROR
         * @see #WARNING
         */
        public int getType() {
            return type;
        }

        /**
         * Returns the file this entry applies to.
         *
         * @return the file affected
         */
        public File getFile() {
            return location.getFile();
        }

        /**
         * Returns the line number.
         *
         * @return the line number
         */
        public int getLineNumber() {
            return location.getLineNumber();
        }

        /**
         * Returns the column number.
         *
         * @return the column number
         */
        public int getColumnNumber() {
            return location.getColumnNumber();
        }

        /**
         * Returns the log entry message.
         *
         * @return the log entry message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Reads the line from the referenced file. If the file couldn't
         * be opened or read correctly, null will be returned. The line
         * will NOT contain the terminating '\n' character.
         *
         * @return the line read, or
         *         null if not found
         */
        public String readLine() {
            return location.readLine();
        }
    }
}
