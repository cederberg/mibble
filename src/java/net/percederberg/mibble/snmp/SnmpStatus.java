/*
 * SnmpStatus.java
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
 * As a special exception, the copyright holders of this library give
 * you permission to link this library with independent modules to
 * produce an executable, regardless of the license terms of these
 * independent modules, and to copy and distribute the resulting
 * executable under terms of your choice, provided that you also meet,
 * for each linked independent module, the terms and conditions of the
 * license of that module. An independent module is a module which is
 * not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the
 * library, but you are not obligated to do so. If you do not wish to
 * do so, delete this exception statement from your version.
 *
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

/**
 * An SNMP status value. This class is used to encapsulate the status
 * value constants used in several SNMP macro types. Note that due to
 * the support for both SNMPv1, SNMPv2 and SNMPv3 not all of the
 * constants defined in this class can be present in all files.
 * Please see the comments for each individual constant regarding the
 * support for different SNMP versions.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class SnmpStatus {

    /**
     * The mandatory SNMP status. This status is only used in SNMPv1.
     */
    public static final SnmpStatus MANDATORY =
        new SnmpStatus("mandatory");

    /**
     * The optional SNMP status. This status is only used in SNMPv1.
     */
    public static final SnmpStatus OPTIONAL =
        new SnmpStatus("optional");

    /**
     * The current SNMP status. This status is only used in SNMPv2
     * and later.
     */
    public static final SnmpStatus CURRENT =
        new SnmpStatus("current");

    /**
     * The deprecated SNMP status. This status is only used in SNMPv2
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
