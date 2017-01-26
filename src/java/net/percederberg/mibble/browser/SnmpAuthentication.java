/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

/**
 * An SNMP authentication parameter container.
 *
 * @author   Per Cederberg
 * @version  2.5
 * @since    2.5
 */
public class SnmpAuthentication {

    /**
     * The MD5 authentication type.
     */
    public static final String MD5_TYPE = "MD5";

    /**
     * The SHA-1 authentication type.
     */
    public static final String SHA1_TYPE = "SHA-1";

    /**
     * The authentication type.
     */
    public String type;

    /**
     * The authentication password.
     */
    public String password;

    /**
     * Creates a new SNMP authentication container.
     *
     * @param type           the authentication type
     * @param password       the authentication password
     */
    public SnmpAuthentication(String type, String password) {
        this.type = type;
        this.password = password;
    }

    /**
     * Returns the authentication type.
     *
     * @return the authentication type
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the authentication password.
     *
     * @return the authentication password
     */
    public String getPassword() {
        return password;
    }
}
