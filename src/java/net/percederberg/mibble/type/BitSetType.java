/*
 * BitSetType.java
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

package net.percederberg.mibble.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.mibble.MibContext;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.value.BitSetValue;
import net.percederberg.mibble.value.NumberValue;

/**
 * A bit set MIB type.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class BitSetType implements MibType, MibContext {

    /**
     * The additional type constraint.
     */
    private Constraint constraint = null;

    /**
     * The additional defined symbols.
     */
    private HashMap symbols = new HashMap();

    /**
     * Creates a new bit set MIB type. 
     */
    public BitSetType() {
    }

    /**
     * Creates a new bit set MIB type.
     * 
     * @param constraint     the additional type constraint 
     */
    public BitSetType(Constraint constraint) {
        this.constraint = constraint;
    }

    /**
     * Creates a new integer MIB type.
     * 
     * @param values         the additional defined symbols
     */
    public BitSetType(ArrayList values) {
        MibValueSymbol   sym;
        ValueConstraint  c;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) instanceof MibValueSymbol) {
                sym = (MibValueSymbol) values.get(i);
                symbols.put(sym.getName(), sym);
                c = new ValueConstraint(sym.getValue());
                if (constraint == null) {
                    constraint = c;
                } else {
                    constraint = new CompoundConstraint(constraint, c);
                }
            }
        }
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
        Iterator        iter = symbols.values().iterator();
        MibValueSymbol  sym;
        String          message;

        if (constraint != null) {
            constraint.initialize(log);
        }
        while (iter.hasNext()) {
            sym = (MibValueSymbol) iter.next();
            sym.initialize(log);
            if (!(sym.getValue() instanceof NumberValue)) {
                message = "value is not compatible with type";
                throw new MibException(sym.getLocation(), message);
            }
        }
        return this;
    }

    /**
     * Checks if this type has any constraint.
     * 
     * @return true if this type has any constraint, or
     *         false otherwise
     */
    public boolean hasConstraint() {
        return constraint != null;
    }
    
    /**
     * Checks if this type has any defined value symbols.
     *  
     * @return true if this type has any defined value symbols, or
     *         false otherwise
     */
    public boolean hasSymbols() {
        return symbols.size() > 0;
    }

    /**
     * Checks if the specified value is compatible with this type.  A
     * value is compatible if it is a bit set value with all 
     * components compatible with the constraints. 
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return value instanceof BitSetValue
            && isCompatible((BitSetValue) value);
    }

    /**
     * Checks if the specified bit set value is compatible with this 
     * type.  A value is compatible if all the bits are compatible 
     * with the constraints. 
     * 
     * @param value          the value to check
     * 
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(BitSetValue value) {
        ArrayList  bits;

        if (constraint == null) {
            return true;
        }
        bits = value.getBits();
        for (int i = 0; i < bits.size(); i++) {
            if (!constraint.isCompatible((MibValue) bits.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a named MIB symbol. This method returns predefined 
     * integer values, if some exist.
     * 
     * @param name           the symbol name
     * 
     * @return the MIB symbol, or null if not found
     */
    public MibSymbol getSymbol(String name) {
        return (MibSymbol) symbols.get(name);
    }

    /**
     * Returns a string representation of this type.
     * 
     * @return a string representation of this type
     */
    public String toString() {
        if (constraint == null) {
            return "BITS";
        } else {
            return "BITS (" + constraint.toString() + ")";
        }
    }
}
