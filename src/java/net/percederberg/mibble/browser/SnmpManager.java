/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
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
import uk.co.westhawk.snmp.stack.SnmpConstants;
import uk.co.westhawk.snmp.stack.SnmpContextBasisFace;
import uk.co.westhawk.snmp.stack.SnmpContextPool;
import uk.co.westhawk.snmp.stack.SnmpContextv2cPool;
import uk.co.westhawk.snmp.stack.SnmpContextv3Face;
import uk.co.westhawk.snmp.stack.SnmpContextv3Pool;

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
 * @author   Per Cederberg
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
     * Creates a new SNMPv1 manager.
     *
     * @param host           the host name or IP address
     * @param port           the agent port
     * @param community      the community name
     *
     * @return the new SNMP manager created
     *
     * @throws SnmpException if an SNMP context pool couldn't be
     *             created from the specified values
     */
    public static SnmpManager createSNMPv1(String host,
                                           int port,
                                           String community)
        throws SnmpException {

        SnmpContextPool  pool;

        try {
            pool = new SnmpContextPool(host, port);
            pool.setCommunity(community);
            return new SnmpManager(pool);
        } catch (IOException e) {
            throw new SnmpException("SNMP communication error: " +
                                    e.getMessage());
        }
    }

    /**
     * Creates a new SNMPv2c manager.
     *
     * @param host           the host name or IP address
     * @param port           the agent port
     * @param community      the community name
     *
     * @return the new SNMP manager created
     *
     * @throws SnmpException if an SNMP context pool couldn't be
     *             created from the specified values
     */
    public static SnmpManager createSNMPv2c(String host,
                                            int port,
                                            String community)
        throws SnmpException {

        SnmpContextv2cPool  pool;

        try {
            pool = new SnmpContextv2cPool(host, port);
            pool.setCommunity(community);
            return new SnmpManager(pool);
        } catch (IOException e) {
            throw new SnmpException("SNMP communication error: " +
                                    e.getMessage());
        }
    }

    /**
     * Creates a new SNMPv3 manager.
     *
     * @param host           the host name or IP address
     * @param port           the agent port
     * @param contextName    the context name
     * @param contextEngine  the context engine id
     * @param userName       the user name
     * @param auth           the authentication parameters
     * @param privacy        the privacy parameters
     *
     * @return the new SNMP manager created
     *
     * @throws SnmpException if an SNMP context pool couldn't be
     *             created from the specified values
     */
    public static SnmpManager createSNMPv3(String host,
                                           int port,
                                           String contextName,
                                           String contextEngine,
                                           String userName,
                                           SnmpAuthentication auth,
                                           SnmpPrivacy privacy)
        throws SnmpException {

        SnmpContextv3Pool  pool;
        String             type;
        int                protocol;

        try {
            pool = new SnmpContextv3Pool(host, port);
            pool.setContextName(contextName);
            pool.setContextEngineId(contextEngine.getBytes());
            pool.setUserName(userName);
            pool.setUseAuthentication(auth != null);
            pool.setUsePrivacy(auth != null && privacy != null);
            if (auth != null) {
                type = auth.getType();
                if (type.equals(SnmpAuthentication.MD5_TYPE)) {
                    protocol = SnmpContextv3Face.MD5_PROTOCOL;
                    pool.setAuthenticationProtocol(protocol);
                } else if (type.equals(SnmpAuthentication.SHA1_TYPE)) {
                    protocol = SnmpContextv3Face.SHA1_PROTOCOL;
                    pool.setAuthenticationProtocol(protocol);
                } else {
                    throw new SnmpException("Unsupported authentication " +
                                            "protocol: " + type);
                }
                pool.setUserAuthenticationPassword(auth.getPassword());
            }
            if (auth != null && privacy != null) {
                type = privacy.getType();
                if (type.equals(SnmpPrivacy.DES_TYPE)) {
                    pool.setPrivacyProtocol(SnmpContextv3Face.DES_ENCRYPT);
                } else if (type.equals(SnmpPrivacy.AES_TYPE)) {
                    pool.setPrivacyProtocol(SnmpContextv3Face.AES_ENCRYPT);
                } else {
                    throw new SnmpException("Unsupported privacy " +
                                            "protocol: " + type);
                }
                pool.setUserPrivacyPassword(privacy.getPassword());
            }
            return new SnmpManager(pool);
        } catch (IOException e) {
            throw new SnmpException("SNMP communication error: " +
                                    e.getMessage());
        }
    }

    /**
     * Creates a new SNMP manager.
     *
     * @param context        the context pool to use
     */
    private SnmpManager(SnmpContextBasisFace context) {
        this.context = context;
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
        for (String oid : oids) {
            addOid(pdu, oid);
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
        for (String oid : oids) {
            addOid(pdu, oid);
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
        for (SnmpRequest req: requests) {
            addOid(pdu, req.getOid(), createAsnValue(req));
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
    private AsnObject createAsnValue(SnmpRequest request)
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
            return new AsnUnsInteger(parseInteger(value),
                                     SnmpConstants.COUNTER);
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 2)) {
            // Gauge
            return new AsnUnsInteger(parseInteger(value),
                                     SnmpConstants.GAUGE);
        } else if (type.hasTag(MibTypeTag.APPLICATION_CATEGORY, 3)) {
            // TimeTicks
            return new AsnUnsInteger(parseInteger(value),
                                     SnmpConstants.TIMETICKS);
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
