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
 * An SNMP access mode value. This class is used to encapsulate the
 * access value constants used in several SNMP macro types. Note that
 * due to the support for both SNMPv1, SNMPv2 and SNMPv3 not all of
 * the constants defined in this class can be present in all files.
 * Please see the comments for each individual constant regarding the
 * support for different SNMP versions.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class SnmpAccess {

    /**
     * The not implemented SNMP access mode. This mode is only used 
     * in SNMPv2 (and later) variation declarations inside an agent 
     * capabilities declaration.
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
     * used in SNMPv2 and later.
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
     * SNMPv2 and later.
     */
    public static final SnmpAccess READ_CREATE =
        new SnmpAccess("read-create");

    /**
     * The write-only SNMP access mode. This mode is only used in 
     * SNMPv1.
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
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */    
    public String toString() {
        return description;
    }
}
