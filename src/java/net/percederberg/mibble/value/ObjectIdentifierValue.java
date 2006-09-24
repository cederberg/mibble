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
 * Copyright (c) 2004-2006 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.value;

import java.util.ArrayList;

import net.percederberg.mibble.FileLocation;
import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;

/**
 * An object identifier value. This class stores the component
 * identifier values in a tree hierarchy.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.8
 * @since    2.0
 */
public class ObjectIdentifierValue extends MibValue {

    /**
     * The declaration file location. This variable is only used when
     * resolving value references in order to present correct error
     * messages. After initialization it is set to null to minimize
     * memory impact.
     *
     * @since 2.5
     */
    private FileLocation location = null;

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
     * The cached numeric string representation of this value. This
     * variable is set when calling the toString() method the first
     * time and is used to optimize performance by avoiding any
     * subsequent recursive calls.
     *
     * @see #toString()
     */
    private String cachedNumericValue = null;

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
     * @param location       the declaration file location
     * @param parent         the component parent
     * @param name           the component name, or null
     * @param value          the component value
     *
     * @throws MibException if the object identifier parent already
     *             had a child with the specified value
     */
    public ObjectIdentifierValue(FileLocation location,
                                 ObjectIdentifierValue parent,
                                 String name,
                                 int value)
        throws MibException {

        super("OBJECT IDENTIFIER");
        this.parent = parent;
        this.name = name;
        this.value = value;
        if (parent.getChildByValue(value) != null) {
            throw new MibException(location,
                                   "cannot add duplicate OID " +
                                   "children with value " + value);
        }
        parent.addChild(null, location, this);
    }

    /**
     * Creates a new object identifier value.
     *
     * @param location       the declaration file location
     * @param parent         the component parent
     * @param name           the component name, or null
     * @param value          the component value
     */
    public ObjectIdentifierValue(FileLocation location,
                                 ValueReference parent,
                                 String name,
                                 int value) {

        super("OBJECT IDENTIFIER");
        this.location = location;
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
     * @param type           the value type
     *
     * @return the basic MIB value
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public MibValue initialize(MibLoaderLog log, MibType type)
        throws MibException {

        ValueReference         ref = null;
        ObjectIdentifierValue  oid;

        if (parent == null) {
            return this;
        } else if (parent instanceof ValueReference) {
            ref = (ValueReference) parent;
        }
        parent = parent.initialize(log, type);
        if (ref != null) {
            if (parent instanceof ObjectIdentifierValue) {
                oid = (ObjectIdentifierValue) parent;
                oid.addChild(log, location, this);
            } else {
                throw new MibException(ref.getLocation(),
                                       "referenced value is not an " +
                                       "object identifier");
            }
        }
        location = null;
        if (parent instanceof ObjectIdentifierValue) {
            return ((ObjectIdentifierValue) parent).getChildByValue(value);
        } else {
            return this;
        }
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
     * Clears and prepares this value for garbage collection. This
     * method will recursively clear any associated types or values,
     * making sure that no data structures references this object.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     */
    protected void clear() {
        Mib                    mib;
        ArrayList              copy;
        ObjectIdentifierValue  child;

        // Recursively clear all children in same MIB
        if (children != null) {
            mib = getMib();
            copy = (ArrayList) children.clone();
            for (int i = 0; i < copy.size(); i++) {
                child = (ObjectIdentifierValue) copy.get(i);
                if (mib == null || mib == child.getMib()) {
                    child.clear();
                }
            }
        }

        // Remove parent reference if all children were cleared 
        if (getChildCount() <= 0) {
            if (parent != null) {
                getParent().children.remove(this);
                parent = null;
            }
            children = null;
        }

        // Clear other value data
        symbol = null;
        super.clear();
    }

    /**
     * Compares this object with the specified object for order. This
     * method will only compare the string representations with each
     * other.
     *
     * @param obj            the object to compare to
     *
     * @return less than zero if this object is less than the specified,
     *         zero if the objects are equal, or
     *         greater than zero otherwise
     *
     * @since 2.6
     */
    public int compareTo(Object obj) {
        return toString().compareTo(obj.toString());
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
     * Sets the symbol connected to this object identifier.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
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
     * Returns the MIB that this object identifer is connected to.
     * This method simply returns the symbol MIB.
     *
     * @return the symbol MIB, or
     *         null if no symbol has been set
     */
    private Mib getMib() {
        return symbol == null ? null : symbol.getMib();
    }

    /**
     * Returns the number of child object identifier values.
     *
     * @return the number of child object identifier values
     */
    public int getChildCount() {
        return children == null ? 0 : children.size();
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
     * Returns a child object identifier value. The children are
     * searched by their component names. This method uses linear
     * search and therefore has time complexity O(n). Note that most
     * OID:s don't have a component name, but only an associated
     * symbol.
     *
     * @param name           the child name
     *
     * @return the child object identifier value, or
     *         null if not found
     *
     * @since 2.5
     */
    public ObjectIdentifierValue getChildByName(String name) {
        ObjectIdentifierValue  child;

        for (int i = 0; i < children.size(); i++) {
            child = (ObjectIdentifierValue) children.get(i);
            if (name.equals(child.getName())) {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns a child object identifier value. The children are
     * searched by their numerical value. This method uses binary
     * search and therefore has time complexity O(log(n)).
     *
     * @param value          the child value
     *
     * @return the child object identifier value, or
     *         null if not found
     *
     * @since 2.5
     */
    public ObjectIdentifierValue getChildByValue(int value) {
        ObjectIdentifierValue  child;
        int                    low = 0;
        int                    high = children.size();
        int                    pos;

        if (low < value && value <= high) {
            // Default to that the value is really the index - 1 
            pos = value - 1;
        } else {
            // Otherwise use normal interval midpoint
            pos = (low + high) / 2;
        }
        while (low < high) {
            child = (ObjectIdentifierValue) children.get(pos);
            if (child.getValue() == value) {
                return child;
            } else if (child.getValue() < value) {
                low = pos + 1;
            } else {
                high = pos;
            }
            pos = (low + high) / 2;
        }
        return null;
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
     * value order. If a child with the same value has already been
     * added, the new child will be merged with the previous one (if
     * possible) and the resulting child will be returned.
     *
     * @param log            the MIB loader log
     * @param location       the file location on error
     * @param child          the child component
     *
     * @return the child object identifier value added
     *
     * @throws MibException if an irrecoverable conflict between two
     *             children occurred
     */
    private ObjectIdentifierValue addChild(MibLoaderLog log,
                                           FileLocation location,
                                           ObjectIdentifierValue child)
        throws MibException {

        ObjectIdentifierValue  value;
        int                    i = children.size();

        // Insert child in value order, searching backwards to 
        // optimize the most common case (ordered insertion)
        while (i > 0) {
            value = (ObjectIdentifierValue) children.get(i - 1);
            if (value.getValue() == child.getValue()) {
                value = value.merge(log, location, child);
                children.set(i - 1, value);
                return value;
            } else if (value.getValue() < child.getValue()) {
                break;
            }
            i--;
        }
        children.add(i, child);
        return child;
    }

    /**
     * Adds all the children from another object identifier value.
     * The children are not copied, but actually transfered from the
     * other value. If this value lacks a name component, it will be
     * set from other value. This operation thus corresponds to a
     * merge and thus can only be made under certain conditions. For
     * example, no child OID:s may have name conflicts. It is assumed
     * that the other OID has the same numerical value as this one.
     *
     * @param log            the MIB loader log
     * @param location       the file location on error
     * @param parent         the OID parent value for the children
     *
     * @throws MibException if an irrecoverable conflict between two
     *             children occurred
     */
    private void addChildren(MibLoaderLog log,
                             FileLocation location,
                             ObjectIdentifierValue parent)
        throws MibException {

        ObjectIdentifierValue  child;
        String                 msg;

        if (name == null) {
            name = parent.name;
        } else if (parent.name != null && !parent.name.equals(name)) {
            msg = "OID component '" + parent.name + "' was previously " +
                  "defined as '" + name + "'";
            if (log == null) {
                throw new MibException(location, msg);
            } else {
                log.addWarning(location, msg);
            }
        }
        if (parent.symbol != null) {
            throw new MibException(location,
                                   "INTERNAL ERROR: OID merge with " +
                                   "symbol reference already set");
        }
        for (int i = 0; i < parent.children.size(); i++) {
            child = (ObjectIdentifierValue) parent.children.get(i);
            child.parent = this;
            addChild(log, location, child);
        }
        parent.children = null;
    }

    /**
     * Merges this object identifier value with another one. One of
     * the two objects will be discarded and the other will be used
     * as the merge destination and returned. Note that this
     * operation modifies both this value and the specified value.
     * The merge can only be made under certain conditions, for
     * example that no child OID:s have name conflicts. It is also
     * assumed that the two OID:s have the same numerical value.
     *
     * @param log            the MIB loader log
     * @param location       the file location on error
     * @param value          the OID value to merge with
     *
     * @return the merged object identifier value
     *
     * @throws MibException if the merge couldn't be performed due to
     *             some conflict or invalid state
     */
    private ObjectIdentifierValue merge(MibLoaderLog log,
                                        FileLocation location,
                                        ObjectIdentifierValue value)
        throws MibException {

        if (symbol != null || (value.symbol == null && children.size() > 0)) {
            addChildren(log, location, value);
            return this;
        } else {
            value.addChildren(log, location, this);
            return value;
        }
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
        StringBuffer  buffer;

        if (cachedNumericValue == null) {
            buffer = new StringBuffer();
            if (parent != null) {
                buffer.append(parent.toString());
                buffer.append(".");
            }
            buffer.append(value);
            cachedNumericValue = buffer.toString();
        }
        return cachedNumericValue;
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

    /**
     * Returns an ASN.1 representation of this value. The string will
     * contain references to any parent OID value that can be found.
     *
     * @return an ASN.1 representation of this value
     * 
     * @since 2.6
     */
    public String toAsn1String() {
        StringBuffer           buffer = new StringBuffer();
        ObjectIdentifierValue  ref;

        if (parent instanceof ObjectIdentifierValue) {
            ref = (ObjectIdentifierValue) parent;
            if (ref.getSymbol() == null) {
                buffer.append(ref.toAsn1String());
            } else {
                buffer.append(ref.getSymbol().getName());
            }
            buffer.append(" ");
        }
        if (name == null || getSymbol() != null) {
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
