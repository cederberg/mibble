/*
 * SnmpOperation.java
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
 * Copyright (c) 2004 Watsh Rajneesh. All rights reserved.
 */

package net.percederberg.mibble.browser;

import javax.swing.JTextArea;

import uk.co.westhawk.snmp.pdu.BlockPdu;
import uk.co.westhawk.snmp.pdu.OneSetPdu;
import uk.co.westhawk.snmp.stack.AsnInteger;
import uk.co.westhawk.snmp.stack.AsnObject;
import uk.co.westhawk.snmp.stack.AsnObjectId;
import uk.co.westhawk.snmp.stack.AsnOctets;
import uk.co.westhawk.snmp.stack.Pdu;
import uk.co.westhawk.snmp.stack.PduException;
import uk.co.westhawk.snmp.stack.SnmpContextFace;
import uk.co.westhawk.snmp.stack.SnmpContextPool;
import uk.co.westhawk.snmp.stack.varbind;

/**
 * This class exposes the different snmp operations which the user 
 * may want to perform. It is a wrapper class over the Westhawk SNMP 
 * stack.
 *
 * @see uk.co.westhawk.snmp.pdu.BlockPdu
 * @see uk.co.westhawk.snmp.stack.SnmpContextPool
 *
 * @author   Watsh Rajneesh
 * @version  2.3
 * @since    2.3
 */
public class SnmpOperation {

    /**
     * The SNMP context pool.
     */
    private SnmpContextPool context;

    /**
     * Creates a new SNMP operation.
     *
     * @param host           the host name or IP address
     * @param port           the agent port
     * @param comm           the community name (read/write depends 
     *                       on type of operation)
     */
    public SnmpOperation(String host, int port, String comm) {
        createContext(host, port, comm);
    }

    /**
     * Creates the new SNMP context.
     *
     * @param host           the host name or IP address
     * @param port           the agent port
     * @param comm           the community name (read/write depends 
     *                       on type of operation)
     */
    private void createContext(String host, int port, String comm) {
        if (context != null) {
            context.destroy();
        }
        try {
            context = new SnmpContextPool(host,
                                          port,
                                          SnmpContextFace.STANDARD_SOCKET);
            context.setCommunity(comm);
        } catch (java.io.IOException exc) {
            // give the user feedback
            exc.printStackTrace();
        }
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
     */
    public void sendSetRequest(String oid, String value) {
        OneSetPdu oneSetPdu = new OneSetPdu(context);
        AsnObject obj;

        if (isNumber(value)) {
            obj = new AsnInteger(Integer.parseInt(value));
        } else {
            obj = new AsnOctets(value);
        }
        oneSetPdu.addOid(oid, obj);
        sendRequestSet(oneSetPdu);
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
                            return "ERR";
                    }
                }
            }
        } catch (PduException exc) {
            // comes here when timeout occurs.
            if (exc.getMessage().equals("No such name error")) {
                return "No such name error";
            }
            return "ERR";
        } catch (java.io.IOException exc) {
            // give the user feedback
            exc.printStackTrace();
        }
        return "";
    }

    /**
     * Send (dispatch) a Set request.
     *
     * @param pdu            the set request pdu
     */
    private void sendRequestSet(Pdu pdu) {
        try {
            pdu.send();
        } catch (PduException exc) {
            exc.printStackTrace();
        } catch (java.io.IOException exc) {
            exc.printStackTrace();
        }
    }

    /**
     * Walk the MIB.
     *
     * @param oid            oid to start walking the mib
     * @param resultArea     text area to populate with the result of 
     *                       walk operation
     *
     * @return a string description of the results
     */
    public String snmpWalk(String oid, JTextArea resultArea) {
        BlockPdu  pdu = new BlockPdu(context);

        pdu = new BlockPdu(context);
        pdu.setPduType(BlockPdu.GETNEXT);
        pdu.addOid(oid);
        while (true) {
            try {
                if ((pdu != null)) {
                    // Request sent and call blocks till response is received...
                    varbind var = pdu.getResponseVariableBinding();
                    if (var != null) {
                        AsnObjectId oidNext = var.getOid();
                        AsnObject res = var.getValue();
                        if (res.getRespType() != 
                            AsnObject.SNMP_VAR_ENDOFMIBVIEW) {

                            if (res != null && oidNext != null) {
                                if ((oidNext.toString().indexOf(oid)) == -1) {
                                    return "END OF WALK";
                                }
                                // print or display the answer
                                resultArea.append("\n" +
                                        oidNext.toString() + "--> "
                                        + res.toString());
                                
                                pdu = new BlockPdu(context);
                                pdu.setPduType(BlockPdu.GETNEXT);
                                pdu.addOid(oidNext.toString());
                            } else {
                                return ("$$1");
                            }
                        } else {
                            return ("$$2");
                        }
                    }
                } else {
                     return ("$$3");
                }
            } catch (PduException exc1) {
                // comes here when timeout occurs.
                exc1.printStackTrace();
                break;
            } catch (java.io.IOException exc) {
                exc.printStackTrace();
                break;
            }
        }
        return ("END OF MIB");
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
