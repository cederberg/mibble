/*
 * BitSetValue.java
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

package net.percederberg.mibble.value;

import java.util.ArrayList;
import java.util.BitSet;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibValue;

/**
 * A bit set MIB value.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class BitSetValue extends MibValue {

    /**
     * The bit set value.
     */
    private BitSet value;
    
    /**
     * The additional value references.
     */
    private ArrayList references;

    /**
     * Creates a new bit set MIB value. 
     * 
     * @param value          the bit set value
     */
    public BitSetValue(BitSet value) {
        this(value, null);
    }

    /**
     * Creates a new bit set MIB value. 
     * 
     * @param value          the bit set value
     * @param references     the additional referenced bit values
     */
    public BitSetValue(BitSet value, ArrayList references) {
        this.value = value;
        this.references = references;
    }

    /**
     * Initializes the MIB value. This will remove all levels of
     * indirection present, such as references to other values, and 
     * returns the basic value. No value information is lost by this 
     * operation. This method may modify this object as a 
     * side-effect, and will be called by the MIB loader.
     * 
     * @param log            the MIB loader log
     * 
     * @return the basic MIB value
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public MibValue initialize(MibLoaderLog log) throws MibException { 
        if (references != null) {
            for (int i = 0; i < references.size(); i++) {
                initialize(log, (ValueReference) references.get(i));
            }
            references = null;
        }
        return this;
    }

    /**
     * Initializes a the MIB value from a value reference. This will 
     * resolve the reference, and set the bit corresponding to the 
     * value.
     * 
     * @param log            the MIB loader log
     * @param ref            the value reference to resolve
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    private void initialize(MibLoaderLog log, ValueReference ref) 
        throws MibException {

        MibValue  value = ref.initialize(log);
    
        if (value instanceof NumberValue) {
            this.value.set(((Number) value.toObject()).intValue());
        } else {
            throw new MibException(ref.getLocation(),
                                   "referenced value is not a number");
        }
    }

    /**
     * Returns all the bits in this bit set as individual number 
     * values.
     * 
     * @return the number values for all bits in this bit set
     */
    public ArrayList getBits() {
        ArrayList  components = new ArrayList();

        for (int i = 0; i < value.size(); i++) {
            if (value.get(i)) {
                components.add(new NumberValue(new Integer(i)));
            }
        }
        return components;
    }

    /**
     * Returns a Java BitSet representation of this value.
     * 
     * @return a Java BitSet representation of this value
     */
    public Object toObject() {
        return value;
    }

    /**
     * Returns a string representation of this value.
     * 
     * @return a string representation of this value
     */
    public String toString() {
        return value.toString();
    }
}
