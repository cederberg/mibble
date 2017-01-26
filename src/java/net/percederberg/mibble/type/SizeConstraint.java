/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.type;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibFileRef;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.value.NumberValue;
import net.percederberg.mibble.value.StringValue;

/**
 * A MIB type size constraint.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SizeConstraint implements Constraint {

    /**
     * The constraint MIB file location. This value is reset to null
     * once the constraint has been initialized.
     */
    private MibFileRef fileRef;

    /**
     * The constrained size values.
     */
    private Constraint values;

    /**
     * Creates a new size constraint.
     *
     * @param fileRef        the constraint MIB file location
     * @param values         the constrained size values
     */
    public SizeConstraint(MibFileRef fileRef, Constraint values) {
        this.fileRef = fileRef;
        this.values = values;
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

        values.initialize(new IntegerType(), log);
        if (fileRef != null && !isCompatible(type)) {
            String message = "size constraint not compatible with this type";
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
        return type instanceof SequenceOfType
            || type instanceof StringType;
    }

    /**
     * Checks if the specified value is compatible with this
     * constraint. Only octet string values can be compatible with a
     * size constraint, and only if the string length is compatible
     * with the value range in the size constraint.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        if (value instanceof StringValue) {
            Integer size = new Integer(value.toString().length());
            return values.isCompatible(new NumberValue(size));
        }
        return false;
    }

    /**
     * Returns a list of the value constraints on the size.
     *
     * @return a list of the value constraints
     */
    public ArrayList<Constraint> getValues() {
        if (values instanceof CompoundConstraint) {
            return ((CompoundConstraint) values).getConstraintList();
        } else {
            ArrayList<Constraint> list = new ArrayList<>();
            list.add(values);
            return list;
        }
    }

    /**
     * Returns the next compatible size constraint value from a start
     * value. The values will be enumerated from lower values to
     * higher.
     *
     * @param start          the initial start value
     *
     * @return the next compatible value, or
     *         -1 if no such value exists
     *
     * @since 2.9
     */
    public int nextValue(int start) {
        // TODO: the constraint list should be sorted
        for (Constraint c : getValues()) {
            Object obj = null;
            if (c instanceof ValueConstraint) {
                obj = ((ValueConstraint) c).getValue().toObject();
            } else if (c instanceof ValueRangeConstraint) {
                if (((ValueRangeConstraint) c).isCompatible(new Integer(start))) {
                    return start;
                }
                obj = ((ValueRangeConstraint) c).getLowerBound().toObject();
            }
            if (obj instanceof Number && start <= ((Number) obj).intValue()) {
                return ((Number) obj).intValue();
            }
        }
        return -1;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("SIZE (");
        buffer.append(values);
        buffer.append(")");
        return buffer.toString();
    }
}
