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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
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
import net.percederberg.mibble.MibTypeTag;
import net.percederberg.mibble.MibValue;

/**
 * A reference to a type symbol.<p>
 *
 * <strong>NOTE:</strong> This class is used internally during the
 * MIB parsing only. After loading a MIB file successfully, all type
 * references will have been resolved to other MIB types. Do
 * <strong>NOT</strong> use or reference this class.
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
     * The MIB type tag to set on the referenced type.
     */
    private MibTypeTag tag = null;

    /**
     * The implicit type tag flag.
     */
    private boolean implicitTag = true;

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

        super("ReferenceToType(" + name + ")", false);
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

        this(location, context, name);
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

        this(location, context, name);
        this.values = values;
    }

    /**
     * Initializes the MIB type. This will remove all levels of
     * indirection present, such as references to types or values. No
     * information is lost by this operation. This method may modify
     * this object as a side-effect, and will return the basic
     * type.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param symbol         the MIB symbol containing this type
     * @param log            the MIB loader log
     *
     * @return the basic MIB type
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     *
     * @since 2.2
     */
    public MibType initialize(MibSymbol symbol, MibLoaderLog log)
        throws MibException {

        MibSymbol  ref;
        String     message;

        ref = context.getSymbol(name);
        if (ref instanceof MibTypeSymbol) {
            type = initialize(symbol, log, (MibTypeSymbol) ref);
            return type;
        } else if (ref == null) {
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
     * @param symbol         the MIB symbol containing this type
     * @param log            the MIB loader log
     * @param ref            the referenced MIB type symbol
     *
     * @return the basic MIB type
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    private MibType initialize(MibSymbol symbol,
                               MibLoaderLog log,
                               MibTypeSymbol ref)
        throws MibException {

        MibType  type = ref.getType();

        type = type.initialize(symbol, log);
        try {
            if (constraint != null) {
                type = type.createReference(constraint);
            } else if (values != null) {
                type = type.createReference(values);
            } else {
                type = type.createReference();
            }
            type = type.initialize(symbol, log);
        } catch (UnsupportedOperationException e) {
            throw new MibException(location, e.getMessage());
        }
        type.setReferenceSymbol(ref);
        initializeTypeTag(type, tag);
        return type;
    }

    /**
     * Initializes the type tags for the specified type. The type tag
     * may be part in a chain of type tags, in which case the chain
     * is preserved. The last tag in the chain will be added first,
     * in order to be able to override (or preserve) a previous tag.
     *
     * @param type           the MIB type
     * @param tag            the MIB type tag
     */
    private void initializeTypeTag(MibType type, MibTypeTag tag) {
        if (tag == null) {
            // Do nothing
        } else if (tag.getNext() == null) {
            type.setTag(implicitTag, tag);
        } else {
            initializeTypeTag(type, tag.getNext());
            type.setTag(false, tag);
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
     * Sets the type tag. This method will keep the type tag stored
     * until the type reference is resolved.
     *
     * @param implicit       the implicit inheritance flag
     * @param tag            the new type tag
     *
     * @since 2.2
     */
    public void setTag(boolean implicit, MibTypeTag tag) {
        if (this.tag == null) {
            this.tag = tag;
            this.implicitTag = implicit;
        } else if (implicit) {
            tag.setNext(this.tag.getNext());
            this.tag = tag;
        } else {
            tag.setNext(this.tag);
            this.tag = tag;
        }
    }
}
