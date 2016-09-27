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
 * A MIB loader exception. This exception is thrown when a MIB file
 * couldn't be loaded properly, normally due to syntactical or
 * semantical errors in the file.
 *
 * @author   Per Cederberg
 * @version  2.3
 * @since    2.0
 */
public class MibLoaderException extends Exception {

    /**
     * The MIB loader log.
     */
    private MibLoaderLog log;

    /**
     * Creates a new MIB loader exception.
     *
     * @param log            the MIB loader log
     */
    public MibLoaderException(MibLoaderLog log) {
        this.log = log;
    }

    /**
     * Creates a new MIB loader exception. The specified message will
     * be added to a new MIB loader log as an error.
     *
     * @param file           the file containg the error
     * @param message        the error message
     *
     * @since 2.3
     */
    public MibLoaderException(File file, String message) {
        log = new MibLoaderLog();
        log.addError(file, -1, -1, message);
    }

    /**
     * Returns the MIB loader log.
     *
     * @return the MIB loader log
     */
    public MibLoaderLog getLog() {
        return log;
    }

    /**
     * Returns a error summary message.
     *
     * @return a error summary message
     */
    public String getMessage() {
        return "found " + log.errorCount() + " MIB loader errors";
    }
}
