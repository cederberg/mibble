/*
 * SnmpType.java
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

package net.percederberg.mibble.snmp;

import net.percederberg.mibble.MibType;

/**
 * The base SNMP macro type. This is an abstract type, meaning there
 * only exist instances of subclasses. It exists to provide methods
 * that are valid across all SNMP macro types.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.5
 */
public abstract class SnmpType extends MibType {

    /**
     * Creates a new SNMP macro type instance. This constructor can
     * only be called by subclasses.
     *
     * @param name           the type name
     */
    protected SnmpType(String name) {
        super(name, false);
    }

    /**
     * Returns the type description.
     *
     * @return the type description, or
     *         null if no description has been set
     */
    public abstract String getDescription();
}
