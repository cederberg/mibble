/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

/**
 * An SNMP error.
 *
 * @author   Per Cederberg
 * @version  2.5
 * @since    2.5
 */
public class SnmpException extends Exception {

    /**
     * Creates a new SNMP exception.
     *
     * @param message        the error message
     */
    public SnmpException(String message) {
        super(message);
    }
}
