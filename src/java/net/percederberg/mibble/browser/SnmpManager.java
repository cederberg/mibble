/*
 * SnmpManager.java
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

import java.io.IOException;

import uk.co.westhawk.snmp.pdu.BlockPdu;
import uk.co.westhawk.snmp.pdu.OneSetPdu;
import uk.co.westhawk.snmp.stack.AsnInteger;
import uk.co.westhawk.snmp.stack.AsnObject;
import uk.co.westhawk.snmp.stack.AsnObjectId;
import uk.co.westhawk.snmp.stack.AsnOctets;
import uk.co.westhawk.snmp.stack.Pdu;
import uk.co.westhawk.snmp.stack.PduException;
import uk.co.westhawk.snmp.stack.SnmpContextBasisFace;
import uk.co.westhawk.snmp.stack.SnmpContextPool;
import uk.co.westhawk.snmp.stack.varbind;

/**
 * An SNMP manager. This class exposes the different snmp operations
 * which the user may want to perform. It is a wrapper class over the
 * Westhawk SNMP stack.
 *
 * @see uk.co.westhawk.snmp.pdu.BlockPdu
 * @see uk.co.westhawk.snmp.stack.SnmpContextPool
 *
 * @author   Watsh Rajneesh
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.3
 */
public class SnmpManager {

    /**
     * The SNMP context pool.
     */
    private SnmpContextPool context;

    /**
     * Creates a new SNMP manager.
     *
     * @param host           the host name or IP address
     * @param port           the agent port
     * @param community      the community name
     *
     * @throws IOException if an SNMP context pool couldn't be
     *             created from the specified values
     */
    public SnmpManager(String host, int port, String community)
        throws IOException {

        String  socket;

        if (context != null) {
            context.destroy();
        }
        socket = SnmpContextBasisFace.STANDARD_SOCKET;
        context = new SnmpContextPool(host, port, socket);
        context.setCommunity(community);
    }

    /**
     * Send a Get request.
     *
     * @param oid            the oid to get
     *
     * @return a string description of the results
     */
    public String sendGetRequest(String oid) {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.GET);
        pdu.addOid(oid);
        return sendRequest(pdu);
    }

    /**
     * Send a GetNext request.
     *
     * @param oid            the oid to perform GetNext
     *
     * @return a string description of the results
     */
    public String sendGetNextRequest(String oid) {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.GETNEXT);
        pdu.addOid(oid);
        return sendRequest(pdu);
    }

    /**
     * Send a Set request.
     *
     * @param oid            the oid to set
     * @param value          the value for the set operation
     *
     * @return a string description of the results
     */
    public String sendSetRequest(String oid, String value) {
        OneSetPdu oneSetPdu = new OneSetPdu(context);
        AsnObject obj;

        if (isNumber(value)) {
            obj = new AsnInteger(Integer.parseInt(value));
        } else {
            obj = new AsnOctets(value);
        }
        oneSetPdu.addOid(oid, obj);
        return sendRequestSet(oneSetPdu);
    }

    /**
     * Send (dispatch) a Get or GetNext request.
     *
     * @param pdu            a blocking PDU for synchronous SNMP
     *                       operation
     *
     * @return a string description of the results
     */
    private String sendRequest(BlockPdu pdu) {
        try {
            if (pdu != null) {
                // Request sent and call blocks till response is received...
                varbind var = pdu.getResponseVariableBinding();
                if (var != null) {
                    AsnObjectId oid = var.getOid();
                    AsnObject res = var.getValue();
                    if (res != null) {
                            // print or display the answer
                            return (oid.toString() + "--> " + res.toString());
                    } else {
                            // Received no answer
                            return "Timeout";
                    }
                }
            }
        } catch (PduException exc) {
            return exc.getMessage();
        } catch (IOException exc) {
            // give the user feedback
            exc.printStackTrace();
        }
        return "";
    }

    /**
     * Send (dispatch) a Set request.
     *
     * @param pdu            the set request pdu
     *
     * @return a string description of the results
     */
    private String sendRequestSet(Pdu pdu) {
        try {
            if (pdu.send()) {
                // Request sent and call blocks until response is received
                varbind var[] = pdu.getRequestVarbinds();
                if (var != null && var.length > 0 && var[0] != null) {
                    AsnObjectId oid = var[0].getOid();
                    AsnObject res = var[0].getValue();
                    if (res != null) {
                        // print or display the answer
                        return (oid.toString() + "--> " + res.toString());
                    } else {
                        // Received no answer
                        return "Timeout";
                    }
                }
            }
        } catch (PduException exc) {
            return exc.getMessage();
        } catch (IOException exc) {
            return exc.getMessage();
        }
        return "";
    }

    /**
     * Checks if a string contains a number.
     *
     * @param str            the string to check
     *
     * @return true if the string contains a number, or
     *         false otherwise
     */
    private boolean isNumber(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
