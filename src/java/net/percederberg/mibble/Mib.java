/*
 * Mib.java
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
 * Copyright (c) 2003 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;
import net.percederberg.mibble.asn1.Asn1Parser;
import net.percederberg.mibble.value.NumberValue;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * An SNMP MIB container. This class contains all the information 
 * from a MIB file, including all defined types and values. MIB files
 * are loaded through a {@link MibLoader MIB loader}.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 * 
 * @see <a href="http://www.ietf.org/rfc/rfc3411.txt">RFC 3411 - An 
 *      Architecture for Describing SNMP Management Frameworks</a>
 */
public class Mib implements MibContext {

    /**
     * The MIB file.
     */
    private File file;

    /**
     * The loader used for this MIB.
     */
    private MibLoader loader;

    /**
     * The loader log used for loading this MIB.
     */
    private MibLoaderLog log;

    /**
     * The MIB name.
     */
    private String name = null;

    /**
     * The references to imported MIB files.
     */
    private ArrayList imports = new ArrayList();

    /**
     * The MIB symbol list. This list contains the MIB symbol objects
     * in the order they were added (i.e. present in the file).
     */
    private ArrayList symbolList = new ArrayList();

    /**
     * The MIB symbol name map. This maps the symbol names to their 
     * respective MIB symbol objects.
     */
    private HashMap symbolNameMap = new HashMap();

    /**
     * The MIB symbol value map. This maps the symbol values to their
     * respective MIB symbol objects. Only the value symbols with 
     * either a number or an object identifier value is present in 
     * this map. 
     */
    private HashMap symbolValueMap = new HashMap();

    /**
     * Creates a new MIB container. This will read the MIB file and
     * create initial MIB symbols. Note that this only corresponds to
     * the first analysis pass (of two), leaving symbols in the MIB 
     * possibly containing unresolved references. A separate call to 
     * initialize() must be made once all referenced MIB files have
     * been loaded.
     * 
     * @param file           the MIB file to load
     * @param loader         the MIB loader to use for imports
     * @param log            the MIB log to use for errors
     * 
     * @throws FileNotFoundException if the MIB file couldn't be 
     *             found
     * @throws MibLoaderException if the MIB file couldn't be parsed 
     *             or analyzed correctly
     * 
     * @see #initialize()
     */
    Mib(File file, MibLoader loader, MibLoaderLog log) 
        throws FileNotFoundException, MibLoaderException {

        this(new FileReader(file), file, loader, log);
    }
    
    /**
     * Creates a new MIB container. This will read the MIB file and
     * create initial MIB symbols. Note that this only corresponds to
     * the first analysis pass (of two), leaving symbols in the MIB 
     * possibly containing unresolved references. A separate call to 
     * initialize() must be made once all referenced MIB files have
     * been loaded.
     *
     * @param input          the input stream to read 
     * @param file           the MIB file name
     * @param loader         the MIB loader to use for imports
     * @param log            the MIB log to use for errors
     * 
     * @throws MibLoaderException if the MIB file couldn't be parsed 
     *             or analyzed correctly
     * 
     * @see #initialize()
     */
    Mib(Reader input, File file, MibLoader loader, MibLoaderLog log)
        throws MibLoaderException {

        // Initialize instance variables
        this.file = file;
        this.loader = loader;
        this.log = log;

        // Parse MIB file
        parse(input);
    }

    /**
     * Parses the MIB file. This will read the MIB file and create 
     * the MIB symbols, types and values. Note that this only 
     * corresponds to the first analysis pass (of three), leaving 
     * possible unresolved references in types and values. Separate 
     * calls to initialize() and validate() must be made once all 
     * referenced MIB files have been loaded.
     * 
     * @param input          the input stream to read
     *  
     * @throws MibLoaderException if the MIB file couldn't be parsed 
     *             or analyzed correctly
     * 
     * @see #initialize()
     * @see #validate()
     */
    private void parse(Reader input) throws MibLoaderException {

        Asn1Parser  parser;
        String      msg;

        try {
            parser = new Asn1Parser(input, new MibAnalyzer(this));
            parser.parse();
        } catch (ParserCreationException e) {
            msg = "parser creation error in ASN.1 parser: " + 
                  e.getMessage();
            log.addInternalError(file, msg);
            throw new MibLoaderException(log);
        } catch (ParserLogException e) {
            log.addAll(file, e);
            throw new MibLoaderException(log);
        }
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
        MibReference  ref;
        int           errors = log.errorCount();

        // Resolve imported MIB files
        for (int i = 0; i < imports.size(); i++) {
            ref = (MibReference) imports.get(i);
            try {
                ref.initialize(log);
            } catch (MibException e) {
                log.addError(e.getLocation(), e.getMessage());
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
        MibSymbol       symbol;
        MibValueSymbol  value;
        int             errors = log.errorCount();

        // Validate all symbols
        for (int i = 0; i < symbolList.size(); i++) {
            symbol = (MibSymbol) symbolList.get(i);
            try {
                symbol.initialize(log);
            } catch (MibException e) {
                log.addError(e.getLocation(), e.getMessage());
            }
            if (symbol instanceof MibValueSymbol) {
                value = (MibValueSymbol) symbol;
                if (value.getValue() instanceof NumberValue
                 || value.getValue() instanceof ObjectIdentifierValue) {

                    symbolValueMap.put(value.getValue().toString(), symbol);
                }
            }
        }

        // Check for errors
        if (errors != log.errorCount()) {
            throw new MibLoaderException(log);
        }
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
        } else if (obj instanceof File) {
            return file.equals(obj);
        } else if (obj instanceof Mib) {
            return obj.equals(name);
        } else {
            return false;
        }
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
        return file;
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
     * Returns an imported MIB reference.
     * 
     * @param name           the MIB name
     * 
     * @return the MIB reference, or null if not found
     */
    MibReference getImport(String name) {
        MibReference  ref;

        for (int i = 0; i < imports.size(); i++) {
            ref = (MibReference) imports.get(i);
            if (ref.getName().equals(name)) {
                return ref;
            }
        }
        return null;
    }
    
    /**
     * Adds a reference to an imported MIB file.
     * 
     * @param ref            the reference to add
     */
    void addImport(MibReference ref) {
        imports.add(ref);
    }

    /**
     * Returns all symbols in this MIB.
     * 
     * @return a collection of the MIB symbols
     */
    public Collection getAllSymbols() {
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
        return (MibSymbol) symbolNameMap.get(name);
    }
    
    /**
     * Returns a value symbol from this MIB.
     * 
     * @param value          the symbol value
     * 
     * @return the MIB value symbol, or null if not found
     */
    public MibValueSymbol getSymbolByValue(String value) {
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
     * Adds a symbol to this MIB.
     * 
     * @param symbol         the symbol to add
     */
    void addSymbol(MibSymbol symbol) {
        symbolList.add(symbol);
        symbolNameMap.put(symbol.getName(), symbol);
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
