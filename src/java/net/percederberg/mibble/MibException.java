/*
 * MibException.java
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

/**
 * A MIB exception. This exception is used to report processing
 * errors for operations on MIB types and values.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class MibException extends Exception {

    /**
     * The file location.
     */
    private FileLocation location;

    /**
     * Creates a new MIB exception.
     *
     * @param location       the file location
     * @param message        the error message
     */
    public MibException(FileLocation location, String message) {
        super(message);
        this.location = location;
    }

    /**
     * Creates a new MIB exception.
     *
     * @param file           the file containing the error
     * @param line           the line number containing the error
     * @param column         the column number containing the error
     * @param message        the error message
     */
    public MibException(File file, int line, int column, String message) {
        this(new FileLocation(file, line, column), message);
    }

    /**
     * Returns the error location.
     *
     * @return the error location
     */
    public FileLocation getLocation() {
        return location;
    }
}
