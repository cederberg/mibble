/*
 * CompoundConstraint.java
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

package net.percederberg.mibble.type;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;

/**
 * A compound MIB type constraint. This class holds two constraints,
 * either one that must be compatible for this constraint to return
 * true. Effectively this class represents an OR composition of the
 * two constraints.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.6
 * @since    2.0
 */
public class CompoundConstraint implements Constraint {

    /**
     * The first constraint.
     */
    private Constraint first;

    /**
     * The second constraint.
     */
    private Constraint second;

    /**
     * Creates a new compound constraint.
     *
     * @param first          the first constraint
     * @param second         the second constraint
     */
    public CompoundConstraint(Constraint first, Constraint second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Initializes the constraint. This will remove all levels of
     * indirection present, such as references to types or values. No
     * constraint information is lost by this operation. This method
     * may modify this object as a side-effect, and will be called by
     * the MIB loader.
     *
     * @param type           the type to constrain
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public void initialize(MibType type, MibLoaderLog log)
        throws MibException {

        first.initialize(type, log);
        second.initialize(type, log);
    }

    /**
     * Checks if the specified type is compatible with this
     * constraint. The type is considered compatible if it is
     * compatible with both constraints.
     *
     * @param type            the type to check
     *
     * @return true if the type is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibType type) {
        return first.isCompatible(type) && second.isCompatible(type);
    }

    /**
     * Checks if the specified value is compatible with this
     * constraint set. The value is considered compatible if it is
     * compatible with either of the two constraints.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return first.isCompatible(value) || second.isCompatible(value);
    }

    /**
     * Returns a list of the constraints in this compound. All
     * compound constraints will be flattened out and their contents
     * will be added to the list.
     *
     * @return a list of the base constraints in the compound
     */
    public ArrayList getConstraintList() {
        ArrayList  list = new ArrayList();

        if (first instanceof CompoundConstraint) {
            list.addAll(((CompoundConstraint) first).getConstraintList());
        } else {
            list.add(first);
        }
        if (second instanceof CompoundConstraint) {
            list.addAll(((CompoundConstraint) second).getConstraintList());
        } else {
            list.add(second);
        }
        return list;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append(first.toString());
        buffer.append(" | ");
        buffer.append(second.toString());

        return buffer.toString();
    }
}
