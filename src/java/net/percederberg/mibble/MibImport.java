/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.util.List;

/**
 * A MIB import list. This class contains a reference to another MIB
 * and a number of symbols in it.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.6
 */
public class MibImport implements MibContext {

    /**
     * The MIB loader being used.
     */
    private MibLoader loader;

    /**
     * The referenced MIB.
     */
    private Mib mib = null;

    /**
     * The import location.
     */
    private MibFileRef fileRef;

    /**
     * The imported MIB name.
     */
    private String name;

    /**
     * The imported MIB symbol names.
     */
    private List<String> symbols;

    /**
     * Creates a new MIB import.
     *
     * @param loader         the MIB loader to use
     * @param fileRef        the MIB file reference
     * @param name           the imported MIB name
     * @param symbols        the imported MIB symbol names, or
     *                       null for all symbols
     */
    MibImport(MibLoader loader,
              MibFileRef fileRef,
              String name,
              List<String> symbols) {

        this.loader = loader;
        this.fileRef = fileRef;
        this.name = name;
        this.symbols = symbols;
    }

    /**
     * Initializes the MIB import. This will resolve all referenced
     * symbols.  This method will be called by the MIB loader.
     *
     * @param log            the MIB loader log
     */
    public void initialize(MibLoaderLog log) {
        mib = loader.getMib(name);
        if (mib == null) {
            String msg = "couldn't find referenced MIB '" + name + "', " +
                         "skipping import of " + symbols.size() + " symbols";
            log.addWarning(fileRef, msg);
        } else if (symbols != null) {
            for (String sym : symbols) {
                if (mib.getSymbol(sym) == null) {
                    String msg = "couldn't find imported symbol '" + sym +
                                 "' in MIB '" + name + "'";
                    log.addWarning(fileRef, msg);
                }
            }
        }
    }

    /**
     * Validates the imported MIB module SMI version. Should be the
     * same as the importing MIB module SMI version. A warning will
     * be logged on mismatch.
     *
     * @param log            the MIB loader log
     * @param mib            the importing MIB module
     */
    protected void validateSmiVersion(MibLoaderLog log, Mib mib) {
        int expectedVer = mib.getSmiVersion();
        int importedVer = (this.mib == null) ? 0 : this.mib.getSmiVersion();
        if (this.mib != null && expectedVer != importedVer) {
            String msg = "imported " + name + " module is SMIv" + importedVer +
                         ", instead of SMIv" + expectedVer;
            log.addWarning(fileRef, msg);
        }
    }

    /**
     * Checks if this import has a symbol list.
     *
     * @return true if this import contains a symbol list, or
     *         false otherwise
     */
    boolean hasSymbols() {
        return symbols != null;
    }

    /**
     * Returns the imported MIB name.
     *
     * @return the imported MIB name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the imported MIB.
     *
     * @return the imported MIB
     */
    public Mib getMib() {
        return mib;
    }

    /**
     * Returns all symbol names in this MIB import declaration.
     *
     * @return a collection of the imported MIB symbol names
     */
    public List<String> getAllSymbolNames() {
        return symbols;
    }

    /**
     * Searches for a named MIB symbol. This method may search outside
     * the normal (or strict) scope, thereby allowing a form of
     * relaxed search. Note that the results from the normal and
     * expanded search may not be identical, due to the context
     * chaining and the same symbol name appearing in various
     * contexts.<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param name           the symbol name
     * @param expanded       the expanded scope flag
     *
     * @return the MIB symbol, or null if not found
     */
    public MibSymbol findSymbol(String name, boolean expanded) {
        if (mib == null) {
            return null;
        } else if (!expanded && symbols != null && !symbols.contains(name)) {
            return null;
        } else {
            return mib.getSymbol(name);
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return name;
    }
}
