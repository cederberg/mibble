/*
 * SnmpObjectGroup.java
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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * The SNMP object group macro type. This macro type was added to
 * SMIv2 and is defined in RFC 2580.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc2580.txt">RFC 2580 (SNMPv2-CONF)</a>
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.8
 * @since    2.0
 */
public class SnmpObjectGroup extends SnmpType {

    /**
     * The value objects.
     */
    private ArrayList objects;

    /**
     * The object group status.
     */
    private SnmpStatus status;

    /**
     * The object group reference.
     */
    private String reference;

    /**
     * Creates a new SNMP object group.
     *
     * @param objects        the value objects
     * @param status         the object group status
     * @param description    the object group description
     * @param reference      the object group reference, or null
     */
    public SnmpObjectGroup(ArrayList objects,
                           SnmpStatus status,
                           String description,
                           String reference) {

        super("OBJECT-GROUP", description);
        this.objects = objects;
        this.status = status;
        this.reference = reference;
    }

    /**
     * Initializes the MIB type. This will remove all levels of
     * indirection present, such as references to types or values. No
     * information is lost by this operation. This method may modify
     * this object as a side-effect, and will return the basic
     * type.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param symbol         the MIB symbol containing this type
     * @param log            the MIB loader log
     *
     * @return the basic MIB type
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     *
     * @since 2.2
     */
    public MibType initialize(MibSymbol symbol, MibLoaderLog log)
        throws MibException {

        ArrayList  list = new ArrayList();
        MibValue   value;

        if (!(symbol instanceof MibValueSymbol)) {
            throw new MibException(symbol.getLocation(),
                                   "only values can have the " +
                                   getName() + " type");
        }
        for (int i = 0; i < objects.size(); i++) {
            value = (MibValue) objects.get(i);
            list.add(value.initialize(log, null));
        }
        this.objects = list;
        return this;
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if and only if it is an object identifier
     * value.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return value instanceof ObjectIdentifierValue;
    }

    /**
     * Returns the value objects. The returned list will consist of
     * MibValue instances.
     *
     * @return the value objects
     *
     * @see net.percederberg.mibble.MibValue
     */
    public ArrayList getObjects() {
        return objects;
    }

    /**
     * Returns the object group status.
     *
     * @return the object group status
     */
    public SnmpStatus getStatus() {
        return status;
    }

    /**
     * Returns the object group reference.
     *
     * @return the object group reference, or
     *         null if no reference has been set
     */
    public String getReference() {
        return reference;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append(super.toString());
        buffer.append(" (");
        buffer.append("\n  Objects: ");
        buffer.append(objects);
        buffer.append("\n  Status: ");
        buffer.append(status);
        buffer.append("\n  Description: ");
        buffer.append(getDescription("               "));
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        buffer.append("\n)");
        return buffer.toString();
    }
}
