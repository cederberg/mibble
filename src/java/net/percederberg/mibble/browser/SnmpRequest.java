/*
 * SnmpRequest.java
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

import net.percederberg.mibble.MibType;

/**
 * An SNMP request value container.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.5
 */
public class SnmpRequest {

    /**
     * The request OID.
     */
    private String oid;

    /**
     * The SET request data type.
     */
    private MibType type;

    /**
     * The SET request data value. 
     */
    private String value;

    /**
     * Creates a new SNMP GET or GETNEXT request.
     *
     * @param oid            the OID to set
     */
    public SnmpRequest(String oid) {
        this.oid = oid;
        this.type = null;
        this.value = null;
    }

    /**
     * Creates a new SNMP SET request.
     *
     * @param oid            the OID to set
     * @param type           the data type
     * @param value          the data value
     */
    public SnmpRequest(String oid, MibType type, String value) {
        this.oid = oid;
        this.type = type;
        this.value = value;
    }

    /**
     * The request OID.
     *
     * @return the request OID 
     */
    public String getOid() {
        return oid;
    }

    /**
     * The request value data type.
     *
     * @return the request type
     */
    public MibType getType() {
        return type;
    }

    /**
     * The request value.
     *
     * @return the request value
     */
    public String getValue() {
        return value;
    }
}
