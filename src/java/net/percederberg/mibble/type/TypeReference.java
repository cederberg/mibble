/*
 * TypeReference.java
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

import net.percederberg.mibble.FileLocation;
import net.percederberg.mibble.MibContext;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.snmp.SnmpTextualConvention;

/**
 * A reference to a type symbol.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.2
 * @since    2.0
 */
public class TypeReference extends MibType implements MibContext {

    /**
     * The reference location.
     */
    private FileLocation location;

    /**
     * The reference context.
     */
    private MibContext context;

    /**
     * The referenced type name.
     */
    private String name;

    /**
     * The referenced type.
     */
    private MibType type = null;

    /**
     * The additional type constraints.
     */
    private Constraint constraint = null;
    
    /**
     * The additional defined symbols.
     */
    private ArrayList values = null;

    /**
     * Creates a new type reference.
     * 
     * @param location       the reference location
     * @param context        the reference context 
     * @param name           the reference name
     */
    public TypeReference(FileLocation location, 
                         MibContext context, 
                         String name) {

        this.location = location;
        this.context = context;
        this.name = name;
    }

    /**
     * Creates a new type reference.
     * 
     * @param location       the reference location
     * @param context        the reference context 
     * @param name           the reference name
     * @param constraint     the additional type constraint 
     */
    public TypeReference(FileLocation location, 
                         MibContext context, 
                         String name,
                         Constraint constraint) {

        this.location = location;
        this.context = context;
        this.name = name;
        this.constraint = constraint;
    }

    /**
     * Creates a new type reference.
     * 
     * @param location       the reference location
     * @param context        the reference context 
     * @param name           the reference name
     * @param values         the additional defined symbols
     */
    public TypeReference(FileLocation location, 
                         MibContext context, 
                         String name,
                         ArrayList values) {

        this.location = location;
        this.context = context;
        this.name = name;
        this.values = values;
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
        MibSymbol  symbol;
        String     message;

        symbol = context.getSymbol(name);
        if (symbol instanceof MibTypeSymbol) {
            type = initialize(log, ((MibTypeSymbol) symbol).getType());
            return type;
        } else if (symbol == null) {
            message = "undefined symbol '" + name + "'";
            throw new MibException(location, message);
        } else {
            message = "referenced symbol '" + name + "' is not a type";
            throw new MibException(location, message);
        }
    }

    /**
     * Initializes the specified MIB type. This will remove all 
     * levels of indirection present, such as references to other 
     * types, and returns the basic type. This method  will add any 
     * constraints or defined values if possible.
     * 
     * @param log            the MIB loader log
     * @param type           the MIB type
     * 
     * @return the basic MIB type
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    private MibType initialize(MibLoaderLog log, MibType type) 
        throws MibException {

        type = type.initialize(log);
        if (constraint == null && values == null) {
            return type;
        } else if (type instanceof IntegerType) {
            return initialize(log, (IntegerType) type);
        } else if (type instanceof StringType) {
            return initialize(log, (StringType) type);
        } else if (type instanceof BitSetType) {
            return initialize(log, (BitSetType) type);
        } else if (type instanceof SnmpTextualConvention) {
            type = ((SnmpTextualConvention) type).getSyntax();
            return initialize(log, type);
        } else {
            throw new MibException(location, 
                                   "type does not support constraints");
        }
    }

    /**
     * Initializes the specified integer MIB type. This method will 
     * add the constraints or defined values.
     * 
     * @param log            the MIB loader log
     * @param type           the MIB type
     * 
     * @return the basic MIB type
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    private MibType initialize(MibLoaderLog log, IntegerType type) 
        throws MibException {

        if (values != null) {
            return new IntegerType(values).initialize(log);
        } else {
            return new IntegerType(constraint).initialize(log);
        }
    }

    /**
     * Initializes the specified string MIB type. This method  will 
     * add the constraints.
     * 
     * @param log            the MIB loader log
     * @param type           the MIB type
     * 
     * @return the basic MIB type
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    private MibType initialize(MibLoaderLog log, StringType type) 
        throws MibException {

        if (values != null) {
            throw new MibException(location, 
                                   "string type does not support " +
                                   "defined values");
        } else {
            return new StringType(constraint).initialize(log);
        }
    }

    /**
     * Initializes the specified bit set MIB type. This method  will 
     * add the constraints or defined values.
     * 
     * @param log            the MIB loader log
     * @param type           the MIB type
     * 
     * @return the basic MIB type
     * 
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    private MibType initialize(MibLoaderLog log, BitSetType type) 
        throws MibException {

        if (values != null) {
            return new BitSetType(values).initialize(log);
        } else {
            return new BitSetType(constraint).initialize(log);
        }
    }

    /**
     * Returns the file containing the reference.
     * 
     * @return the file containing the reference
     */
    public FileLocation getLocation() {
        return location;
    }

    /**
     * Checks if the specified value is compatible with this type. 
     * This metod will always return false for referenced types.
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
     * Returns a named MIB symbol. This method checks the referenced
     * type for a MibContext implementation. 
     * 
     * @param name           the symbol name
     * 
     * @return the MIB symbol, or null if not found
     */
    public MibSymbol getSymbol(String name) {
        if (type instanceof MibContext) {
            return ((MibContext) type).getSymbol(name);
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
        return "ReferenceToType(" + name + ")";
    }
}
