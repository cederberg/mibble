/*
 * SnmpNotificationGroup.java
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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * An SNMP notification group.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class SnmpNotificationGroup extends MibType {

    /**
     * The list of notification values.
     */
    private ArrayList notifications;

    /**
     * The type status.
     */
    private SnmpStatus status;

    /**
     * The type description.
     */
    private String description;
    
    /**
     * The type reference.
     */
    private String reference;

    /**
     * Creates a new SNMP notification group.
     * 
     * @param notifications  the list of notification values
     * @param status         the type status
     * @param description    the type description
     * @param reference      the type reference, or null
     */
    public SnmpNotificationGroup(ArrayList notifications,
                                 SnmpStatus status,
                                 String description,
                                 String reference) {

        super("NOTIFICATION-GROUP", false);
        this.notifications = notifications;
        this.status = status;
        this.description = description;
        this.reference = reference;                                     
    }

    /**
     * Initializes the MIB type. This will remove all levels of
     * indirection present, such as references to other types, and 
     * returns the basic type. No type information is lost by this 
     * operation. This method may modify this object as a 
     * side-effect, and will be called by the MIB loader.
     * 
     * @param log            the MIB loader log
     * 
     * @return the basic MIB type
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public MibType initialize(MibLoaderLog log) throws MibException {
        ArrayList  list = new ArrayList();
        
        for (int i = 0; i < notifications.size(); i++) {
            list.add(((MibValue) notifications.get(i)).initialize(log));
        }
        this.notifications = list;
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
     * Returns the list of notification MIB values.
     * 
     * @return the list of notification MIB values
     * 
     * @see net.percederberg.mibble.MibValue
     */
    public ArrayList getNotifications() {
        return notifications;
    }

    /**
     * Returns the type status.
     * 
     * @return the type status
     */
    public SnmpStatus getStatus() {
        return status;
    }

    /**
     * Returns the type description.
     * 
     * @return the type description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the type reference.
     * 
     * @return the type reference, or
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
        buffer.append("\n  Notifications: ");
        buffer.append(notifications);
        buffer.append("\n  Status: ");
        buffer.append(status);
        buffer.append("\n  Description: ");
        buffer.append(description);
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        buffer.append("\n)");
        return buffer.toString();
    }
}
