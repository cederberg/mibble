/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import java.util.ArrayList;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeSymbol;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.type.ElementType;
import net.percederberg.mibble.type.SequenceOfType;
import net.percederberg.mibble.type.SequenceType;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * The SNMP object type macro type. This macro type was present in
 * SMIv1, but was somewhat extended in SMIv2. It is defined in the
 * RFC:s 1155, 1212 and 2578.
 *
 * @see <a href="http://www.ietf.org/rfc/rfc1155.txt">RFC 1155 (RFC1155-SMI)</a>
 * @see <a href="http://www.ietf.org/rfc/rfc1212.txt">RFC 1212 (RFC-1212)</a>
 * @see <a href="http://www.ietf.org/rfc/rfc2578.txt">RFC 2578 (SNMPv2-SMI)</a>
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SnmpObjectType extends SnmpType {

    /**
     * The type syntax.
     */
    private MibType syntax;

    /**
     * The units description.
     */
    private String units;

    /**
     * The access mode.
     */
    private SnmpAccess access;

    /**
     * The type status.
     */
    private SnmpStatus status;

    /**
     * The type reference.
     */
    private String reference;

    /**
     * The list of index values or types.
     */
    private ArrayList<SnmpIndex> index;

    /**
     * The index augments value.
     */
    private MibValue augments;

    /**
     * The default value.
     */
    private MibValue defaultValue;

    /**
     * Creates a new SNMP object type.
     *
     * @param syntax         the object type syntax
     * @param units          the units description, or null
     * @param access         the access mode
     * @param status         the type status
     * @param description    the type description, or null
     * @param reference      the type reference, or null
     * @param index          the list of index objects
     * @param defaultValue   the default value, or null
     */
    public SnmpObjectType(MibType syntax,
                          String units,
                          SnmpAccess access,
                          SnmpStatus status,
                          String description,
                          String reference,
                          ArrayList<SnmpIndex> index,
                          MibValue defaultValue) {

        super("OBJECT-TYPE", description);
        this.syntax = syntax;
        this.units = units;
        this.access = access;
        this.status = status;
        this.reference = reference;
        this.index = index;
        this.augments = null;
        this.defaultValue = defaultValue;
    }

    /**
     * Creates a new SNMP object type.
     *
     * @param syntax         the object type syntax
     * @param units          the units description, or null
     * @param access         the access mode
     * @param status         the type status
     * @param description    the type description, or null
     * @param reference      the type reference, or null
     * @param augments       the index augments value
     * @param defaultValue   the default value, or null
     */
    public SnmpObjectType(MibType syntax,
                          String units,
                          SnmpAccess access,
                          SnmpStatus status,
                          String description,
                          String reference,
                          MibValue augments,
                          MibValue defaultValue) {

        super("OBJECT-TYPE", description);
        this.syntax = syntax;
        this.units = units;
        this.access = access;
        this.status = status;
        this.reference = reference;
        this.index = new ArrayList<>(0);
        this.augments = augments;
        this.defaultValue = defaultValue;
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

        if (!(symbol instanceof MibValueSymbol)) {
            throw new MibException(symbol.getFileRef(),
                                   "only values can have the " +
                                   getName() + " type");
        }
        syntax = syntax.initialize(symbol, log);
        checkType((MibValueSymbol) symbol, log, syntax);
        for (SnmpIndex idx : index) {
            idx.initialize(symbol, log);
        }
        if (augments != null) {
            augments = augments.initialize(log, syntax);
        }
        if (defaultValue != null) {
            defaultValue = defaultValue.initialize(log, syntax);
        }
        return this;
    }

    /**
     * Validates a MIB type. This will check any sequences and make
     * sure their elements are present in the MIB file. If they are
     * not, new symbols will be added to the MIB.
     *
     * @param symbol         the MIB symbol containing this type
     * @param log            the MIB loader log
     * @param type           the MIB type to check
     *
     * @throws MibException if an error was encountered during the
     *             validation
     *
     * @since 2.2
     */
    private void checkType(MibValueSymbol symbol,
                           MibLoaderLog log,
                           MibType type)
        throws MibException {

        if (type instanceof SequenceOfType) {
            SequenceOfType sequence = (SequenceOfType) type;
            checkType(symbol, log, sequence.getElementType());
        } else if (type instanceof SequenceType) {
            int i = 1;
            for (ElementType elem : ((SequenceType) type).getAllElements()) {
                checkElement(symbol, log, elem, i++);
            }
        }
    }

    /**
     * Validates an element type. This will check that the element
     * is present in the MIB file. If it is not, a new symbol will be
     * added to the MIB.
     *
     * @param symbol         the MIB symbol containing this type
     * @param log            the MIB loader log
     * @param element        the MIB element type to check
     * @param pos            the MIB element position
     *
     * @throws MibException if an error was encountered during the
     *             validation
     *
     * @since 2.2
     */
    private void checkElement(MibValueSymbol symbol,
                              MibLoaderLog log,
                              ElementType element,
                              int pos)
        throws MibException {

        String name = String.valueOf(pos);
        if (element.getName() != null) {
            name = pos + " '" + element.getName() + "'";
        }
        Mib mib = symbol.getMib();
        MibSymbol elementSymbol = mib.getSymbol(element.getName());
        if (elementSymbol == null) {
            log.addWarning(symbol.getFileRef(),
                           "sequence element " + name + " is undefined " +
                           "in MIB, a default symbol will be created");
            name = element.getName();
            if (name == null) {
                name = symbol.getName() + "." + pos;
            }
            MibType type = new SnmpObjectType(element.getType(),
                                              null,
                                              SnmpAccess.READ_ONLY,
                                              SnmpStatus.CURRENT,
                                              "AUTOMATICALLY CREATED SYMBOL",
                                              null,
                                              new ArrayList<SnmpIndex>(0),
                                              null);
            ObjectIdentifierValue value = (ObjectIdentifierValue) symbol.getValue();
            value = new ObjectIdentifierValue(symbol.getFileRef(),
                                              value,
                                              element.getName(),
                                              pos);
            elementSymbol = new MibValueSymbol(symbol.getFileRef(),
                                               mib,
                                               name,
                                               type,
                                               value);
            elementSymbol.initialize(log);
        } else if (elementSymbol instanceof MibTypeSymbol) {
            throw new MibException(symbol.getFileRef(),
                                   "sequence element " + name +
                                   " does not refer to a value, but " +
                                   "to a type");
        }
    }

    /**
     * Checks if the specified value is compatible with this type. A
     * value is compatible if and only if it is an object identifier
     * value.
     *
     * @param value          the value to check
     *
     * @return true if the value is compatible, or
     *         false otherwise
     */
    public boolean isCompatible(MibValue value) {
        return value instanceof ObjectIdentifierValue;
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
     * Returns the units description.
     *
     * @return the units description, or
     *         null if no units has been set
     */
    public String getUnits() {
        return units;
    }

    /**
     * Returns the access mode.
     *
     * @return the access mode
     */
    public SnmpAccess getAccess() {
        return access;
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
     * Returns the type reference.
     *
     * @return the type reference, or
     *         null if no reference has been set
     */
    public String getReference() {
        return reference;
    }

    /**
     * Returns the list of indices. The returned list will consist of
     * SnmpIndex instances. Note that the semantics of this method
     * changed in version 2.6, as the returned list previously
     * contained type and value objects.
     *
     * @return the list of SNMP index objects, or
     *         an empty list if no indices are defined
     *
     * @see SnmpIndex
     *
     * @since 2.6
     */
    public ArrayList<SnmpIndex> getIndex() {
        return index;
    }

    /**
     * Returns the augmented index value.
     *
     * @return the augmented index value, or
     *         null if no augments index is used
     */
    public MibValue getAugments() {
        return augments;
    }

    /**
     * Returns the default value.
     *
     * @return the default value, or
     *         null if no default value has been set
     */
    public MibValue getDefaultValue() {
        return defaultValue;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(super.toString());
        buffer.append(" (");
        buffer.append("\n  Syntax: ");
        buffer.append(syntax);
        if (units != null) {
            buffer.append("\n  Units: ");
            buffer.append(units);
        }
        buffer.append("\n  Access: ");
        buffer.append(access);
        buffer.append("\n  Status: ");
        buffer.append(status);
        if (getUnformattedDescription() != null) {
            buffer.append("\n  Description: ");
            buffer.append(getDescription("               "));
        }
        if (reference != null) {
            buffer.append("\n  Reference: ");
            buffer.append(reference);
        }
        if (index.size() > 0) {
            buffer.append("\n  Index: ");
            buffer.append(index);
        }
        if (augments != null) {
            buffer.append("\n  Augments: ");
            buffer.append(augments);
        }
        if (defaultValue != null) {
            buffer.append("\n  Default Value: ");
            buffer.append(defaultValue);
        }
        buffer.append("\n)");
        return buffer.toString();
    }
}
