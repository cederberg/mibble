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
import java.net.InetAddress;

import uk.co.westhawk.snmp.pdu.BlockPdu;
import uk.co.westhawk.snmp.stack.AsnInteger;
import uk.co.westhawk.snmp.stack.AsnObject;
import uk.co.westhawk.snmp.stack.AsnObjectId;
import uk.co.westhawk.snmp.stack.AsnOctets;
import uk.co.westhawk.snmp.stack.AsnUnsInteger;
import uk.co.westhawk.snmp.stack.AsnUnsInteger64;
import uk.co.westhawk.snmp.stack.PduException;
import uk.co.westhawk.snmp.stack.SnmpContextBasisFace;
import uk.co.westhawk.snmp.stack.SnmpContextPool;

import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeTag;

/**
 * An SNMPv1 manager. This class handles the GET, GETNEXT and SET
 * SNMP operations. It is a wrapper class over the Westhawk SNMP
 * stack.
 *
 * @see uk.co.westhawk.snmp.pdu.BlockPdu
 * @see uk.co.westhawk.snmp.stack.SnmpContextPool
 *
 * @author   Watsh Rajneesh
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.5
 */
public class SnmpManager {

    /**
     * The default SNMP port.
     */
    public static final int DEFAULT_PORT = SnmpContextBasisFace.DEFAULT_PORT;

    /**
     * The SNMP context pool.
     */
    private SnmpContextBasisFace context = null;

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

        SnmpContextPool  poolV1;

        poolV1 = new SnmpContextPool(host, port);
        poolV1.setCommunity(community);
        context = poolV1;
    }

    /**
     * Destroys the encapsulated SNMP context. This will free all
     * resources used by this instance. After calling this method,
     * no other methods should be called on this instance.
     */
    public void destroy() {
        context.destroy();
        context = null;
    }

    /**
     * Sends an SNMP get request for a single OID.
     *
     * @param oid            the OID to get
     *
     * @return the SNMP response
     *
     * @throws SnmpException if the SNMP request failed
     */
    public SnmpResponse get(String oid) throws SnmpException {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.GET);
        addOid(pdu, oid);
        return send(pdu);
    }

    /**
     * Sends an SNMP get request for multiple OIDs.
     *
     * @param oids           the OIDs to get
     *
     * @return the SNMP response
     *
     * @throws SnmpException if the SNMP request failed
     */
    public SnmpResponse get(String[] oids) throws SnmpException {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.GET);
        for (int i = 0; i < oids.length; i++) {
            addOid(pdu, oids[i]);
        }
        return send(pdu);
    }

    /**
     * Sends an SNMP get gext request for a single OID.
     *
     * @param oid            the OID whose successor will be returned
     *
     * @return the SNMP response
     *
     * @throws SnmpException if the SNMP request failed
     */
    public SnmpResponse getNext(String oid) throws SnmpException {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.GETNEXT);
        addOid(pdu, oid);
        return send(pdu);
    }

    /**
     * Sends an SNMP get gext request for multiple OIDs.
     *
     * @param oids           the OIDs whose successors will be returned
     *
     * @return the SNMP response
     *
     * @throws SnmpException if the SNMP request failed
     */
    public SnmpResponse getNext(String[] oids) throws SnmpException {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.GETNEXT);
        for (int i = 0; i < oids.length; i++) {
            addOid(pdu, oids[i]);
        }
        return send(pdu);
    }

    /**
     * Sends an SNMP set request for a single OID.
     *
     * @param request        the request object
     *
     * @return the SNMP response
     *
     * @throws SnmpException if the SNMP request failed
     */
    public SnmpResponse set(SnmpRequest request) throws SnmpException {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.SET);
        addOid(pdu, request.getOid(), createAsnValue(request));
        return send(pdu);
    }

    /**
     * Sends an SNMP set request for multiple OIDs.
     *
     * @param requests       the request objects
     *
     * @return the SNMP response
     *
     * @throws SnmpException if the SNMP request failed
     */
    public SnmpResponse set(SnmpRequest[] requests) throws SnmpException {
        BlockPdu  pdu = new BlockPdu(context);

        pdu.setPduType(BlockPdu.SET);
        for (int i = 0; i < requests.length; i++) {
            addOid(pdu, requests[i].getOid(), createAsnValue(requests[i]));
        }
        return send(pdu);
    }

    /**
     * A synchronous SNMP request dispatch method.
     *
     * @param pdu            the blocking synchronous pdu
     *
     * @return the SNMP response
     *
     * @throws SnmpException if the SNMP request failed
     */
    private SnmpResponse send(BlockPdu pdu) throws SnmpException {
        try {
            return new SnmpResponse(pdu, pdu.getResponseVariableBindings());
        } catch (PduException e) {
            // Timeout errors end up here
            throw new SnmpException(e.getMessage());
        } catch (IOException e) {
            throw new SnmpException(e.getMessage());
        }
    }

    /**
     * Adds an OID to a blocking PDU.
     *
     * @param pdu            the blocking PDU
     * @param oid            the OID to add
     *
     * @throws SnmpException if the OID couldn't be added correctly
     */
    private void addOid(BlockPdu pdu, String oid) throws SnmpException {
        try {
            pdu.addOid(oid);
        } catch (IllegalArgumentException e) {
            throw new SnmpException(e.getMessage());
        }
    }

    /**
     * Adds an OID to a blocking PDU.
     *
     * @param pdu            the blocking PDU
     * @param oid            the OID to add
     * @param value          the associated value
     *
     * @throws SnmpException if the OID couldn't be added correctly
     */
    private void addOid(BlockPdu pdu, String oid, AsnObject value)
        throws SnmpException {

        try {
            pdu.addOid(oid, value);
        } catch (IllegalArgumentException e) {
            throw new SnmpException(e.getMessage());
        }
    }

    /**
     * Creates an ASN.1 value object for an SNMP set request. The
     * value object will be created based on the MibType and string
     * value in the request. If the value cannot be converted to the
     * correct data type, an exception is thrown. 
     *
     * @param request        the request object
     *
     * @return the value object
     *
     * @throws SnmpException if the type is unsupported or if the
     *             value didn't match the type
     */
    public AsnObject createAsnValue(SnmpRequest request)
        throws SnmpException {

        MibType  type = request.getType();
        String   value = request.getValue();

        if (type.hasTag(MibTypeTag.UNIVERSAL_CATEGORY, 2)) {
            // INTEGER & Integer32
            return new AsnInteger(parseInteger(value));
        } else if (type.hasTag(MibTypeTag.UNIVERSAL_CATEGORY, 4)) {
            // OCTET STRING
            return new AsnOctets(value);
        } else if (type.hasTag(MibTypeTag.UNIVERSAL_CATEGORY, 6)) {
            // OBJECT IDENTIFIER
            return new AsnObjectId(value);
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 0)) {
            // IPAddress
            return new AsnOctets(parseInetAddress(value));
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 1)) {
            // Counter
            return new AsnUnsInteger(parseInteger(value));
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 2)) {
            // Gauge
            return new AsnUnsInteger(parseInteger(value));
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 3)) {
            // TimeTicks
            return new AsnUnsInteger(parseInteger(value));
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 4)) {
            // Opaque
            return new AsnOctets(value);
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 6)) {
            // Counter64
            return new AsnUnsInteger64(parseLong(value));
        } else {
            throw new SnmpException("Unsupported MIB type: " + type);
        }
    }

    /**
     * Parses an integer string.
     *
     * @param value          the value string
     *
     * @return the integer value
     *
     * @throws SnmpException if the value couldn't be parsed
     *             correctly
     */
    private int parseInteger(String value) throws SnmpException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new SnmpException("Value not numeric: " + value);
        }
    }

    /**
     * Parses a long integer string.
     *
     * @param value          the value string
     *
     * @return the long integer value
     *
     * @throws SnmpException if the value couldn't be parsed
     *             correctly
     */
    private long parseLong(String value) throws SnmpException {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new SnmpException("Value not numeric: " + value);
        }
    }

    /**
     * Parses an ip address or host string.
     *
     * @param value          the value string
     *
     * @return the resolved ip address value
     *
     * @throws SnmpException if the value couldn't be parsed
     *             correctly
     */
    private InetAddress parseInetAddress(String value)
        throws SnmpException {

        try {
            return InetAddress.getByName(value);
        } catch (java.net.UnknownHostException e) {
            throw new SnmpException("Invalid hostname or IP address: " +
                                    value);
        }
    }
}
