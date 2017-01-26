/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
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
 * @author   Per Cederberg
 * @version  2.10
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
    public ArrayList<String> oids = new ArrayList<>();

    /**
     * The map of all value, indexed by OID.
     */
    private HashMap<String,String> values = new HashMap<>();

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
            for (varbind bind : variables) {
                oids.add(bind.getOid().toString());
                values.put(bind.getOid().toString(),
                           bind.getValue().toString());
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
    public Iterator<String> getOids() {
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
        return values.get(oid);
    }

    /**
     * Returns a string representation of the OID and value map.
     *
     * @return a string representation of the OID and value map
     */
    public String getOidsAndValues() {
        StringBuilder buffer = new StringBuilder();
        for (String oid : oids) {
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
        StringBuilder buffer = new StringBuilder();
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
