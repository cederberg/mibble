/*
 * SnmpResponse.java
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import uk.co.westhawk.snmp.pdu.BlockPdu;
import uk.co.westhawk.snmp.stack.varbind;

/**
 * An SNMP response container.
 *
 * @author   Watsh Rajneesh
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.5
 */
public class SnmpResponse {

    /**
     * The response error status.
     */
    public int errorStatus;

    /**
     * The response error status as a string.
     */
    public String errorStatusString;

    /**
     * The error index.
     */
    public int errorIndex;

    /**
     * The list of response OIDs.
     */
    public ArrayList oids = new ArrayList();

    /**
     * The map of all value, indexed by OID.
     */
    private HashMap values = new HashMap();

    /**
     * Creates a new SNMP response container.
     *
     * @param pdu            the PDU that was used
     * @param variables      the variable bindings (or null)
     */
    public SnmpResponse(BlockPdu pdu, varbind[] variables) {
        errorStatus = pdu.getErrorStatus();
        errorStatusString = pdu.getErrorStatusString();
        errorIndex = pdu.getErrorIndex();
        if (variables != null) {
            for (int i = 0; i < variables.length; i++) {
                oids.add(variables[i].getOid().toString());
                values.put(variables[i].getOid().toString(),
                           variables[i].getValue().toString());
            }
        }
    }

    /**
     * Returns the number of OID and value pairs.
     *
     * @return the number of OID and value pairs
     */
    public int getCount() {
        return oids.size();
    }

    /**
     * Returns the OID at a specified position.
     *
     * @param index          the OID index, 0 <= index < getCount()
     *
     * @return the OID string
     *
     * @see #getCount()
     */
    public String getOid(int index) {
        if (index < 0 || index >= oids.size()) {
            return null;
        } else {
            return oids.get(index).toString();
        }
    }

    /**
     * Returns an iterator with all the OIDs.
     *
     * @return an iterator with all the OIDs
     */
    public Iterator getOids() {
        return oids.iterator();
    }

    /**
     * Returns the value at a specified position.
     *
     * @param index          the value index, 0 <= index < getCount()
     *
     * @return the value string
     *
     * @see #getCount()
     */
    public String getValue(int index) {
        return getValue(getOid(index));
    }

    /**
     * Returns the response value for a specified OID.
     *
     * @param oid            the OID value
     *
     * @return the response value, or
     *         null if not found
     */
    public String getValue(String oid) {
        return (String) values.get(oid);
    }

    /**
     * Returns a string representation of the OID and value map.
     *
     * @return a string representation of the OID and value map
     */
    public String getOidsAndValues() {
        StringBuffer  buffer = new StringBuffer();
        String        oid;

        for (int i = 0; i < oids.size(); i++) {
            oid = oids.get(i).toString();
            buffer.append(oid);
            buffer.append(": ");
            buffer.append(getValue(oid));
            buffer.append("\n");
        }
        return buffer.toString();
    }

    /**
     * Returns a string representation of this response.
     *
     * @return a string representation of this response
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append(getOidsAndValues());
        buffer.append("Error status: ");
        buffer.append(errorStatus);
        buffer.append("\n");
        buffer.append("Error index: ");
        buffer.append(errorIndex);
        buffer.append("\n");
        buffer.append("Error status message: ");
        buffer.append(errorStatusString);
        buffer.append("\n");
        return buffer.toString();
    }
}
