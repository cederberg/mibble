/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

/**
 * A MIB symbol. This is the base class for all symbols in a MIB file.
 * Each symbol is typically identified by it's name, which must be
 * unique within the MIB file. All symbols also have a data type.
 *
 * @author   Per Cederberg
 * @version  2.7
 * @since    2.0
 */
public abstract class MibSymbol {

    /**
     * The symbol location.
     */
    private FileLocation location;

    /**
     * The MIB containing this symbol.
     */
    private Mib mib;

    /**
     * The symbol name.
     */
    private String name;

    /**
     * The symbol comment.
     */
    private String comment;

    /**
     * Creates a new symbol with the specified name. The symbol will
     * also be added to the MIB file.
     *
     * @param location       the symbol location
     * @param mib            the symbol MIB file
     * @param name           the symbol name
     *
     * @since 2.2
     */
    MibSymbol(FileLocation location, Mib mib, String name) {
        this.location = location;
        this.mib = mib;
        this.name = name;
        if (mib != null) {
            mib.addSymbol(this);
        }
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
    public abstract void initialize(MibLoaderLog log) throws MibException;

    /**
     * Clears and prepares this MIB symbol for garbage collection.
     * This method will recursively clear any associated types or
     * values, making sure that no data structures references this
     * symbol.
     */
    abstract void clear();

    /**
     * Returns the file location.
     *
     * @return the file location
     */
    public FileLocation getLocation() {
        return location;
    }

    /**
     * Returns the symbol MIB file. This is the MIB file where the
     * symbol is defined.
     *
     * @return the symbol MIB file
     *
     * @since 2.2
     */
    public Mib getMib() {
        return mib;
    }

    /**
     * Returns the symbol name.
     *
     * @return the symbol name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the symbol comment.
     *
     * @return the symbol comment, or
     *         null if no comment was set
     *
     * @since 2.6
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the symbol comment.
     *
     * @param comment        the symbol comment
     *
     * @since 2.6
     */
    void setComment(String comment) {
        this.comment = comment;
    }
}
