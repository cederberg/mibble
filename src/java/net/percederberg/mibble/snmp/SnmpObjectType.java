/*
 * SnmpObjectType.java
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
 * An SNMP object type. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class SnmpObjectType implements MibType {

    /**
     * The type syntax.
     */
    private MibType syntax;

    /**
     * The units description.
     */
    private String units;

    /**
     * The access mode.
     */    
    private SnmpAccess access;
    
    /**
     * The type status.
     */
    private SnmpStatus status;

    /**
     * The type description.
     */
    private String description;
    
    /**
     * The type reference.
     */
    private String reference;
    
    /**
     * The list of index values or types.
     */
    private ArrayList index;
    
    /**
     * The index augments value.
     */
    private MibValue augments;

    /**
     * The default value.
     */
    private MibValue defaultValue;

    /**
     * Creates a new SNMP object type. 
     * 
     * @param syntax         the object type syntax
     * @param units          the units description, or null
     * @param access         the access mode
     * @param status         the type status
     * @param description    the type description, or null
     * @param reference      the type reference, or null
     * @param index          the list of index values or types
     * @param defaultValue   the default value, or null
     */
    public SnmpObjectType(MibType syntax, 
                          String units, 
                          SnmpAccess access, 
                          SnmpStatus status,
                          String description,
                          String reference,
                          ArrayList index,
                          MibValue defaultValue) {

        this.syntax = syntax;
        this.units = units;
        this.access = access;
        this.status = status;
        this.description = description;
        this.reference = reference;
        this.index = index;
        this.augments = null;
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a new SNMP object type. 
     * 
     * @param syntax         the object type syntax
     * @param units          the units description, or null
     * @param access         the access mode
     * @param status         the type status
     * @param description    the type description, or null
     * @param reference      the type reference, or null
     * @param augments       the index augments value
     * @param defaultValue   the default value, or null
     */
    public SnmpObjectType(MibType syntax, 
                          String units, 
                          SnmpAccess access, 
                          SnmpStatus status,
                          String description,
                          String reference,
                          MibValue augments,
                          MibValue defaultValue) {

        this.syntax = syntax;
        this.units = units;
        this.access = access;
        this.status = status;
        this.description = description;
        this.reference = reference;
        this.index = new ArrayList();
        this.augments = augments;
        this.defaultValue = defaultValue;
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
        ArrayList  list = new ArrayList();
        Object     obj;

        syntax = syntax.initialize(log);
        for (int i = 0; i < index.size(); i++) {
            obj = index.get(i);
            if (obj instanceof MibValue) {
                list.add(((MibValue) obj).initialize(log));
            } else {
                list.add(((MibType) obj).initialize(log));
            }
        }
        this.index = list;
        if (augments != null) {
            augments = augments.initialize(log);
        }
        if (defaultValue != null) {
            defaultValue = defaultValue.initialize(log);
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
     * Returns the type syntax.
     * 
     * @return the type syntax
     */
    public MibType getSyntax() {
        return syntax;
    }
    
    /**
     * Returns the units description.
     * 
     * @return the units description, or
     *         null if no units has been set
     */
    public String getUnits() {
        return units;
    }
    
    /**
     * Returns the access mode.
     * 
     * @return the access mode
     */
    public SnmpAccess getAccess() {
        return access;
    }
    
    /**
     * Returns the type status.
     * 
     * @return the type status
     */
    public SnmpStatus getStatus() {
        return status;
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
     * Returns the list of index values or types.
     * 
     * @return the list of index values or types
     * 
     * @see net.percederberg.mibble.MibValue
     * @see net.percederberg.mibble.MibType
     */
    public ArrayList getIndex() {
        return index;
    }
    
    /**
     * Returns the augmented index value.
     * 
     * @return the augmented index value, or
     *         null if no augments index is used
     */
    public MibValue getAugments() {
        return augments;
    }
    
    /**
     * Returns the default value.
     * 
     * @return the default value, or
     *         null if no default value has been set
     */
    public MibValue getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append("OBJECT-TYPE (");
        buffer.append("\n  Syntax: ");
        buffer.append(syntax);
        if (units != null) {
            buffer.append("\n  Units: ");
            buffer.append(units);
        }
        buffer.append("\n  Access: ");
        buffer.append(access);
        buffer.append("\n  Status: ");
        buffer.append(status);
        if (description != null) {
            buffer.append("\n  Description: ");
            buffer.append(description);
        }
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        if (index.size() > 0) {
            buffer.append("\n  Index: ");
            buffer.append(index);
        }
        if (augments != null) {
            buffer.append("\n  Augments: ");
            buffer.append(augments);
        }
        if (defaultValue != null) {
            buffer.append("\n  Default Value: ");
            buffer.append(defaultValue);
        }
        buffer.append("\n)");
        return buffer.toString();
    }
}
