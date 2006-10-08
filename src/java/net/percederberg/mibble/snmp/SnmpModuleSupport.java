/*
 * SnmpModuleSupport.java
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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibValue;

/**
 * An SNMP module support value. This declaration is used inside the
 * agent capabilities type.
 *
 * @see SnmpAgentCapabilities
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.8
 * @since    2.0
 */
public class SnmpModuleSupport {

    /**
     * The module name.
     */
    private String module;

    /**
     * The list of included group values.
     */
    private ArrayList groups;

    /**
     * The list of variations.
     */
    private ArrayList variations;

    /**
     * Creates a new module support declaration.
     *
     * @param module         the module name, or null
     * @param groups         the list of included group values
     * @param variations     the list of variations
     */
    public SnmpModuleSupport(String module,
                             ArrayList groups,
                             ArrayList variations) {

        this.module = module;
        this.groups = groups;
        this.variations = variations;
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
        int        i;

        for (i = 0; i < groups.size(); i++) {
            list.add(((MibValue) groups.get(i)).initialize(log, null));
        }
        this.groups = list;
        for (i = 0; i < variations.size(); i++) {
            try {
                ((SnmpVariation) variations.get(i)).initialize(log);
            } catch (MibException e) {
                log.addError(e.getLocation(), e.getMessage());
            }
        }
    }

    /**
     * Returns the module name.
     *
     * @return the module name, or
     *         null if not set
     */
    public String getModule() {
        return module;
    }

    /**
     * Returns the list of included group values. The returned list
     * will consist of MibValue instances.
     *
     * @return the list of included group values
     *
     * @see net.percederberg.mibble.MibValue
     */
    public ArrayList getGroups() {
        return groups;
    }

    /**
     * Returns the list of variations. The returned list will consist
     * of SnmpVariation instances.
     *
     * @return the list of variations
     *
     * @see SnmpVariation
     */
    public ArrayList getVariations() {
        return variations;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        if (module != null) {
            buffer.append(module);
        }
        buffer.append("\n    Includes: ");
        buffer.append(groups);
        for (int i = 0; i < variations.size(); i++) {
            buffer.append("\n    Variation: ");
            buffer.append(variations.get(i));
        }
        return buffer.toString();
    }
}
