/*
 * SnmpPrivacy.java
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

package net.percederberg.mibble.browser;

/**
 * An SNMP privacy (encryption) parameter container.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
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
