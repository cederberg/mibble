/*
 * SnmpTrapType.java
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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
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
 * SNMPv1 and is defined in RFC 1215. In SNMPv2 and later, the 
 * notification type macro should be used instead. 
 * 
 * @see SnmpNotificationType
 * @see <a href="http://www.ietf.org/rfc/rfc1215.txt">RFC 1215 (RFC-1215)</a>
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class SnmpTrapType extends MibType {

    /**
     * The enterprise value.
     */
    private MibValue enterprise;

    /**
     * The list of MIB values.
     */
    private ArrayList variables;

    /**
     * The type description.
     */
    private String description;
    
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
                        ArrayList variables,
                        String description,
                        String reference) {

        super("TRAP-TYPE", false);
        this.enterprise = enterprise;
        this.variables = variables;
        this.description = description;
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

        if (!(symbol instanceof MibValueSymbol)) {
            throw new MibException(symbol.getLocation(), 
                                   "only values can have the " +
                                   getName() + " type");
        }
        enterprise = enterprise.initialize(log);
        for (int i = 0; i < variables.size(); i++) {
            list.add(((MibValue) variables.get(i)).initialize(log));
        }
        variables = list;
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
    public ArrayList getVariables() {
        return variables;
    }

    /**
     * Returns the type description.
     * 
     * @return the type description, or 
     *         null if no description has been set
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
        buffer.append("\n  Enterprise: ");
        buffer.append(enterprise);
        buffer.append("\n  Variables: ");
        buffer.append(variables);
        if (description != null) {
            buffer.append("\n  Description: ");
            buffer.append(description);
        }
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        buffer.append("\n)");
        return buffer.toString();
    }
}
