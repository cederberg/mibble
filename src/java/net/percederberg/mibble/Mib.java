/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.percederberg.mibble.value.NumberValue;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * An SNMP MIB module. This class contains all the information
 * from a single MIB module, including all defined types and values.
 * Note that a single MIB file may contain several such modules,
 * although that is not very common. MIB files are loaded through a
 * {@link MibLoader MIB loader}.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 *
 * @see <a href="http://www.ietf.org/rfc/rfc3411.txt">RFC 3411 - An
 *      Architecture for Describing SNMP Management Frameworks</a>
 */
public class Mib implements MibContext {

    /**
     * The loader used for this MIB.
     */
    private MibLoader loader;

    /**
     * The loader log used for loading this MIB.
     */
    private MibLoaderLog log;

    /**
     * The MIB file reference.
     */
    private MibFileRef fileRef = null;

    /**
     * The explicitly loaded flag. This flag is set when a MIB is
     * loaded by a direct call to the MibLoader, in contrast to when
     * it is loaded as the result of an import.
     */
    private boolean loaded = false;

    /**
     * The MIB name.
     */
    private String name = null;

    /**
     * The SMI version.
     */
    private int smiVersion = 1;

    /**
     * The MIB file header comment.
     */
    private String headerComment = null;

    /**
     * The MIB file footer comment.
     */
    private String footerComment = null;

    /**
     * The MIB source text (split into lines).
     */
    private ArrayList<String> text = new ArrayList<>();

    /**
     * The references to imported MIB files.
     */
    private ArrayList<MibImport> imports = new ArrayList<>();

    /**
     * The MIB symbol list. This list contains the MIB symbol objects
     * in the order they were added (i.e. present in the file).
     */
    private ArrayList<MibSymbol> symbolList = new ArrayList<>();

    /**
     * The MIB symbol name map. This maps the symbol names to their
     * respective MIB symbol objects.
     */
    private HashMap<String,MibSymbol> symbolNameMap = new HashMap<>();

    /**
     * The MIB symbol value map. This maps the symbol values to their
     * respective MIB symbol objects. Only the value symbols with
     * either a number or an object identifier value is present in
     * this map.
     */
    private HashMap<String,MibSymbol> symbolValueMap = new HashMap<>();

    /**
     * Creates a new MIB module. This will NOT read the actual MIB
     * file, but only creates an empty container. The symbols are
     * then added during the first analysis pass (of two), leaving
     * symbols in the MIB possibly containing unresolved references.
     * A separate call to initialize() must be made once all
     * referenced MIB modules have also been loaded.
     *
     * @param loader         the MIB loader to use for imports
     * @param log            the MIB log to use for errors
     *
     * @see #initialize()
     */
    Mib(MibLoader loader, MibLoaderLog log) {
        this.loader = loader;
        this.log = log;
    }

    /**
     * Initializes the MIB file. This will resolve all imported MIB
     * file references. Note that this method shouldn't be called
     * until all referenced MIB files (and their respective
     * references) have been loaded.
     *
     * @throws MibLoaderException if the MIB file couldn't be
     *             analyzed correctly
     *
     * @see #validate()
     */
    void initialize() throws MibLoaderException {

        // Resolve imported MIB files
        int  errors = log.errorCount();
        for (MibImport imp : imports) {
            imp.initialize(log);
            if (loaded) {
                imp.validateSmiVersion(log, this);
            }
        }

        // Check for errors
        if (errors != log.errorCount()) {
            throw new MibLoaderException(log);
        }
    }

    /**
     * Validates the MIB file. This will resolve all type and value
     * references in the MIB symbols, while also validating them for
     * consistency. Note that this method shouldn't be called until
     * all referenced MIB files (and their respective references)
     * have been initialized.
     *
     * @throws MibLoaderException if the MIB file couldn't be
     *             analyzed correctly
     *
     * @see #initialize()
     */
    void validate() throws MibLoaderException {

        // Validate all symbols
        int errors = log.errorCount();
        for (MibSymbol symbol : new ArrayList<>(symbolList)) {
            try {
                symbol.initialize(log);
            } catch (MibException e) {
                log.addError(e);
            }
            if (symbol instanceof MibValueSymbol) {
                MibValue value = ((MibValueSymbol) symbol).getValue();
                boolean isNumber = value instanceof NumberValue;
                boolean isOID = value instanceof ObjectIdentifierValue;
                if (isNumber || isOID) {
                    if (symbolValueMap.containsKey(value.toString())) {
                        String msg = "duplicate definition of " +
                                     value.toString() + " in MIB";
                        log.addWarning(symbol.getFileRef(), msg);
                    }
                    symbolValueMap.put(value.toString(), symbol);
                }
            }
        }

        // Check for errors
        if (errors != log.errorCount()) {
            throw new MibLoaderException(log);
        }
    }

    /**
     * Clears and prepares this MIB for garbage collection. This method
     * will recursively clear all associated symbols, making sure that
     * no data structures references symbols from this MIB. Obviously,
     * this method shouldn't be called unless all dependent MIBs have
     * been cleared first.
     */
    void clear() {
        loader = null;
        log = null;
        if (imports != null) {
            imports.clear();
        }
        imports = null;
        if (symbolList != null) {
            for (MibSymbol symbol : new ArrayList<>(symbolList)) {
                symbol.clear();
            }
            symbolList.clear();
        }
        symbolList = null;
        if (symbolNameMap != null) {
            symbolNameMap.clear();
        }
        symbolNameMap = null;
        if (symbolValueMap != null) {
            symbolValueMap.clear();
        }
        symbolValueMap = null;
    }

    /**
     * Compares this MIB to another object. This method will return
     * true if the object is a string containing the MIB name, a file
     * containing the MIB file, or a Mib having the same name.
     *
     * @param obj            the object to compare with
     *
     * @return true if the objects are equal, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        if (obj instanceof String) {
            return name.equals(obj);
        } else if (fileRef.file != null && obj instanceof File) {
            return fileRef.file.equals(obj);
        } else if (obj instanceof Mib) {
            return obj.equals(name);
        } else {
            return false;
        }
    }

    /**
     * Returns the hash code value for the object. This method is
     * reimplemented to fulfil the contract of returning the same
     * hash code for objects that are considered equal.
     *
     * @return the hash code value for the object
     *
     * @since 2.6
     */
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Checks if this MIB module has been explicitly loaded. A MIB
     * module is considered explicitly loaded if the file or resource
     * containing the MIB definition was loaded by a direct call to
     * the MIB loader. Implictly loaded MIB modules are loaded as a
     * result of import statements in explicitly loaded MIBs.
     *
     * @return true if this MIB module was explicitly loaded, or
     *         false otherwise
     *
     * @since 2.7
     */
    public boolean isLoaded() {
        return loaded;
    }

    /**
     * Sets the the explicitly loaded flag.
     *
     * @param loaded         the new flag value
     */
    void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    /**
     * Returns the MIB name. This is sometimes also referred to as
     * the MIB module name.
     *
     * @return the MIB name
     */
    public String getName() {
        return name;
    }

    /**
     * Changes the MIB name. This method should only be called by
     * the MIB analysis classes.
     *
     * @param name           the MIB name
     */
    void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the MIB file.
     *
     * @return the MIB file
     */
    public File getFile() {
        return fileRef.getFile();
    }

    /**
     * Sets the MIB file reference. This method should only be called
     * by the MIB analysis classes.
     *
     * @param fileRef        the MIB file reference
     */
    void setFileRef(MibFileRef fileRef) {
        this.fileRef = fileRef;
        if (fileRef.file == null) {
            fileRef.file = new File(name);
        }
    }

    /**
     * Returns the MIB loader used when loading this MIB.
     *
     * @return the loader used
     */
    public MibLoader getLoader() {
        return loader;
    }

    /**
     * Returns the loader log used when loading this MIB.
     *
     * @return the loader log used
     */
    public MibLoaderLog getLog() {
        return log;
    }

    /**
     * Returns the SMI version used for defining this MIB. This
     * number can be either 1 (for SMIv1) or 2 (for SMIv2). It is set
     * based on which macros are used in the MIB file.
     *
     * @return the SMI version used for defining the MIB
     *
     * @since 2.6
     */
    public int getSmiVersion() {
        return smiVersion;
    }

    /**
     * Sets the SMI version used for defining this MIB. This method
     * should only be called by the MIB analysis classes.
     *
     * @param version        the new SMI version
     *
     * @since 2.6
     */
    void setSmiVersion(int version) {
        this.smiVersion = version;
    }

    /**
     * Returns the unparsed input MIB text.
     *
     * @return the raw MIB file text
     *
     * @since 2.10
     */
    public String getText() {
        StringBuilder buffer = new StringBuilder();
        for (String line : text) {
            buffer.append(line);
            buffer.append('\n');
        }
        return buffer.toString();
    }

    /**
     * Returns the unparsed input MIB text for a reference.
     *
     * @param ref            the MIB file reference
     *
     * @return the raw MIB text for the reference
     *
     * @since 2.10
     */
    String getText(MibFileRef ref) {
        int from = ref.lineCommentStart - this.fileRef.lineCommentStart;
        int to = ref.lineEnd - this.fileRef.lineCommentStart;
        StringBuilder buffer = new StringBuilder();
        for (int i = from; i <= to; i++) {
            buffer.append(text.get(i));
            buffer.append('\n');
        }
        return buffer.toString();
    }

    /**
     * Sets the unparsed input MIB text. This method should only be
     * called by the MIB analysis classes.
     *
     * @param text           the raw MIB file text
     *
     * @since 2.10
     */
    void setText(String text) {
        this.text.clear();
        String[] lines = text.split("[ \\t\\r]*\\n");
        for (String line : lines) {
            this.text.add(line);
        }
    }

    /**
     * Returns the MIB file header comment.
     *
     * @return the MIB file header comment, or
     *         null if no comment was present
     *
     * @since 2.6
     */
    public String getHeaderComment() {
        return headerComment;
    }

    /**
     * Sets the MIB file header comment.
     *
     * @param comment        the MIB header comment
     *
     * @since 2.6
     */
    void setHeaderComment(String comment) {
        this.headerComment = comment;
    }

    /**
     * Returns the MIB file footer comment.
     *
     * @return the MIB file footer comment, or
     *         null if no comment was present
     *
     * @since 2.6
     */
    public String getFooterComment() {
        return footerComment;
    }

    /**
     * Sets the MIB file footer comment.
     *
     * @param comment        the MIB footer comment
     *
     * @since 2.6
     */
    void setFooterComment(String comment) {
        this.footerComment = comment;
    }

    /**
     * Returns all MIB import references.
     *
     * @return a list of all imports
     *
     * @see MibImport
     *
     * @since 2.6
     */
    public List<MibImport> getAllImports() {
        ArrayList<MibImport> res = new ArrayList<>();
        for (MibImport imp : imports) {
            if (imp.hasSymbols()) {
                res.add(imp);
            }
        }
        return res;
    }

    /**
     * Returns a MIB import reference.
     *
     * @param name           the imported MIB name
     *
     * @return the MIB import reference, or
     *         null if not found
     */
    MibImport getImport(String name) {
        for (MibImport imp : imports) {
            if (imp.getName().equals(name)) {
                return imp;
            }
        }
        return null;
    }

    /**
     * Adds a reference to an imported MIB file.
     *
     * @param ref            the reference to add
     */
    void addImport(MibImport ref) {
        imports.add(ref);
    }

    /**
     * Finds all MIB:s that are dependant on this one. The search
     * will iterate through all loaded MIB:s and return those that
     * import this one.
     *
     * @return the array of MIB:s importing this one
     *
     * @see MibLoader
     *
     * @since 2.7
     */
    public Mib[] getImportingMibs() {
        ArrayList<Mib> res = new ArrayList<>();
        for (Mib mib : loader.getAllMibs()) {
            if (mib != this && mib.getImport(name) != null) {
                res.add(mib);
            }
        }
        return res.toArray(new Mib[res.size()]);
    }

    /**
     * Returns all symbols in this MIB.
     *
     * @return a list of the MIB symbols
     *
     * @see MibSymbol
     */
    public List<MibSymbol> getAllSymbols() {
        return symbolList;
    }

    /**
     * Returns a symbol from this MIB.
     *
     * @param name           the symbol name
     *
     * @return the MIB symbol, or null if not found
     */
    public MibSymbol getSymbol(String name) {
        return symbolNameMap.get(name);
    }

    /**
     * Returns a value symbol from this MIB.
     *
     * @param value          the symbol value
     *
     * @return the MIB value symbol, or null if not found
     */
    public MibValueSymbol getSymbolByValue(String value) {
        if (value.startsWith(".")) {
            value = value.substring(1);
        }
        return (MibValueSymbol) symbolValueMap.get(value);
    }

    /**
     * Returns a value symbol from this MIB.
     *
     * @param value          the symbol value
     *
     * @return the MIB value symbol, or null if not found
     */
    public MibValueSymbol getSymbolByValue(MibValue value) {
        return (MibValueSymbol) symbolValueMap.get(value.toString());
    }

    /**
     * Returns a value symbol from this MIB. The search is performed
     * by using the strictly numerical OID value specified. Differing
     * from the getSymbolByValue() methods, this method may return a
     * symbol with only a partial OID match. If an exact match for
     * the OID is present in the MIB, this method will always return
     * the same result as getSymbolByValue(). Otherwise, the symbol
     * with the longest matching OID will be returned, making it
     * possible to identify a MIB symbol from an OID containing table
     * row indices or similar.
     *
     * @param oid            the numeric OID value
     *
     * @return the MIB value symbol, or null if not found
     *
     * @since 2.5
     */
    public MibValueSymbol getSymbolByOid(String oid) {
        if (oid.startsWith(".")) {
            oid = oid.substring(1);
        }
        int pos = 0;
        do {
            MibValueSymbol sym = getSymbolByValue(oid);
            if (sym != null) {
                return sym;
            }
            pos = oid.lastIndexOf(".");
            if (pos > 0) {
                oid = oid.substring(0, pos);
            }
        } while (pos > 0);
        return null;
    }

    /**
     * Returns the root MIB value symbol. This value symbol is
     * normally the module identifier (in SMIv2), but may also be
     * just the base object identifier in the MIB.
     *
     * @return the root MIB value symbol
     *
     * @since 2.6
     */
    public MibValueSymbol getRootSymbol() {
        MibValueSymbol root = null;
        for (MibSymbol symbol : symbolList) {
            if (symbol instanceof MibValueSymbol) {
                root = (MibValueSymbol) symbol;
                break;
            }
        }
        MibValueSymbol parent = null;
        while (root != null && (parent = root.getParent()) != null) {
            if (!root.getMib().equals(parent.getMib())) {
                break;
            }
            root = parent;
        }
        return root;
    }

    /**
     * Adds a symbol to this MIB.
     *
     * @param symbol         the symbol to add
     */
    void addSymbol(MibSymbol symbol) {
        symbolList.add(symbol);
        symbolNameMap.put(symbol.getName(), symbol);
    }

    /**
     * Searches for a named MIB symbol. This method is required to
     * implement the MibContext interface but returns the same results
     * as getSymbol(String).<p>
     *
     * <strong>NOTE:</strong> This is an internal method that should
     * only be called by the MIB loader.
     *
     * @param name           the symbol name
     * @param expanded       the expanded scope flag
     *
     * @return the MIB symbol, or null if not found
     *
     * @since 2.4
     */
    public MibSymbol findSymbol(String name, boolean expanded) {
        return getSymbol(name);
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        return getName();
    }
}
