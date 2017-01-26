/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

/**
 * An SNMP status value. This class is used to encapsulate the status
 * value constants used in several SNMP macro types. Note that, due
 * to the support for both SMIv1 and SMIv2, not all of the constants
 * defined in this class can be present in all files. Please see the
 * comments for each individual constant regarding the support for
 * different SMI versions.
 *
 * @author   Per Cederberg
 * @version  2.2
 * @since    2.0
 */
public class SnmpStatus {

    /**
     * The mandatory SNMP status. This status is only used in SMIv1.
     */
    public static final SnmpStatus MANDATORY =
        new SnmpStatus("mandatory");

    /**
     * The optional SNMP status. This status is only used in SMIv1.
     */
    public static final SnmpStatus OPTIONAL =
        new SnmpStatus("optional");

    /**
     * The current SNMP status. This status is only used in SMIv2
     * and later.
     */
    public static final SnmpStatus CURRENT =
        new SnmpStatus("current");

    /**
     * The deprecated SNMP status. This status is only used in SMIv2
     * and later.
     */
    public static final SnmpStatus DEPRECATED =
        new SnmpStatus("deprecated");

    /**
     * The obsolete SNMP status.
     */
    public static final SnmpStatus OBSOLETE =
        new SnmpStatus("obsolete");

    /**
     * The status description.
     */
    private String description;

    /**
     * Creates a new SNMP status.
     *
     * @param description    the status description
     */
    private SnmpStatus(String description) {
        this.description = description;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return description;
    }
}
