/*
 * SnmpAccess.java
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

package net.percederberg.mibble.snmp;

/**
 * An SNMP access mode value. This class is used to encapsulate the
 * access value constants used in several SNMP macro types. Note that
 * due to the support for both SMIv1 and SMIv2 not all of the
 * constants defined in this class can be present in all files.
 * Please see the comments for each individual constant regarding the
 * support for different SNMP versions.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.0
 */
public class SnmpAccess {

    /**
     * The not implemented SNMP access mode. This mode is only used
     * in SMIv2 variation declarations inside an agent capabilities
     * declaration.
     */
    public static final SnmpAccess NOT_IMPLEMENTED =
        new SnmpAccess("not-implemented");

    /**
     * The not accesible SNMP access mode.
     */
    public static final SnmpAccess NOT_ACCESSIBLE =
        new SnmpAccess("not-accessible");

    /**
     * The accesible for notify SNMP access mode. This mode is only
     * used in SMIv2.
     */
    public static final SnmpAccess ACCESSIBLE_FOR_NOTIFY =
        new SnmpAccess("accessible-for-notify");

    /**
     * The read-only SNMP access mode.
     */
    public static final SnmpAccess READ_ONLY =
        new SnmpAccess("read-only");

    /**
     * The read-write SNMP access mode.
     */
    public static final SnmpAccess READ_WRITE =
        new SnmpAccess("read-write");

    /**
     * The read-create SNMP access mode. This mode is only used in
     * SMIv2.
     */
    public static final SnmpAccess READ_CREATE =
        new SnmpAccess("read-create");

    /**
     * The write-only SNMP access mode. This mode is only used in
     * SMIv1.
     */
    public static final SnmpAccess WRITE_ONLY =
        new SnmpAccess("write-only");

    /**
     * The access mode description.
     */
    private String description;

    /**
     * Creates a new SNMP access mode.
     *
     * @param description    the access mode description
     */
    private SnmpAccess(String description) {
        this.description = description;
    }

    /**
     * Checks if this access mode allows reading the value.
     *
     * @return true if reading is allowed, or
     *         false otherwise
     *
     * @since 2.5
     */
    public boolean canRead() {
        return this == READ_ONLY
            || this == READ_WRITE
            || this == READ_CREATE;
    }

    /**
     * Checks if this access mode allows writing the value.
     *
     * @return true if writing is allowed, or
     *         false otherwise
     *
     * @since 2.5
     */
    public boolean canWrite() {
        return this == READ_WRITE
            || this == READ_CREATE
            || this == WRITE_ONLY;
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
