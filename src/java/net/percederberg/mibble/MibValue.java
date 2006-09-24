/*
 * MibValue.java
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

package net.percederberg.mibble;

/**
 * The base MIB value class. There are only a few MIB value classes,
 * each corresponding to a primitive ASN.1 type. To extract the basic
 * Java representation of the MIB value, the toObject() method should
 * be used.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.8
 * @since    2.0
 */
public abstract class MibValue implements Comparable {

    /**
     * The value name.
     */
    private String name;

    /**
     * The value reference symbol. This is set to the referenced
     * value symbol when resolving this value.
     */
    private MibValueSymbol reference = null;

    /**
     * Creates a new MIB value instance.
     *
     * @param name           the value name
     */
    protected MibValue(String name) {
        this.name = name;
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
    public abstract MibValue initialize(MibLoaderLog log, MibType type)
        throws MibException;

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
     * @throws UnsupportedOperationException if a value reference
     *             couldn't be created
     *
     * @since 2.2
     */
    public MibValue createReference()
        throws UnsupportedOperationException {

        String msg = name + " value cannot be referenced";
        throw new UnsupportedOperationException(msg);
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
        reference = null;
    }

    /**
     * Checks if this value referenced the specified value symbol.
     *
     * @param name           the value symbol name
     *
     * @return true if this value was a reference to the symbol, or
     *         false otherwise
     *
     * @since 2.2
     */
    public boolean isReferenceTo(String name) {
        if (reference == null) {
            return false;
        } else if (reference.getName().equals(name)) {
            return true;
        } else {
            return reference.getValue().isReferenceTo(name);
        }
    }

    /**
     * Checks if this value referenced the specified value symbol.
     *
     * @param module         the value symbol module (MIB) name
     * @param name           the value symbol name
     *
     * @return true if this value was a reference to the symbol, or
     *         false otherwise
     *
     * @since 2.2
     */
    public boolean isReferenceTo(String module, String name) {
        Mib  mib;

        if (reference == null) {
            return false;
        }
        mib = reference.getMib();
        if (mib.getName().equals(module)
         && reference.getName().equals(name)) {

            return true;
        } else {
            return reference.getValue().isReferenceTo(module, name);
        }
    }

    /**
     * Returns the value name.
     *
     * @return the value name, or
     *         an empty string if not applicable
     *
     * @since 2.2
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value reference symbol. A value reference is
     * created whenever a value is defined in a value assignment, and
     * later referenced by name from some other symbol. The complete
     * chain of value references is available by calling
     * getReference() recursively on the value of the returned value
     * symbol.
     *
     * @return the value reference symbol, or
     *         null if this value never referenced another value
     *
     * @since 2.2
     */
    public MibValueSymbol getReferenceSymbol() {
        return reference;
    }

    /**
     * Sets the value reference symbol. The value reference is set
     * whenever a value is defined in a value assignment, and later
     * referenced by name from some other symbol.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param symbol         the referenced value symbol
     *
     * @since 2.2
     */
    public void setReferenceSymbol(MibValueSymbol symbol) {
        this.reference = symbol;
    }

    /**
     * Returns a Java object representation of this value.
     *
     * @return a Java object representation of this value
     */
    public abstract Object toObject();
}
