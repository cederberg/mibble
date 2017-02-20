/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

/**
 * An SNMP privacy (encryption) parameter container.
 *
 * @author   Per Cederberg
 * @version  2.5
 * @since    2.5
 */
public class SnmpPrivacy {

    /**
     * The DES privacy type.
     */
    public static final String DES_TYPE = "3-DES (CBC-DES)";

    /**
     * The AES privacy type.
     */
    public static final String AES_TYPE = "AES (CFB128-AES-128)";

    /**
     * The privacy type.
     */
    public String type;

    /**
     * The privacy password.
     */
    public String password;

    /**
     * Creates a new SNMP privacy container.
     *
     * @param type           the privacy type
     * @param password       the privacy password
     */
    public SnmpPrivacy(String type, String password) {
        this.type = type;
        this.password = password;
    }

    /**
     * Returns the privacy type.
     *
     * @return the privacy type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the privacy password.
     *
     * @return the privacy password
     */
    public String getPassword() {
        return password;
    }
}
