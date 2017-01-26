/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.browser;

import net.percederberg.mibble.MibType;

/**
 * An SNMP request value container.
 *
 * @author   Per Cederberg
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
