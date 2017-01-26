/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2005-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A MIB macro symbol. This class holds information relevant to a MIB
 * macro definition, i.e. a defined macro name.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.6
 */
public class MibMacroSymbol extends MibSymbol {

    /**
     * Creates a new macro symbol
     *
     * @param fileRef        the MIB file reference
     * @param mib            the symbol MIB file
     * @param name           the symbol name
     */
    MibMacroSymbol(MibFileRef fileRef, Mib mib, String name) {
        super(fileRef, mib, name);
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
        // Nothing to be initialized
    }

    /**
     * Clears and prepares this MIB symbol for garbage collection.
     * This method will recursively clear any associated types or
     * values, making sure that no data structures references this
     * symbol.
     */
    void clear() {
        // Nothing to clear
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("MACRO ");
        buffer.append(getName());
        return buffer.toString();
    }
}
