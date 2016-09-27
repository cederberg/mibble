/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;

/**
 * A MIB exception. This exception is used to report processing
 * errors for operations on MIB types and values.
 *
 * @author   Per Cederberg
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
