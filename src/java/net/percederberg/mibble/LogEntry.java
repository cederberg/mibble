/*
 * LogEntry.java
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

/**
 * A log entry. This class holds all the details in an error or a 
 * warning log entry.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
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
        this.location = location;
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
