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
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import net.percederberg.grammatica.parser.ParseException;
import net.percederberg.grammatica.parser.ParserLogException;

/**
 * A MIB loader log. This class contains error and warning messages
 * from loading a MIB file and all imports not previously loaded.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
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
     * Prints all log entries to the specified output stream. A 
     * simple log printer will be created for handling the printout.
     * 
     * @param output         the output stream to use
     */
    public void printTo(PrintStream output) {
        printTo(new PrintWriter(output));
    }

    /**
     * Prints all log entries to the specified output stream. A 
     * simple log printer will be created for handling the printout.
     * 
     * @param output         the output stream to use
     */
    public void printTo(PrintWriter output) {
        printTo(new SimpleLogPrinter(output, 70));
    }

    /**
     * Prints all log entries to the specified log printer.
     * 
     * @param printer        the log printer to use
     */
    public void printTo(LogPrinter printer) {
        for (int i = 0; i < entries.size(); i++) {
            printer.print((LogEntry) entries.get(i));
        }
    }
}
