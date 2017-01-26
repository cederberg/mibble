/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A MIB validation exception. This exception is used to report
 * validation errors for MIB types and values.
 *
 * <strong>NOTE:</strong> This class is used internally during the
 * MIB parsing only. Do <strong>NOT</strong> use or reference this
 * class.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class MibException extends Exception {

    /**
     * The MIB file location.
     */
    private MibFileRef fileRef;

    /**
     * Creates a new MIB exception.
     *
     * @param fileRef        the MIB file location
     * @param message        the error message
     */
    public MibException(MibFileRef fileRef, String message) {
        super(message);
        this.fileRef = fileRef;
    }

    /**
     * Returns the error MIB file location.
     *
     * @return the error MIB file location
     */
    public MibFileRef getFileRef() {
        return fileRef;
    }
}
