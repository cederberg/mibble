/*
 * MibType.java
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

package net.percederberg.mibble;

import java.util.ArrayList;

import net.percederberg.mibble.type.Constraint;

/**
 * The base MIB type class. All the basic MIB types can also be 
 * cloned, and implements the Cloneable interface. Some of the more
 * complex SNMP types cannot be cloned, however, which is the reason
 * for this base class not supporting cloning. 
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public abstract class MibType {

    /**
     * The type name.
     */
    private String name;

    /**
     * The primitive type flag.
     */
    private boolean primitive;

    /**
     * The type tag.
     */
    private MibTypeTag tag = null;

    /**
     * Creates a new MIB type instance.
     * 
     * @param name           the type name
     * @param primitive      the primitive type flag
     */
    public MibType(String name, boolean primitive) {
        this.name = name;
        this.primitive = primitive;
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
    public abstract MibType initialize(MibLoaderLog log) 
        throws MibException;

    /**
     * Creates a type reference to this type. The type reference is
     * normally an identical type, but with the primitive flag set to 
     * false. Only certain types support being referenced, and the
     * default implementation of this method throws an exception. 
     * 
     * @return the MIB type reference
     * 
     * @throws UnsupportedOperationException if a type reference 
     *             couldn't be created
     * 
     * @since 2.2
     */
    public MibType createReference() 
        throws UnsupportedOperationException {

        String msg = name + " type cannot be referenced";
        throw new UnsupportedOperationException(msg); 
    }

    /**
     * Creates a constrained type reference to this type. The type 
     * reference is normally an identical type, but with the 
     * primitive flag set to false. Only certain types support being 
     * referenced, and the default implementation of this method 
     * throws an exception. 
     *
     * @param constraint     the type constraint
     *  
     * @return the MIB type reference
     * 
     * @throws UnsupportedOperationException if a type reference 
     *             couldn't be created with constraints
     * 
     * @since 2.2
     */
    public MibType createReference(Constraint constraint) 
        throws UnsupportedOperationException {

        String msg = name + " type cannot be referenced with constraints";
        throw new UnsupportedOperationException(msg); 
    }

    /**
     * Creates a constrained type reference to this type. The type 
     * reference is normally an identical type, but with the 
     * primitive flag set to false. Only certain types support being 
     * referenced, and the default implementation of this method 
     * throws an exception. 
     *
     * @param values         the type value symbols
     *  
     * @return the MIB type reference
     * 
     * @throws UnsupportedOperationException if a type reference 
     *             couldn't be created with value constraints
     * 
     * @since 2.2
     */
    public MibType createReference(ArrayList values) 
        throws UnsupportedOperationException {

        String msg = name + " type cannot be referenced with " +
                     "defined values";
        throw new UnsupportedOperationException(msg); 
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if it has a type that matches this one and
     * a value that satisfies all constraints. 
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public abstract boolean isCompatible(MibValue value);
    
    /**
     * Checks if this type represents a primitive type. The primitive
     * types are the basic building blocks of the ASN.1 type system.
     * By defining new types (that may be identical to a primitive 
     * type), the new type looses it's primitive status.
     * 
     * @return true if this type represents a primitive type, or
     *         false otherwise
     * 
     * @since 2.2
     */
    public boolean isPrimitive() {
        return primitive;
    }

    /**
     * Returns the type tag.
     * 
     * @return the type tag, or
     *         null if no type tag has been set 
     * 
     * @since 2.2
     */
    public MibTypeTag getTag() {
        return tag;
    }

    /**
     * Sets the type tag. The old type tag is kept to some extent,
     * depending on if the implicit flag is set to true or false. For
     * implicit inheritance, the first tag in the old tag chain is 
     * replaced with the new tag. For explicit inheritance, the new
     * tag is added first in the tag chain without removing any old 
     * tag.
     *
     * @param implicit       the implicit inheritance flag
     * @param tag            the new type tag
     * 
     * @since 2.2
     */
    public void setTag(boolean implicit, MibTypeTag tag) {
        MibTypeTag  next = this.tag;

        if (implicit && next != null) {
            next = next.getNext();
        }
        if (tag != null) {
            tag.setNext(next);
        }
        this.tag = tag; 
    }

    /**
     * Returns a string representation of this object.
     * 
     * @return a string representation of this object
     */
    public String toString() {
        if (tag != null) {
            return tag.toString() + " " + name;
        } else {
            return name;
        }
    }
}
