/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.type;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibFileRef;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.value.NumberValue;
import net.percederberg.mibble.value.StringValue;

/**
 * A MIB type value constraint. This class represents a single value
 * in a set of value constraints.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class ValueConstraint implements Constraint {

    /**
     * The constraint MIB file location. This value is reset to null
     * once the constraint has been initialized.
     */
    private MibFileRef fileRef;

    /**
     * The constraint value.
     */
    private MibValue value;

    /**
     * Creates a new value constraint.
     *
     * @param fileRef        the constraint MIB file location
     * @param value          the constraint value
     */
    public ValueConstraint(MibFileRef fileRef, MibValue value) {
        this.fileRef = fileRef;
        this.value = value;
    }

    /**
     * Initializes the constraint. This will remove all levels of
     * indirection present, such as references to types or values. No
     * constraint information is lost by this operation. This method
     * may modify this object as a side-effect, and will be called by
     * the MIB loader.
     *
     * @param type           the type to constrain
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public void initialize(MibType type, MibLoaderLog log)
        throws MibException {

        String  message;

        value = value.initialize(log, type);
        if (fileRef != null && !isCompatible(type)) {
            message = "value constraint not compatible with this type";
            log.addWarning(fileRef, message);
        }
        fileRef = null;
    }

    /**
     * Checks if the specified type is compatible with this
     * constraint.
     *
     * @param type            the type to check
     *
     * @return true if the type is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibType type) {
        return type == null || value == null || type.isCompatible(value);
    }

    /**
     * Checks if the specified value is compatible with this
     * constraint.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        String  str1 = this.value.toString();
        String  str2 = value.toString();

        if (this.value instanceof NumberValue
         && value instanceof NumberValue) {

            return str1.equals(str2);
        } else if (this.value instanceof StringValue
                && value instanceof StringValue) {

            return str1.equals(str2);
        } else {
            return false;
        }
    }

    /**
     * Returns the constraint value.
     *
     * @return the constraint value
     */
    public MibValue getValue() {
        return value;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return value.toString();
    }
}
