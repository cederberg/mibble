/*
 * ObjectIdentifierValue.java
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

package net.percederberg.mibble.value;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;

/**
 * An object identifier value. This class stores the component
 * identifier values in a tree hierarchy.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.3
 * @since    2.0
 */
public class ObjectIdentifierValue extends MibValue {

    /**
     * The component parent.
     */
    private MibValue parent;

    /**
     * The component children.
     */
    private ArrayList children = new ArrayList();

    /**
     * The object identifier component name.
     */
    private String name;

    /**
     * The object identifier component value.
     */
    private int value;

    /**
     * The MIB value symbol referenced by this object identifier.
     */
    private MibValueSymbol symbol = null;

    /**
     * Creates a new root object identifier value.
     *
     * @param name           the component name, or null
     * @param value          the component value
     */
    public ObjectIdentifierValue(String name, int value) {
        super("OBJECT IDENTIFIER");
        this.parent = null;
        this.name = name;
        this.value = value;
    }

    /**
     * Creates a new object identifier value.
     *
     * @param parent         the component parent
     * @param name           the component name, or null
     * @param value          the component value
     */
    public ObjectIdentifierValue(ObjectIdentifierValue parent,
                                 String name,
                                 int value) {

        super("OBJECT IDENTIFIER");
        this.parent = parent;
        this.name = name;
        this.value = value;
        parent.addChild(this);
    }

    /**
     * Creates a new object identifier value.
     *
     * @param parent         the component parent
     * @param name           the component name, or null
     * @param value          the component value
     */
    public ObjectIdentifierValue(ValueReference parent,
                                 String name,
                                 int value) {

        super("OBJECT IDENTIFIER");
        this.parent = parent;
        this.name = name;
        this.value = value;
    }

    /**
     * Initializes the MIB value. This will remove all levels of
     * indirection present, such as references to other values. No
     * value information is lost by this operation. This method may
     * modify this object as a side-effect, and will return the basic
     * value.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param log            the MIB loader log
     *
     * @return the basic MIB value
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public MibValue initialize(MibLoaderLog log) throws MibException {
        ValueReference  ref = null;

        if (parent == null) {
            return this;
        } else if (parent instanceof ValueReference) {
            ref = (ValueReference) parent;
        }
        parent = parent.initialize(log);
        if (ref != null) {
            if (parent instanceof ObjectIdentifierValue) {
                ((ObjectIdentifierValue) parent).addChild(this);
            } else {
                throw new MibException(ref.getLocation(),
                                       "referenced value is not an " +
                                       "object identifier");
            }
        }
        return this;
    }

    /**
     * Creates a value reference to this value. The value reference
     * is normally an identical value. Only certain values support
     * being referenced, and the default implementation of this
     * method throws an exception.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @return the MIB value reference
     *
     * @since 2.2
     */
    public MibValue createReference() {
        return this;
    }

    /**
     * Checks if this object equals another object. This method will
     * compare the string representations for equality.
     *
     * @param obj            the object to compare with
     *
     * @return true if the objects are equal, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code for this object
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Returns the parent object identifier value.
     *
     * @return the parent object identifier value, or
     *         null if no parent exists
     */
    public ObjectIdentifierValue getParent() {
        if (parent != null && parent instanceof ObjectIdentifierValue) {
            return (ObjectIdentifierValue) parent;
        } else {
            return null;
        }
    }

    /**
     * Returns this object identifier component name.
     *
     * @return the object identifier component name, or
     *         null if the component has no name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns this object identifier component value.
     *
     * @return the object identifier component value
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the symbol connected to this object identifier.
     *
     * @return the symbol connected to this object identifier, or
     *         null if no value symbol is connected
     */
    public MibValueSymbol getSymbol() {
        return symbol;
    }

    /**
     * Sets the symbol connected to this object identifier. This
     * method is called during the value symbol initialization.
     *
     * @param symbol         the value symbol
     */
    public void setSymbol(MibValueSymbol symbol) {
        if (name == null) {
            name = symbol.getName();
        }
        this.symbol = symbol;
    }

    /**
     * Returns the number of child object identifier values.
     *
     * @return the number of child object identifier values
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * Returns a child object identifier value. The children are
     * ordered by their value, not necessarily in the order in which
     * they appear in the original MIB file.
     *
     * @param index          the child position, 0 <= index < count
     *
     * @return the child object identifier value, or
     *         null if not found
     */
    public ObjectIdentifierValue getChild(int index) {
        return (ObjectIdentifierValue) children.get(index);
    }

    /**
     * Returns an array of all child object identifier values. The
     * children are ordered by their value, not necessarily in the
     * order in which they appear in the original MIB file.
     *
     * @return the child object identifier values
     *
     * @since 2.3
     */
    public ObjectIdentifierValue[] getAllChildren() {
        ObjectIdentifierValue[]  values;

        values = new ObjectIdentifierValue[children.size()];
        children.toArray(values);
        return values;
    }

    /**
     * Adds a child component. The children will be inserted in the
     * value order. If a child has already been added, it will be
     * ignored.
     *
     * @param child          the child component
     */
    private void addChild(ObjectIdentifierValue child) {
        ObjectIdentifierValue  value;
        int                    i = children.size();

        // Insert child in value order, searching backwards to 
        // optimize the most common case (ordered insertion)
        while (i > 0) {
            value = (ObjectIdentifierValue) children.get(i - 1);
            if (value.getValue() == child.getValue()) {
                return;
            } else if (value.getValue() < child.getValue()) {
                break;
            }
            i--;
        }
        children.add(i, child);
    }

    /**
     * Returns a string representation of this value. The string will
     * contain the full numeric object identifier value with each
     * component separated with a dot ('.').
     *
     * @return a string representation of this value
     */
    public Object toObject() {
        return toString();
    }

    /**
     * Returns a string representation of this value. The string will
     * contain the full numeric object identifier value with each
     * component separated with a dot ('.').
     *
     * @return a string representation of this value
     */
    public String toString() {
        if (parent == null) {
            return String.valueOf(value);
        } else {
            return parent.toString() + "." + String.valueOf(value);
        }
    }

    /**
     * Returns a detailed string representation of this value. The
     * string will contain the full numeric object identifier value
     * with optional names for each component.
     *
     * @return a detailed string representation of this value
     */
    public String toDetailString() {
        StringBuffer  buffer = new StringBuffer();

        if (parent instanceof ObjectIdentifierValue) {
            buffer.append(((ObjectIdentifierValue) parent).toDetailString());
            buffer.append(".");
        }
        if (name == null) {
            buffer.append(value);
        } else {
            buffer.append(name);
            buffer.append("(");
            buffer.append(value);
            buffer.append(")");
        }
        return buffer.toString();
    }
}
