/*
 * SnmpModuleIdentity.java
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
 * An SNMP module identity. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class SnmpModuleIdentity extends MibType {

    /**
     * The last updated date.
     */
    private String lastUpdated;
    
    /**
     * The organization name.
     */
    private String organization;

    /**
     * The organization contact information.
     */
    private String contactInfo;

    /**
     * The module description.
     */
    private String description;

    /**
     * The list of SNMP revision objects.
     */
    private ArrayList revisions;

    /**
     * Creates a new SNMP module identity. 
     * 
     * @param lastUpdated    the last updated date
     * @param organization   the organization name
     * @param contactInfo    the organization contact information
     * @param description    the module description
     * @param revisions      the list of module revisions
     */
    public SnmpModuleIdentity(String lastUpdated, 
                              String organization, 
                              String contactInfo,
                              String description,
                              ArrayList revisions) {
                                  
        super("MODULE-IDENTITY", false);
        this.lastUpdated = lastUpdated;
        this.organization = organization;
        this.contactInfo = contactInfo;
        this.description = description;
        this.revisions = revisions;
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
        SnmpRevision  rev;

        for (int i = 0; i < revisions.size(); i++) {
            rev = (SnmpRevision) revisions.get(i);
            rev.initialize(log);
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
     * Returns the last updated date.
     * 
     * @return the last updated date
     */
    public String getLastUpdated() {
        return lastUpdated;
    }
    
    /**
     * Returns the organization name.
     * 
     * @return the organization name
     */
    public String getOrganization() {
        return organization;
    }
    
    /**
     * Returns the organization contact information.
     * 
     * @return the organization contact information
     */
    public String getContactInfo() {
        return contactInfo;
    }
    
    /**
     * Returns the module description.
     * 
     * @return the module description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a list of all the SNMP module revisions.
     * 
     * @return a list of all the SNMP module revisions
     * 
     * @see SnmpRevision
     */
    public ArrayList getRevisions() {
        return revisions;
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
        buffer.append("\n  Last Updated: ");
        buffer.append(lastUpdated);
        buffer.append("\n  Organization: ");
        buffer.append(organization);
        buffer.append("\n  Contact Info: ");
        buffer.append(contactInfo);
        buffer.append("\n  Description: ");
        buffer.append(description);
        for (int i = 0; i < revisions.size(); i++) {
            buffer.append("\n  Revision: ");
            buffer.append(revisions.get(i));
        }
        buffer.append("\n)");
        return buffer.toString();
    }
}
