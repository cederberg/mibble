/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
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
     * The CBC-DES privacy type.
     */
    public static final String CBC_DES_TYPE = "CBC-DES";

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
