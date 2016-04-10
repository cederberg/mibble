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
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
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
