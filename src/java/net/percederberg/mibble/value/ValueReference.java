/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.value;

import net.percederberg.mibble.MibContext;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibFileRef;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;

/**
 * A reference to a value symbol.<p>
 *
 * <strong>NOTE:</strong> This class is used internally during the
 * MIB parsing only. After loading a MIB file successfully, all value
 * references will have been resolved to other MIB values. Do
 * <strong>NOT</strong> use or reference this class.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class ValueReference extends MibValue {

    /**
     * The declaration file location.
     *
     * @since 2.10
     */
    private MibFileRef fileRef;

    /**
     * The reference context.
     */
    private MibContext context;

    /**
     * The referenced name.
     */
    private String name;

    /**
     * Creates a new value reference.
     *
     * @param fileRef        the reference MIB file location
     * @param context        the reference context
     * @param name           the reference name
     */
    public ValueReference(MibFileRef fileRef, MibContext context, String name) {
        super("ReferenceToValue(" + name + ")");
        this.fileRef = fileRef;
        this.context = context;
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
    public MibValue initialize(MibLoaderLog log, MibType type)
        throws MibException {

        MibSymbol  sym;
        MibValue   value;
        String     message;

        sym = getSymbol(log);
        if (sym instanceof MibValueSymbol) {
            value = ((MibValueSymbol) sym).getValue();
            if (value != null) {
                value = value.initialize(log, type);
            }
            if (value == null) {
                return null;
            }
            try {
                value = value.createReference();
            } catch (UnsupportedOperationException e) {
                throw new MibException(fileRef, e.getMessage());
            }
            if (!(value instanceof ObjectIdentifierValue)) {
                value.setReferenceSymbol((MibValueSymbol) sym);
            }
            return value;
        } else if (sym == null) {
            message = "undefined symbol '" + name + "'";
            throw new MibException(fileRef, message);
        } else {
            message = "referenced symbol '" + name + "' is not a value";
            throw new MibException(fileRef, message);
        }
    }

    /**
     * Returns the reference MIB file location.
     *
     * @return the reference MIB file location
     */
    public MibFileRef getFileRef() {
        return fileRef;
    }

    /**
     * Returns the referenced symbol.
     *
     * @return the referenced symbol
     */
    public MibSymbol getSymbol() {
        return getSymbol(null);
    }

    /**
     * Returns the referenced symbol.
     *
     * @param log            the optional loader log
     *
     * @return the referenced symbol
     */
    private MibSymbol getSymbol(MibLoaderLog log) {
        MibSymbol  sym;
        String     message;

        sym = context.findSymbol(name, false);
        if (sym == null) {
            sym = context.findSymbol(name, true);
            if (sym != null && log != null) {
                message = "missing import for '" + name + "', using " +
                          sym.getMib().getName();
                log.addWarning(fileRef, message);
            }
        }
        return sym;
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
     *
     * @since 2.6
     */
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    /**
     * Returns a hash code for this object.
     *
     * @return a hash code for this object
     *
     * @since 2.6
     */
    public int hashCode() {
        return toString().hashCode();
    }

    /**
     * Returns a Java object representation of this value. This
     * method will always return null.
     *
     * @return a Java object representation of this value
     */
    public Object toObject() {
        return null;
    }

    /**
     * Returns a string representation of this value.
     *
     * @return a string representation of this value
     */
    public String toString() {
        return "ReferenceToValue(" + name + ")";
    }
}
