/*
 * SnmpTextualConvention.java
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

import net.percederberg.mibble.MibContext;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.type.Constraint;

/**
 * An SNMP textual convention.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class SnmpTextualConvention extends MibType implements MibContext {

    /**
     * The display hint.
     */
    private String displayHint;

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
     * The type syntax.
     */    
    private MibType syntax;

    /**
     * Creates a new SNMP textual convention.
     * 
     * @param displayHint    the display hint, or null
     * @param status         the type status
     * @param description    the type description
     * @param reference      the type reference, or null
     * @param syntax         the type syntax 
     */
    public SnmpTextualConvention(String displayHint,
                                 SnmpStatus status,
                                 String description,
                                 String reference,
                                 MibType syntax) {

        super("TEXTUAL-CONVENTION", false);
        this.displayHint = displayHint;
        this.status = status;
        this.description = description;
        this.reference = reference;
        this.syntax = syntax;
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
        syntax = syntax.initialize(log);
        return this;
    }

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

        return syntax.createReference();
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

        return syntax.createReference(constraint);
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

        return syntax.createReference(values);
    }

    /**
     * Checks if the specified value is compatible with this type. No
     * value is compatible with this type, so this method always 
     * returns false. 
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return false;
    }

    /**
     * Returns the display hint.
     * 
     * @return the display hint, or
     *         null if no display hint has been set
     */
    public String getDisplayHint() {
        return displayHint;
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
     * @return the type description
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
     * Returns the type syntax.
     * 
     * @return the type syntax
     */
    public MibType getSyntax() {
        return syntax;
    }

    /**
     * Returns a named MIB symbol. This method checks the syntax type 
     * for a MibContext implementation. 
     * 
     * @param name           the symbol name
     * 
     * @return the MIB symbol, or null if not found
     */
    public MibSymbol getSymbol(String name) {
        if (syntax instanceof MibContext) {
            return ((MibContext) syntax).getSymbol(name);
        } else {
            return null;
        }
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
        if (displayHint != null) {
            buffer.append("\n  Display-Hint: ");
            buffer.append(displayHint);
        }
        buffer.append("\n  Status: ");
        buffer.append(status);
        buffer.append("\n  Description: ");
        buffer.append(description);
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        buffer.append("\n  Syntax: ");
        buffer.append(syntax);
        buffer.append("\n)");
        return buffer.toString();
    }
}
