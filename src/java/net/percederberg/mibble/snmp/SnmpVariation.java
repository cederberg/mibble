/*
 * SnmpVariation.java
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

/**
 * An SNMP module variation value. This declaration is used inside a
 * module support declaration.
 * 
 * @see SnmpModuleSupport 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class SnmpVariation {

    /**
     * The variation value.
     */
    private MibValue value;

    /**
     * The value syntax.
     */
    private MibType syntax;

    /**
     * The value write syntax.
     */
    private MibType writeSyntax;

    /**
     * The access mode.
     */    
    private SnmpAccess access;

    /**
     * The cell values required for creation.
     */
    private ArrayList requiredCells;
    
    /**
     * The default value.
     */
    private MibValue defaultValue;
    
    /**
     * The variation description.
     */
    private String description;

    /**
     * Creates a new SNMP module variation.
     * 
     * @param value          the variation value
     * @param syntax         the value syntax, or null 
     * @param writeSyntax    the value write syntax, or null
     * @param access         the access mode, or null
     * @param requiredCells  the cell values required for creation
     * @param defaultValue   the default value, or null
     * @param description    the variation description
     */
    public SnmpVariation(MibValue value,
                         MibType syntax,
                         MibType writeSyntax,
                         SnmpAccess access,
                         ArrayList requiredCells,
                         MibValue defaultValue,
                         String description) {

        this.value = value;
        this.syntax = syntax;
        this.writeSyntax = writeSyntax;
        this.access = access;
        this.requiredCells = requiredCells;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    /**
     * Initializes the object. This will remove all levels of
     * indirection present, such as references to other types, and 
     * returns the basic type. No type information is lost by this 
     * operation. This method may modify this object as a 
     * side-effect, and will be called by the MIB loader.
     * 
     * @param log            the MIB loader log
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    void initialize(MibLoaderLog log) throws MibException {
        ArrayList  list = new ArrayList();

        value = value.initialize(log);
        if (syntax != null) {
            syntax = syntax.initialize(null, log);
        }
        if (writeSyntax != null) {
            writeSyntax = writeSyntax.initialize(null, log);
        }
        for (int i = 0; i < requiredCells.size(); i++) {
            list.add(((MibValue) requiredCells.get(i)).initialize(log));
        }
        this.requiredCells = list;
        if (defaultValue != null) {
            defaultValue = defaultValue.initialize(log);
        }
    }

    /**
     * Returns the value.
     * 
     * @return the value
     */
    public MibValue getValue() {
        return value;
    }

    /**
     * Returns the value syntax.
     * 
     * @return the value syntax, or 
     *         null if not set
     */
    public MibType getSyntax() {
        return syntax;
    }

    /**
     * Returns the value write syntax.
     * 
     * @return the value write syntax, or 
     *         null if not set
     */
    public MibType getWriteSyntax() {
        return writeSyntax;
    }

    /**
     * Returns the access mode.
     * 
     * @return the access mode, or
     *         null if not set
     */
    public SnmpAccess getAccess() {
        return access;
    }

    /**
     * Returns cell values required for creation. The returned list
     * will consist of MibValue instances.
     * 
     * @return cell values required for creation
     * 
     * @see net.percederberg.mibble.MibValue
     */
    public ArrayList getRequiredCells() {
        return requiredCells;
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
     * Returns the variation description.
     * 
     * @return the variation description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();
        
        buffer.append(value);
        if (syntax != null) {
            buffer.append("\n      Syntax: ");
            buffer.append(syntax);
        }
        if (writeSyntax != null) {
            buffer.append("\n      Write-Syntax: ");
            buffer.append(writeSyntax);
        }
        if (access != null) {
            buffer.append("\n      Access: ");
            buffer.append(access);
        }
        if (requiredCells.size() > 0) {
            buffer.append("\n      Creation-Requires: ");
            buffer.append(requiredCells);
        }
        if (defaultValue != null) {
            buffer.append("\n      Default Value: ");
            buffer.append(defaultValue);
        }
        buffer.append("\n      Description: ");
        buffer.append(description);
        return buffer.toString();
    }
}
