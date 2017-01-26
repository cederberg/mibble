/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
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
 * The SNMP notification type macro. This macro type was added to
 * SMIv2 and is defined in RFC 2578. The notification type macro is
 * used instead of the trap type macro in SMIv2.
 *
 * @see SnmpTrapType
 * @see <a href="http://www.ietf.org/rfc/rfc2578.txt">RFC 2578 (SNMPv2-SMI)</a>
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SnmpNotificationType extends SnmpType {

    /**
     * The value objects.
     */
    private ArrayList<MibValue> objects;

    /**
     * The notification type status.
     */
    private SnmpStatus status;

    /**
     * The notification type reference.
     */
    private String reference;

    /**
     * Creates a new SNMP notification type.
     *
     * @param objects        the value objects
     * @param status         the notification type status
     * @param description    the notification type description
     * @param reference      the notification type reference, or null
     */
    public SnmpNotificationType(ArrayList<MibValue> objects,
                                SnmpStatus status,
                                String description,
                                String reference) {

        super("NOTIFICATION-TYPE", description);
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

        if (!(symbol instanceof MibValueSymbol)) {
            throw new MibException(symbol.getFileRef(),
                                   "only values can have the " +
                                   getName() + " type");
        }
        for (int i = 0; i < objects.size(); i++) {
            objects.set(i, objects.get(i).initialize(log, null));
        }
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
    public ArrayList<MibValue> getObjects() {
        return objects;
    }

    /**
     * Returns the notification type status.
     *
     * @return the notification type status
     */
    public SnmpStatus getStatus() {
        return status;
    }

    /**
     * Returns the notification type reference.
     *
     * @return the notification type reference, or
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
        StringBuilder buffer = new StringBuilder();
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
