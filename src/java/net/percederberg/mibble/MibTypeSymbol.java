/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A MIB type symbol. This class holds information relevant to a MIB
 * type assignment, i.e. a defined type name.
 *
 * @author   Per Cederberg
 * @version  2.7
 * @since    2.0
 */
public class MibTypeSymbol extends MibSymbol {

    /**
     * The symbol type.
     */
    private MibType type;

    /**
     * Creates a new type symbol
     *
     * @param location       the symbol location
     * @param mib            the symbol MIB file
     * @param name           the symbol name
     * @param type           the symbol type
     *
     * @since 2.2
     */
    MibTypeSymbol(FileLocation location,
                  Mib mib,
                  String name,
                  MibType type) {

        super(location, mib, name);
        this.type = type;
    }

    /**
     * Initializes the MIB symbol. This will remove all levels of
     * indirection present, such as references to types or values. No
     * information is lost by this operation. This method may modify
     * this object as a side-effect.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    public void initialize(MibLoaderLog log) throws MibException {
        if (type != null) {
            try {
                type = type.initialize(this, log);
            } catch (MibException e) {
                log.addError(e.getLocation(), e.getMessage());
                type = null;
            }
        }
    }

    /**
     * Clears and prepares this MIB symbol for garbage collection.
     * This method will recursively clear any associated types or
     * values, making sure that no data structures references this
     * symbol.
     */
    void clear() {
        type = null;
    }

    /**
     * Returns the symbol type.
     *
     * @return the symbol type
     */
    public MibType getType() {
        return type;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuffer  buffer = new StringBuffer();

        buffer.append("TYPE ");
        buffer.append(getName());
        buffer.append(" ::= ");
        buffer.append(getType());
        return buffer.toString();
    }
}
