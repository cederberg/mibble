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
import net.percederberg.mibble.value.NumberValue;

/**
 * The SNMP trap type macro. This macro type is only present in
 * SMIv1 and is defined in RFC 1215. In SMIv2 and later, the
 * notification type macro should be used instead.
 *
 * @see SnmpNotificationType
 * @see <a href="http://www.ietf.org/rfc/rfc1215.txt">RFC 1215 (RFC-1215)</a>
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SnmpTrapType extends SnmpType {

    /**
     * The enterprise value.
     */
    private MibValue enterprise;

    /**
     * The list of MIB values.
     */
    private ArrayList<MibValue> variables;

    /**
     * The type reference.
     */
    private String reference;

    /**
     * Creates a new SNMP trap type.
     *
     * @param enterprise     the enterprise value
     * @param variables      the list of MIB values
     * @param description    the type description, or null
     * @param reference      the type reference, or null
     */
    public SnmpTrapType(MibValue enterprise,
                        ArrayList<MibValue> variables,
                        String description,
                        String reference) {

        super("TRAP-TYPE", description);
        this.enterprise = enterprise;
        this.variables = variables;
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
        enterprise = enterprise.initialize(log, null);
        for (int i = 0; i < variables.size(); i++) {
            variables.set(i, variables.get(i).initialize(log, null));
        }
        return this;
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if and only if it is an integer number
     * value.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return value instanceof NumberValue
            && !(value.toObject() instanceof Float);
    }

    /**
     * Returns the enterprise value.
     *
     * @return the enterprise value
     */
    public MibValue getEnterprise() {
        return enterprise;
    }

    /**
     * Returns the list of MIB values.
     *
     * @return the list of MIB values
     *
     * @see net.percederberg.mibble.MibValue
     */
    public ArrayList<MibValue> getVariables() {
        return variables;
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
        StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append(" (");
        buffer.append("\n  Enterprise: ");
        buffer.append(enterprise);
        buffer.append("\n  Variables: ");
        buffer.append(variables);
        if (getUnformattedDescription() != null) {
            buffer.append("\n  Description: ");
            buffer.append(getDescription("               "));
        }
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        buffer.append("\n)");
        return buffer.toString();
    }
}
