/*
 * MibAnalyzer.java
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
 * Copyright (c) 2004-2008 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;

import net.percederberg.grammatica.parser.Node;
import net.percederberg.grammatica.parser.ParseException;
import net.percederberg.grammatica.parser.Production;
import net.percederberg.grammatica.parser.Token;
import net.percederberg.mibble.asn1.Asn1Analyzer;
import net.percederberg.mibble.asn1.Asn1Constants;
import net.percederberg.mibble.snmp.SnmpAccess;
import net.percederberg.mibble.snmp.SnmpAgentCapabilities;
import net.percederberg.mibble.snmp.SnmpCompliance;
import net.percederberg.mibble.snmp.SnmpIndex;
import net.percederberg.mibble.snmp.SnmpModule;
import net.percederberg.mibble.snmp.SnmpModuleCompliance;
import net.percederberg.mibble.snmp.SnmpModuleIdentity;
import net.percederberg.mibble.snmp.SnmpModuleSupport;
import net.percederberg.mibble.snmp.SnmpNotificationGroup;
import net.percederberg.mibble.snmp.SnmpNotificationType;
import net.percederberg.mibble.snmp.SnmpObjectGroup;
import net.percederberg.mibble.snmp.SnmpObjectIdentity;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpRevision;
import net.percederberg.mibble.snmp.SnmpStatus;
import net.percederberg.mibble.snmp.SnmpTextualConvention;
import net.percederberg.mibble.snmp.SnmpTrapType;
import net.percederberg.mibble.snmp.SnmpVariation;
import net.percederberg.mibble.type.BitSetType;
import net.percederberg.mibble.type.BooleanType;
import net.percederberg.mibble.type.ChoiceType;
import net.percederberg.mibble.type.CompoundConstraint;
import net.percederberg.mibble.type.Constraint;
import net.percederberg.mibble.type.ElementType;
import net.percederberg.mibble.type.IntegerType;
import net.percederberg.mibble.type.NullType;
import net.percederberg.mibble.type.ObjectIdentifierType;
import net.percederberg.mibble.type.RealType;
import net.percederberg.mibble.type.SequenceOfType;
import net.percederberg.mibble.type.SequenceType;
import net.percederberg.mibble.type.SizeConstraint;
import net.percederberg.mibble.type.StringType;
import net.percederberg.mibble.type.TypeReference;
import net.percederberg.mibble.type.ValueConstraint;
import net.percederberg.mibble.type.ValueRangeConstraint;
import net.percederberg.mibble.value.BinaryNumberValue;
import net.percederberg.mibble.value.BitSetValue;
import net.percederberg.mibble.value.BooleanValue;
import net.percederberg.mibble.value.HexNumberValue;
import net.percederberg.mibble.value.NullValue;
import net.percederberg.mibble.value.NumberValue;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import net.percederberg.mibble.value.StringValue;
import net.percederberg.mibble.value.ValueReference;

/**
 * A MIB file analyzer. This class analyzes the MIB file parse tree,
 * and creates appropriate MIB modules with the right symbols. This
 * analyzer handles imports by adding them to the MIB loader queue.
 * As the imported MIB symbols aren't available during the analysis,
 * type and value references will be created whenever an identifier
 * is encountered.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.0
 */
class MibAnalyzer extends Asn1Analyzer {

    /**
     * The list of MIB modules found.
     */
    private ArrayList mibs = new ArrayList();

    /**
     * The MIB file being analyzed.
     */
    private File file;

    /**
     * The MIB loader using this analyzer.
     */
    private MibLoader loader;

    /**
     * The MIB loader log.
     */
    private MibLoaderLog log;

    /**
     * The current MIB module being analyzed.
     */
    private Mib currentMib = null;

    /**
     * The base MIB symbol context. This context will be extended
     * when parsing the import list.
     */
    private MibContext baseContext = null;

    /**
     * The MIB context stack. This stack is modified during the
     * parsing to add type or import contexts as necessary. The top
     * context on the stack is returned by the getContext() method.
     *
     * @see #getContext()
     */
    private ArrayList contextStack = new ArrayList();

    /**
     * The implicit tags flag.
     */
    private boolean implicitTags = true;

    /**
     * Creates a new MIB file analyzer.
     *
     * @param file           the MIB file being analyzed
     * @param loader         the MIB loader using this analyzer
     * @param log            the MIB loader log to use
     */
    public MibAnalyzer(File file, MibLoader loader, MibLoaderLog log) {
        this.file = file;
        this.loader = loader;
        this.log = log;
    }

    /**
     * Resets this analyzer. This method is mostly used to release
     * all references to parsed data.
     */
    public void reset() {
        mibs = new ArrayList();
        currentMib = null;
        baseContext = null;
        contextStack.clear();
        implicitTags = true;
    }

    /**
     * Returns the list of MIB modules found during analysis.
     *  
     * @return a list of MIB modules
     */
    public ArrayList getMibs() {
        return mibs;
    }

    /**
     * Adds the binary number as a node value. This method will
     * convert the binary string to either an Integer, a Long, or a
     * BigInteger.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitBinaryString(Token node) {
        String  str = node.getImage();
        Number  value;

        str = str.substring(1, str.length() - 2);
        if (str.length() == 0) {
            value = new Integer(0);
        } else if (str.length() < 32) {
            value = new Integer(Integer.parseInt(str, 2));
        } else if (str.length() < 64) {
            value = new Long(Long.parseLong(str, 2));
        } else {
            value = new BigInteger(str, 2);
        }
        node.addValue(value);
        node.addValue(str);
        return node;
    }

    /**
     * Adds the hexadecimal number as a node value. This method will
     * convert the hexadecimal string to either an Integer, a Long,
     * or a BigInteger.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitHexadecimalString(Token node) {
        String  str = node.getImage();
        Number  value;

        str = str.substring(1, str.length() - 2);
        if (str.length() == 0) {
            value = new Integer(0);
        } else if (str.length() < 8) {
            value = new Integer(Integer.parseInt(str, 16));
        } else if (str.length() < 16) {
            value = new Long(Long.parseLong(str, 16));
        } else {
            value = new BigInteger(str, 16);
        }
        node.addValue(value);
        node.addValue(str);
        return node;
    }

    /**
     * Adds the quoted string as a node value. This method will
     * remove the quotation marks and replace any double marks inside
     * the string with a single mark.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitQuotedString(Token node) {
        String  str = node.getImage();
        int     pos;

        str = str.substring(1, str.length() - 1);
        do {
            pos = str.indexOf("\"\"");
            if (pos >= 0) {
                str = str.substring(0, pos) + '"' + str.substring(pos + 2);
            }
        } while (pos >= 0);
        node.addValue(str);
        return node;
    }

    /**
     * Adds the identifier string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitIdentifierString(Token node) {
        node.addValue(node.getImage());
        return node;
    }

    /**
     * Adds the number as a node value. This method will convert the
     * number string to either an Integer, a Long, or a BigInteger.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitNumberString(Token node) {
        String  str = node.getImage();
        Number  value;

        if (str.length() < 10) {
            value = new Integer(str);
        } else if (str.length() < 19) {
            value = new Long(str);
        } else {
            value = new BigInteger(str);
        }
        node.addValue(value);
        return node;
    }

    /**
     * Stores any MIB tail comments if available.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitStart(Production node) {
        String  comment = MibAnalyzerUtil.getCommentsFooter(node);

        if (currentMib != null) {
            currentMib.setFooterComment(comment);
        }
        return null;
    }

    /**
     * Creates the current MIB module container and the base context.
     *
     * @param node           the node being entered
     */
    protected void enterModuleDefinition(Production node) {
        currentMib = new Mib(file, loader, log);
        baseContext = loader.getDefaultContext();
        baseContext = new CompoundContext(currentMib, baseContext);
        pushContext(baseContext);
    }

    /**
     * Sets the MIB name to the module identifier string value. Also
     * removes this node from the parse tree.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitModuleDefinition(Production node)
        throws ParseException {

        currentMib.setName(getStringValue(getChildAt(node, 0), 0));
        currentMib.setHeaderComment(MibAnalyzerUtil.getComments(node));
        mibs.add(currentMib);
        return node;
    }

    /**
     * Adds the module identifier string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitModuleIdentifier(Production node)
        throws ParseException {

        node.addValue(getStringValue(getChildAt(node, 0), 0));
        return node;
    }

    /**
     * Adds the module identifier string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitModuleReference(Production node)
        throws ParseException {

        node.addValue(getStringValue(getChildAt(node, 0), 0));
        return node;
    }

    /**
     * Sets the implicit tags flag.
     *
     * @param node           the node being exited
     *
     * @return null to remove the node from the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitTagDefault(Production node)
        throws ParseException {

        Node  child;

        child = getChildAt(node, 0);
        if (child.getId() == Asn1Constants.EXPLICIT) {
            implicitTags = false;
        } else {
            implicitTags = true;
        }
        return null;
    }

    /**
     * Adds all imported MIB files to the MIB context. Also removes
     * this node from the parse tree.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitImportList(Production node) {
        ArrayList   imports = getChildValues(node);
        MibImport   imp;
        MibContext  current = loader.getDefaultContext();
        boolean     addMissingSmi = true;

        for (int i = 0; i < imports.size(); i++) {
            imp = (MibImport) imports.get(i);
            if (imp.getName().startsWith("RFC1065-SMI") ||
                imp.getName().startsWith("RFC1155-SMI") ||
                imp.getName().startsWith("SNMPv2-SMI")) {

                addMissingSmi = false;
            }
        }
        if (addMissingSmi) {
            // TODO: Ugly hack that adds a "hidden" SNMPv1 SMI as the last
            //       import, but without any named symbols (triggering
            //       warnings for each symbol used).
            imp = new MibImport(loader, getLocation(node), "RFC1155-SMI", new ArrayList());
            loader.scheduleLoad(imp.getName());
            currentMib.addImport(imp);
            imports.add(imp);
        }
        for (int i = imports.size() - 1; i >= 0; i--) {
            imp = (MibImport) imports.get(i);
            current = new CompoundContext(imp, current);
        }
        baseContext = new CompoundContext(currentMib, current);
        popContext();
        pushContext(baseContext);
        return null;
    }

    /**
     * Schedules the imported MIB file for loading. Also adds a MIB
     * reference as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSymbolsFromModule(Production node)
        throws ParseException {

        MibImport  imp;
        String     module;
        ArrayList  symbols;
        Node       child;

        // Create MIB reference
        child = getChildAt(node, 0);
        symbols = child.getAllValues();
        if (symbols == null) {
            symbols = new ArrayList();
        }
        child = getChildAt(node, 2);
        module = getStringValue(child, 0);
        imp = new MibImport(loader, getLocation(child), module, symbols);

        // Schedule MIB loading
        loader.scheduleLoad(module);

        // Add reference to MIB and node
        currentMib.addImport(imp);
        node.addValue(imp);
        return node;
    }

    /**
     * Adds all symbol identifiers as node values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSymbolList(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the identifier string as a node value. If the symbol name
     * is not an identifier, no node value will be added.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSymbol(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Creates a macro symbol and adds it to the MIB. Also removes
     * this node from the parse tree.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitMacroDefinition(Production node)
        throws ParseException {
        
        String          name;
        MibMacroSymbol  symbol;

        // Check macro name
        name = getStringValue(getChildAt(node, 0), 0);
        if (currentMib.getSymbol(name) != null) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "a symbol '" + name + "' already present in the MIB",
                node.getStartLine(),
                node.getStartColumn());
        }

        // Create macro symbol
        symbol = new MibMacroSymbol(getLocation(node),
                                    currentMib,
                                    name);
        symbol.setComment(MibAnalyzerUtil.getComments(node));

        return null;
    }

    /**
     * Adds the macro name as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitMacroReference(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Creates a type symbol and adds it to the MIB. Also removes
     * this node from the parse tree.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitTypeAssignment(Production node)
        throws ParseException {

        String         name;
        MibType        type;
        MibTypeSymbol  symbol;

        // Check type name
        name = getStringValue(getChildAt(node, 0), 0);
        if (currentMib.getSymbol(name) != null) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "a symbol '" + name + "' already present in the MIB",
                node.getStartLine(),
                node.getStartColumn());
        }
        if (!Character.isUpperCase(name.charAt(0))) {
            log.addWarning(getLocation(node),
                           "type identifier '" + name + "' doesn't " +
                           "start with an uppercase character");
        }

        // Create type symbol
        type = (MibType) getValue(getChildAt(node, 2), 0);
        symbol = new MibTypeSymbol(getLocation(node),
                                   currentMib,
                                   name,
                                   type);
        symbol.setComment(MibAnalyzerUtil.getComments(node));

        return null;
    }

    /**
     * Adds a MIB type as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitType(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a type reference as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitDefinedType(Production node)
        throws ParseException {

        MibContext     local = getContext();
        String         name = null;
        Object         value = null;
        FileLocation   loc = getLocation(node);
        Node           child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.MODULE_REFERENCE:
                name = getStringValue(child, 0);
                local = currentMib.getImport(name);
                if (local == null) {
                    throw new ParseException(
                        ParseException.ANALYSIS_ERROR,
                        "referenced module not imported '" + name + "'",
                        child.getStartLine(),
                        child.getStartColumn());
                }
                break;
            case Asn1Constants.IDENTIFIER_STRING:
                name = getStringValue(child, 0);
                break;
            case Asn1Constants.VALUE_OR_CONSTRAINT_LIST:
                value = getValue(child, 0);
            }
        }
        if (value instanceof Constraint) {
            value = new TypeReference(loc, local, name, (Constraint) value);
        } else if (value instanceof ArrayList) {
            value = new TypeReference(loc, local, name, (ArrayList) value);
        } else {
            value = new TypeReference(loc, local, name);
        }
        node.addValue(value);
        return node;
    }

    /**
     * Adds a MIB type as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitBuiltinType(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a null type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitNullType(Production node) {
        node.addValue(new NullType());
        return node;
    }

    /**
     * Adds a boolean type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitBooleanType(Production node) {
        node.addValue(new BooleanType());
        return node;
    }

    /**
     * Adds a real type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitRealType(Production node) {
        node.addValue(new RealType());
        return node;
    }

    /**
     * Adds an integer type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitIntegerType(Production node) {
        IntegerType  type;
        ArrayList    values;
        Object       obj;

        values = getChildValues(node);
        if (values.size() == 0) {
            type = new IntegerType();
        } else {
            obj = values.get(0);
            if (obj instanceof ArrayList) {
                type = new IntegerType((ArrayList) obj);
            } else {
                type = new IntegerType((Constraint) obj);
            }
        }
        node.addValue(type);
        return node;
    }

    /**
     * Adds an object identifier type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitObjectIdentifierType(Production node) {
        node.addValue(new ObjectIdentifierType());
        return node;
    }

    /**
     * Adds a string type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitStringType(Production node) {
        StringType  type;
        ArrayList   values;

        values = getChildValues(node);
        if (values.size() == 0) {
            type = new StringType();
        } else {
            type = new StringType((Constraint) values.get(0));
        }
        node.addValue(type);
        return node;
    }

    /**
     * Adds a bit set type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitBitStringType(Production node) {
        BitSetType  type;
        ArrayList   values;
        Object      obj;

        values = getChildValues(node);
        if (values.size() == 0) {
            type = new BitSetType();
        } else {
            obj = values.get(0);
            if (obj instanceof ArrayList) {
                type = new BitSetType((ArrayList) obj);
            } else {
                type = new BitSetType((Constraint) obj);
            }
        }
        node.addValue(type);
        return node;
    }

    /**
     * Adds a bit set type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitBitsType(Production node) {
        return exitBitStringType(node);
    }

    /**
     * Adds a MIB sequence type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSequenceType(Production node) {
        ArrayList  elements = getChildValues(node);

        node.addValue(new SequenceType(elements));
        return node;
    }

    /**
     * Adds a sequence of MIB type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSequenceOfType(Production node)
        throws ParseException {

        MibType     type;
        Constraint  c = null;
        Node        child;

        child = getChildAt(node, node.getChildCount() - 1);
        type = (MibType) getValue(child, 0);
        if (node.getChildCount() == 4) {
            child = getChildAt(node, 1);
            c = (Constraint) getValue(child, 0);
        }
        node.addValue(new SequenceOfType(type, c));
        return node;
    }

    /**
     * Adds a null type as a node value. This method also prints an
     * error about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSetType(Production node) {
        // TODO: implement set type support
        log.addError(getLocation(node),
                     "SET type currently unsupported");
        node.addValue(new NullType());
        return node;
    }

    /**
     * Adds a null type as a node value. This method also prints an
     * error about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSetOfType(Production node) {
        // TODO: implement set of type support
        log.addError(getLocation(node),
                     "SET OF type currently unsupported");
        node.addValue(new NullType());
        return node;
    }

    /**
     * Adds a MIB choice type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitChoiceType(Production node) {
        node.addValue(new ChoiceType(getChildValues(node)));
        return node;
    }

    /**
     * Adds a null type as a node value. This method also prints an
     * error about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitEnumeratedType(Production node) {
        // TODO: implement enumerated type support
        log.addError(getLocation(node),
                     "ENUMERATED type currently unsupported");
        node.addValue(new NullType());
        return node;
    }

    /**
     * Adds a null type as a node value. This method also prints an
     * error about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSelectionType(Production node) {
        // TODO: implement selection type support
        log.addError(getLocation(node),
                     "selection type currently unsupported");
        node.addValue(new NullType());
        return node;
    }

    /**
     * Adds the tagged type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitTaggedType(Production node)
        throws ParseException {

        MibType     type;
        MibTypeTag  tag;
        boolean     implicit = implicitTags;
        Node        child;

        child = getChildAt(node, 0);
        tag = (MibTypeTag) getValue(child, 0);
        child = getChildAt(node, 1);
        if (child.getId() == Asn1Constants.EXPLICIT_OR_IMPLICIT_TAG) {
            implicit = ((Boolean) getValue(child, 0)).booleanValue();
        }
        child = getChildAt(node, node.getChildCount() - 1);
        type = (MibType) getValue(child, 0);
        type.setTag(implicit, tag);
        node.addValue(type);
        return node;
    }

    /**
     * Called when exiting a parse tree node.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree, or
     *         null if no parse tree should be created
     */
    protected Node exitTag(Production node) {
        ArrayList  values = getChildValues(node);
        int        category = MibTypeTag.CONTEXT_SPECIFIC_CATEGORY;
        int        value;

        if (values.size() == 1) {
            value = ((Number) values.get(0)).intValue();
        } else {
            category = ((Integer) values.get(0)).intValue();
            value = ((Number) values.get(1)).intValue();
        }
        node.addValue(new MibTypeTag(category, value));
        return node;
    }

    /**
     * Adds the type tag category value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitClass(Production node) throws ParseException {
        Node  child = getChildAt(node, 0);
        int   category;

        if (child.getId() == Asn1Constants.UNIVERSAL) {
            category = MibTypeTag.UNIVERSAL_CATEGORY;
        } else if (child.getId() == Asn1Constants.APPLICATION) {
            category = MibTypeTag.APPLICATION_CATEGORY;
        } else if (child.getId() == Asn1Constants.PRIVATE) {
            category = MibTypeTag.PRIVATE_CATEGORY;
        } else {
            category = MibTypeTag.CONTEXT_SPECIFIC_CATEGORY;
        }
        node.addValue(new Integer(category));
        return node;
    }

    /**
     * Adds the implicit boolean flag as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitExplicitOrImplicitTag(Production node)
        throws ParseException {

        Node  child = getChildAt(node, 0);

        if (child.getId() == Asn1Constants.EXPLICIT) {
            node.addValue(Boolean.FALSE);
        } else {
            node.addValue(Boolean.TRUE);
        }
        return node;
    }

    /**
     * Adds a null type as a node value. This method also prints an
     * error about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitAnyType(Production node) {
        // TODO: implement any type support
        log.addError(getLocation(node),
                     "ANY type currently unsupported");
        node.addValue(new NullType());
        return node;
    }

    /**
     * Adds all element types as a node values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitElementTypeList(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds an element type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitElementType(Production node)
        throws ParseException {

        String   name = null;
        MibType  type;
        Node     child;

        child = getChildAt(node, 0);
        if (child.getId() == Asn1Constants.IDENTIFIER_STRING) {
            name = getStringValue(child, 0);
            child = getChildAt(node, 1);
        }
        if (child.getId() != Asn1Constants.TYPE) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "referencing components is currently unsupported",
                child.getStartLine(),
                child.getStartColumn());
        }
        type = new ElementType(name, (MibType) getValue(child, 0));
        type.setComment(MibAnalyzerUtil.getComments(node));
        node.addValue(type);
        return node;
    }

    /**
     * Prints an error about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitOptionalOrDefaultElement(Production node) {
        // TODO: implement this method?
        log.addError(getLocation(node),
                     "optional and default elements are currently " +
                     "unsupported");
        return null;
    }

    /**
     * Adds an array list with symbols or a constraint as the node
     * value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitValueOrConstraintList(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds an array list with symbols as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitNamedNumberList(Production node) {
        MibValueSymbol  symbol;
        Node            child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            if (child.getId() == Asn1Constants.NAMED_NUMBER) {
                symbol = (MibValueSymbol) child.getValue(0);
                symbol.setComment(MibAnalyzerUtil.getComments(child));
            }
        }
        node.addValue(getChildValues(node));
        return node;
    }

    /**
     * Adds a value symbol as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitNamedNumber(Production node)
        throws ParseException {

        MibValueSymbol  symbol;
        String          name;
        MibValue        value;

        name = getStringValue(getChildAt(node, 0), 0);
        value = (MibValue) getValue(getChildAt(node, 2), 0);
        symbol = new MibValueSymbol(getLocation(node),
                                    null,
                                    name,
                                    null,
                                    value);
        node.addValue(symbol);
        return node;
    }

    /**
     * Adds a MIB value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitNumber(Production node) throws ParseException {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a MIB type constraint as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitConstraintList(Production node) {
        Constraint  result = null;
        ArrayList   values;
        Constraint  c;

        values = getChildValues(node);
        for (int i = values.size() - 1; i >= 0; i--) {
            c = (Constraint) values.get(i);
            if (result == null) {
                result = c;
            } else {
                result = new CompoundConstraint(c, result);
            }
        }
        node.addValue(result);
        return node;
    }

    /**
     * Adds a MIB type constraint as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitConstraint(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a MIB type constraint as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitValueConstraintList(Production node) {
        return exitConstraintList(node);
    }

    /**
     * Adds a MIB type value or value range constraint as a node
     * value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitValueConstraint(Production node)
        throws ParseException {

        ArrayList  list = getChildValues(node);
        MibValue   lower = null;
        MibValue   upper = null;
        Boolean    strictLower = null;
        Boolean    strictUpper = null;
        Object     obj;

        if (list.size() == 0) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "no value specified in constraint",
                node.getStartLine(),
                node.getStartColumn());
        } else if (list.size() == 1) {
            lower = (MibValue) list.get(0);
            obj = new ValueConstraint(getLocation(node), lower);
        } else {
            for (int i = 0; i < list.size(); i++) {
                obj = list.get(i);
                if (obj instanceof Boolean && strictLower == null) {
                    strictLower = (Boolean) obj;
                } else if (obj instanceof Boolean) {
                    strictUpper = (Boolean) obj;
                } else if (strictLower == null) {
                    lower = (MibValue) obj;
                } else {
                    upper = (MibValue) obj;
                }
            }
            if (strictLower == null) {
                strictLower = Boolean.FALSE;
            }
            if (strictUpper == null) {
                strictUpper = Boolean.FALSE;
            }
            obj = new ValueRangeConstraint(getLocation(node),
                                           lower,
                                           strictLower.booleanValue(),
                                           upper,
                                           strictUpper.booleanValue());
        }
        node.addValue(obj);
        return node;
    }

    /**
     * Adds the upper end point and strict inequality flags as node
     * values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitValueRange(Production node)
        throws ParseException {

        Node  child;

        // Check for strict lower end point
        child = getChildAt(node, 0);
        if (child.getId() == Asn1Constants.LESS_THAN) {
            node.addValue(Boolean.TRUE);
        } else {
            node.addValue(Boolean.FALSE);
        }

        // Add upper end point (or null)
        child = getChildAt(node, node.getChildCount() - 1);
        node.addValue(child.getValue(0));

        // Check for strict upper end point
        child = getChildAt(node, node.getChildCount() - 2);
        if (child.getId() == Asn1Constants.LESS_THAN) {
            node.addValue(Boolean.TRUE);
        } else {
            node.addValue(Boolean.FALSE);
        }

        return node;
    }

    /**
     * Adds a MIB value or null as a node value. The null value is
     * used to represent a minimum value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitLowerEndPoint(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a MIB value or null as a node value. The null value is
     * used to represent a maximum value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitUpperEndPoint(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a MIB type size constraint as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSizeConstraint(Production node)
        throws ParseException {

        Constraint  c;

        c = (Constraint) getValue(getChildAt(node, 1), 0);
        node.addValue(new SizeConstraint(getLocation(node), c));
        return node;
    }

    /**
     * Removes this node from the parse tree, and prints an error
     * about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitAlphabetConstraint(Production node) {
        // TODO: implement alphabet constraints
        log.addError(getLocation(node),
                     "FROM constraints are currently unsupported");
        return null;
    }

    /**
     * Removes this node from the parse tree, and prints an error
     * about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitContainedTypeConstraint(Production node) {
        // TODO: implement contained type constraints
        log.addError(getLocation(node),
                     "INCLUDES constraints are currently unsupported");
        return null;
    }

    /**
     * Removes this node from the parse tree, and prints an error
     * about this construct being unsupported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitInnerTypeConstraint(Production node) {
        // TODO: implement inner type constraints
        log.addError(getLocation(node),
                     "WITH COMPONENT(S) constraints are currently " +
                     "unsupported");
        return null;
    }

    /**
     * Creates a value symbol and adds it to the MIB. Also removes
     * this node from the parse tree.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitValueAssignment(Production node)
        throws ParseException {

        String          name;
        MibType         type;
        MibValue        value;
        MibValueSymbol  symbol;

        // Check value name
        name = getStringValue(getChildAt(node, 0), 0);
        if (currentMib.getSymbol(name) != null) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "a symbol '" + name + "' already present in the MIB",
                node.getStartLine(),
                node.getStartColumn());
        }
        if (!Character.isLowerCase(name.charAt(0))) {
            log.addWarning(getLocation(node),
                           "value identifier '" + name + "' doesn't " +
                           "start with a lowercase character");
        }

        // Create value symbol
        type = (MibType) getValue(getChildAt(node, 1), 0);
        value = (MibValue) getValue(getChildAt(node, 3), 0);
        symbol = new MibValueSymbol(getLocation(node),
                                    currentMib,
                                    name,
                                    type,
                                    value);
        symbol.setComment(MibAnalyzerUtil.getComments(node));

        return null;
    }

    /**
     * Adds a MIB value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitValue(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a value reference as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitDefinedValue(Production node)
        throws ParseException {

        ValueReference  ref;
        MibContext      local = getContext();
        String          name;
        Node            child;

        // Check for module reference
        child = getChildAt(node, 0);
        if (child.getId() == Asn1Constants.MODULE_REFERENCE) {
            name = getStringValue(child, 0);
            local = currentMib.getImport(name);
            if (local == null) {
                throw new ParseException(
                    ParseException.ANALYSIS_ERROR,
                    "referenced module not imported '" + name + "'",
                    child.getStartLine(),
                    child.getStartColumn());
            }
            child = getChildAt(node, 1);
        }

        // Create value reference
        name = getStringValue(child, 0);
        ref = new ValueReference(getLocation(node), local, name);
        node.addValue(ref);
        return node;
    }

    /**
     * Adds a MIB value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitBuiltinValue(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a MIB null value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitNullValue(Production node) {
        node.addValue(NullValue.NULL);
        return node;
    }

    /**
     * Adds a MIB boolean value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitBooleanValue(Production node)
        throws ParseException {

        Node  child = getChildAt(node, 0);

        if (child.getId() == Asn1Constants.TRUE) {
            node.addValue(BooleanValue.TRUE);
        } else {
            node.addValue(BooleanValue.FALSE);
        }
        return node;
    }

    /**
     * Adds a MIB number value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSpecialRealValue(Production node)
        throws ParseException {

        Number  number;

        if (getChildAt(node, 0).getId() == Asn1Constants.PLUS_INFINITY) {
            number = new Float(Float.POSITIVE_INFINITY);
        } else {
            number = new Float(Float.NEGATIVE_INFINITY);
        }
        node.addValue(new NumberValue(number));
        return node;
    }

    /**
     * Adds a MIB number value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitNumberValue(Production node)
        throws ParseException {

        Number  number;

        if (getChildAt(node, 0).getId() == Asn1Constants.MINUS) {
            number = (Number) getValue(getChildAt(node, 1), 0);
            if (number instanceof Integer) {
                number = new Integer(-number.intValue());
            } else if (number instanceof Long) {
                number = new Long(-number.longValue());
            } else {
                number = ((BigInteger) number).negate();
            }
        } else {
            number = (Number) getValue(getChildAt(node, 0), 0);
        }
        node.addValue(new NumberValue(number));
        return node;
    }

    /**
     * Adds a MIB number value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitBinaryValue(Production node)
        throws ParseException {

        Node    child;
        Number  number;
        String  text;

        child = getChildAt(node, 0);
        number = (Number) child.getValue(0);
        text = (String) child.getValue(1);
        node.addValue(new BinaryNumberValue(number, text.length()));
        return node;
    }

    /**
     * Adds a MIB number value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitHexadecimalValue(Production node)
        throws ParseException {

        Node    child;
        Number  number;
        String  text;

        child = getChildAt(node, 0);
        number = (Number) child.getValue(0);
        text = (String) child.getValue(1);
        node.addValue(new HexNumberValue(number, text.length()));
        return node;
    }

    /**
     * Adds a MIB string value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitStringValue(Production node)
        throws ParseException {

        String  str;

        str = getStringValue(getChildAt(node, 0), 0);
        node.addValue(new StringValue(str));
        return node;
    }

    /**
     * Adds a MIB value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitBitOrObjectIdentifierValue(Production node)
        throws ParseException {

        if (MibAnalyzerUtil.isBitValue(node)) {
            return exitBitValue(node);
        } else {
            return exitObjectIdentifierValue(node);
        }
    }

    /**
     * Adds a MIB bit set value as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitBitValue(Production node) throws ParseException {
        ArrayList     components = getChildValues(node);
        BitSet        bits = new BitSet();
        ArrayList     values = new ArrayList();
        NamedNumber   number;

        for (int i = 0; i < components.size(); i++) {
            number = (NamedNumber) components.get(i);
            if (number.hasNumber()) {
                bits.set(number.getNumber().intValue());
            } else {
                values.add(number.getReference());
            }
        }
        node.addValue(new BitSetValue(bits, values));
        return node;
    }

    /**
     * Adds a MIB object identifier value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitObjectIdentifierValue(Production node)
        throws ParseException {

        ArrayList     components = getChildValues(node);
        MibValue      parent = null;
        NamedNumber   number;
        int           value;

        // Check for minimum number of components
        if (components.size() < 1) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "object identifier must contain at least one component",
                node.getStartLine(),
                node.getStartColumn());
        }

        // Analyze components
        for (int i = 0; i < components.size(); i++) {
            number = (NamedNumber) components.get(i);
            if (number.hasNumber()) {
                value = number.getNumber().intValue();
                if (parent == null && value == 0) {
                    parent = new ValueReference(getLocation(node),
                                                getContext(),
                                                DefaultContext.CCITT);
                } else if (parent == null && value == 1) {
                    parent = new ValueReference(getLocation(node),
                                                getContext(),
                                                DefaultContext.ISO);
                } else if (parent == null && value == 2) {
                    parent = new ValueReference(getLocation(node),
                                                getContext(),
                                                DefaultContext.JOINT_ISO_CCITT);
                } else if (parent instanceof ObjectIdentifierValue) {
                    try {
                        parent = new ObjectIdentifierValue(
                                        getLocation(node),
                                        (ObjectIdentifierValue) parent,
                                        number.getName(),
                                        value);
                    } catch (MibException e) {
                        log.addError(e.getLocation(), e.getMessage());
                        parent = null;
                    }
                } else {
                    parent = new ObjectIdentifierValue(
                                        getLocation(node),
                                        (ValueReference) parent,
                                        number.getName(),
                                        value);
                }
            } else if (parent != null) {
                throw new ParseException(
                    ParseException.ANALYSIS_ERROR,
                    "object identifier component '" + number.getName() +
                    "' has been previously defined, remove any " +
                    "components to the left",
                    node.getStartLine(),
                    node.getStartColumn());
            } else {
                parent = number.getReference();
            }
        }

        // Set node value
        node.addValue(parent);
        return node;
    }

    /**
     * Adds all the named numbers as the node values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitNameValueList(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a named number as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitNameValueComponent(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }


    /**
     * Adds a named number as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitNameOrNumber(Production node)
        throws ParseException {

        NamedNumber     value;
        Object          obj;
        ValueReference  ref;

        obj = getValue(getChildAt(node, 0), 0);
        if (obj instanceof Number) {
            value = new NamedNumber((Number) obj);
        } else if (obj instanceof String) {
            ref = new ValueReference(getLocation(node),
                                     getContext(),
                                     (String) obj);
            value = new NamedNumber((String) obj, ref);
        } else {
            value = (NamedNumber) obj;
        }
        node.addValue(value);
        return node;
    }

    /**
     * Adds a named number as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitNameAndNumber(Production node)
        throws ParseException {

        NamedNumber  value;
        String       name;
        Object       obj;

        name = getStringValue(getChildAt(node, 0), 0);
        obj = getValue(getChildAt(node, 2), 0);
        if (obj instanceof Number) {
            value = new NamedNumber(name, (Number) obj);
        } else {
            value = new NamedNumber(name, (ValueReference) obj);
        }
        node.addValue(value);
        return node;
    }

    /**
     * Adds an SNMP type as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitDefinedMacroType(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the defined macro name as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitDefinedMacroName(Production node) {
        node.addValue(((Token) node.getChildAt(0)).getImage());
        return node;
    }

    /**
     * Adds an SNMP module identity as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpModuleIdentityMacroType(Production node)
        throws ParseException {

        String     update;
        String     org;
        String     contact;
        String     desc;
        ArrayList  revisions = new ArrayList();

        currentMib.setSmiVersion(2);
        update = getStringValue(getChildAt(node, 1), 0);
        org = getStringValue(getChildAt(node, 2), 0);
        contact = getStringValue(getChildAt(node, 3), 0);
        desc = getStringValue(getChildAt(node, 4), 0);
        for (int i = 5; i < node.getChildCount(); i++) {
            revisions.add(getValue(getChildAt(node, i), 0));
        }
        node.addValue(new SnmpModuleIdentity(update,
                                             org,
                                             contact,
                                             desc,
                                             revisions));
        return node;
    }

    /**
     * Called when exiting a parse tree node.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpObjectIdentityMacroType(Production node)
        throws ParseException {

        SnmpStatus  status;
        String      desc;
        String      ref;

        currentMib.setSmiVersion(2);
        status = (SnmpStatus) getValue(getChildAt(node, 1), 0);
        desc = getStringValue(getChildAt(node, 2), 0);
        if (node.getChildCount() <= 3) {
            ref = null;
        } else {
            ref = getStringValue(getChildAt(node, 3), 0);
        }
        node.addValue(new SnmpObjectIdentity(status, desc, ref));
        return node;
    }

    /**
     * Adds the syntax type to the MIB context stack if possible.
     *
     * @param node           the parent node
     * @param child          the child node, or null
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected void childSnmpObjectTypeMacroType(Production node, Node child)
        throws ParseException {

        MibType  type;

        if (child.getId() == Asn1Constants.SNMP_SYNTAX_PART) {
            type = (MibType) getValue(child, 0);
            if (type instanceof MibContext) {
                pushContextExtension((MibContext) type);
            }
        }
        node.addChild(child);
    }

    /**
     * Adds an SNMP object type as a node value. This method also
     * removes any syntax type from the MIB context stack if needed.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpObjectTypeMacroType(Production node)
        throws ParseException {

        SnmpObjectType  type;
        MibType         syntax = null;
        String          units = null;
        SnmpAccess      access = null;
        SnmpStatus      status = null;
        String          desc = null;
        String          ref = null;
        Object          index = null;
        MibValue        defVal = null;
        Node            child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_SYNTAX_PART:
                syntax = (MibType) getValue(child, 0);
                if (syntax instanceof MibContext) {
                    popContext();
                }
                syntax.setComment(MibAnalyzerUtil.getComments(child));
                break;
            case Asn1Constants.SNMP_UNITS_PART:
                units = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_ACCESS_PART:
                access = (SnmpAccess) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_STATUS_PART:
                status = (SnmpStatus) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_REFER_PART:
                ref = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_INDEX_PART:
                index = getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DEF_VAL_PART:
                defVal = (MibValue) getValue(child, 0);
                break;
            }
        }
        if (index instanceof ArrayList) {
            type = new SnmpObjectType(syntax,
                                      units,
                                      access,
                                      status,
                                      desc,
                                      ref,
                                      (ArrayList) index,
                                      defVal);
        } else {
            type = new SnmpObjectType(syntax,
                                      units,
                                      access,
                                      status,
                                      desc,
                                      ref,
                                      (MibValue) index,
                                      defVal);
        }
        node.addValue(type);
        return node;
    }

    /**
     * Adds an SNMP notification type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpNotificationTypeMacroType(Production node)
        throws ParseException {

        ArrayList   objects = new ArrayList();
        SnmpStatus  status = null;
        String      desc = null;
        String      ref = null;
        Node        child;

        currentMib.setSmiVersion(2);
        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_OBJECTS_PART:
                objects = (ArrayList) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_STATUS_PART:
                status = (SnmpStatus) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_REFER_PART:
                ref = getStringValue(child, 0);
                break;
            }
        }
        node.addValue(new SnmpNotificationType(objects,
                                               status,
                                               desc,
                                               ref));
        return node;
    }

    /**
     * Adds an SNMP trap type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpTrapTypeMacroType(Production node)
        throws ParseException {

        MibValue   enterprise = null;
        ArrayList  vars = new ArrayList();
        String     desc = null;
        String     ref = null;
        Node       child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_ENTERPRISE_PART:
                enterprise = (MibValue) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_VAR_PART:
                vars = child.getAllValues();
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_REFER_PART:
                ref = getStringValue(child, 0);
                break;
            }
        }
        node.addValue(new SnmpTrapType(enterprise, vars, desc, ref));
        return node;
    }

    /**
     * Adds an SNMP textual convention as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpTextualConventionMacroType(Production node)
        throws ParseException {

        String      display = null;
        SnmpStatus  status = null;
        String      desc = null;
        String      ref = null;
        MibType     syntax = null;
        Node        child;

        currentMib.setSmiVersion(2);
        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_DISPLAY_PART:
                display = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_STATUS_PART:
                status = (SnmpStatus) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_REFER_PART:
                ref = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_SYNTAX_PART:
                syntax = (MibType) getValue(child, 0);
                syntax.setComment(MibAnalyzerUtil.getComments(child));
                break;
            }
        }
        node.addValue(new SnmpTextualConvention(display,
                                                status,
                                                desc,
                                                ref,
                                                syntax));
        return node;
    }

    /**
     * Adds an SNMP object group as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpObjectGroupMacroType(Production node)
        throws ParseException {

        ArrayList   objects;
        SnmpStatus  status;
        String      desc;
        String      ref;

        currentMib.setSmiVersion(2);
        objects = (ArrayList) getValue(getChildAt(node, 1), 0);
        status = (SnmpStatus) getValue(getChildAt(node, 2), 0);
        desc = getStringValue(getChildAt(node, 3), 0);
        if (node.getChildCount() <= 4) {
            ref = null;
        } else {
            ref = getStringValue(getChildAt(node, 4), 0);
        }
        node.addValue(new SnmpObjectGroup(objects, status, desc, ref));
        return node;
    }

    /**
     * Adds an SNMP notification group as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpNotificationGroupMacroType(Production node)
        throws ParseException {

        ArrayList   notifications;
        SnmpStatus  status;
        String      desc;
        String      ref;

        currentMib.setSmiVersion(2);
        notifications = getChildAt(node, 1).getAllValues();
        status = (SnmpStatus) getValue(getChildAt(node, 2), 0);
        desc = getStringValue(getChildAt(node, 3), 0);
        if (node.getChildCount() <= 4) {
            ref = null;
        } else {
            ref = getStringValue(getChildAt(node, 4), 0);
        }
        node.addValue(new SnmpNotificationGroup(notifications,
                                                status,
                                                desc,
                                                ref));
        return node;
    }

    /**
     * Adds an SNMP module compliance type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpModuleComplianceMacroType(Production node)
        throws ParseException {

        SnmpStatus  status = null;
        String      desc = null;
        String      ref = null;
        ArrayList   modules = new ArrayList();
        Node        child;

        currentMib.setSmiVersion(2);
        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_STATUS_PART:
                status = (SnmpStatus) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_REFER_PART:
                ref = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_MODULE_PART:
                modules.add(getValue(child, 0));
                break;
            }
        }
        node.addValue(new SnmpModuleCompliance(status,
                                               desc,
                                               ref,
                                               modules));
        return node;
    }

    /**
     * Adds an SNMP agent capabilities as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpAgentCapabilitiesMacroType(Production node)
        throws ParseException {

        String      prod = null;
        SnmpStatus  status = null;
        String      desc = null;
        String      ref = null;
        ArrayList   modules = new ArrayList();
        Node        child;

        currentMib.setSmiVersion(2);
        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_PRODUCT_RELEASE_PART:
                prod = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_STATUS_PART:
                status = (SnmpStatus) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_REFER_PART:
                ref = getStringValue(child, 0);
                break;
            case Asn1Constants.SNMP_MODULE_SUPPORT_PART:
                modules.add(getValue(child, 0));
                break;
            }
        }
        node.addValue(new SnmpAgentCapabilities(prod,
                                                status,
                                                desc,
                                                ref,
                                                modules));
        return node;
    }

    /**
     * Adds the last update string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpUpdatePart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the organization name as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpOrganizationPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the organization contact info as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpContactPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the description string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpDescrPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds an SNMP revision as the node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpRevisionPart(Production node)
        throws ParseException {

        SnmpRevision  rev;
        MibValue      value;
        String        desc;

        value = (MibValue) getValue(getChildAt(node, 1), 0);
        desc = getStringValue(getChildAt(node, 3), 0);
        rev = new SnmpRevision(value, desc);
        rev.setComment(MibAnalyzerUtil.getComments(node));
        node.addValue(rev);
        return node;
    }

    /**
     * Adds an SNMP status as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpStatusPart(Production node)
        throws ParseException {

        Node    child;
        String  name;

        child = getChildAt(node, 1);
        name = getStringValue(child, 0);
        if (name.equals("mandatory")) {
            node.addValue(SnmpStatus.MANDATORY);
        } else if (name.equals("optional")) {
            node.addValue(SnmpStatus.OPTIONAL);
        } else if (name.equals("current")) {
            node.addValue(SnmpStatus.CURRENT);
        } else if (name.equals("deprecated")) {
            node.addValue(SnmpStatus.DEPRECATED);
        } else if (name.equals("obsolete")) {
            node.addValue(SnmpStatus.OBSOLETE);
        } else {
            node.addValue(SnmpStatus.CURRENT);
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "unrecognized status value: '" + name + "'",
                child.getStartLine(),
                child.getStartColumn());
        }
        return node;
    }

    /**
     * Adds the reference string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpReferPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a MIB type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpSyntaxPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the units string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpUnitsPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the SNMP access as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpAccessPart(Production node)
        throws ParseException {

        Node    child;
        String  name;

        child = getChildAt(node, 0);
        if (child.getId() != Asn1Constants.ACCESS) {
            currentMib.setSmiVersion(2);
        }
        child = getChildAt(node, 1);
        name = getStringValue(child, 0);
        if (name.equals("read-only")) {
            node.addValue(SnmpAccess.READ_ONLY);
        } else if (name.equals("read-write")) {
            node.addValue(SnmpAccess.READ_WRITE);
        } else if (name.equals("read-create")) {
            node.addValue(SnmpAccess.READ_CREATE);
        } else if (name.equals("write-only")) {
            node.addValue(SnmpAccess.WRITE_ONLY);
        } else if (name.equals("not-implemented")) {
            node.addValue(SnmpAccess.NOT_IMPLEMENTED);
        } else if (name.equals("not-accessible")) {
            node.addValue(SnmpAccess.NOT_ACCESSIBLE);
        } else if (name.equals("accessible-for-notify")) {
            node.addValue(SnmpAccess.ACCESSIBLE_FOR_NOTIFY);
        } else {
            node.addValue(SnmpAccess.READ_WRITE);
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "unrecognized access value: '" + name + "'",
                child.getStartLine(),
                child.getStartColumn());
        }
        return node;
    }

    /**
     * Adds either a list of value or a single value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpIndexPart(Production node)
        throws ParseException {

        if (getChildAt(node, 0).getId() == Asn1Constants.INDEX) {
            node.addValue(getChildValues(node));
        } else {
            node.addValues(getChildValues(node));
        }
        return node;
    }

    /**
     * Adds the index MIB values as node values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitIndexValueList(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the index MIB value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitIndexValue(Production node) {
        SnmpIndex  index;
        Object     obj = getChildValues(node).get(0);

        switch (node.getChildAt(0).getId()) {
        case Asn1Constants.VALUE:
            index = new SnmpIndex(false, (MibValue) obj, null);
            break;
        case Asn1Constants.IMPLIED:
            index = new SnmpIndex(true, (MibValue) obj, null);
            break;
        default:
            index = new SnmpIndex(false, null, (MibType) obj);
        }
        node.addValue(index);
        return node;
    }

    /**
     * Adds the index MIB type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitIndexType(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the default MIB value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpDefValPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds a list of MIB values as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpObjectsPart(Production node) {
        node.addValue(getChildValues(node));
        return node;
    }

    /**
     * Adds the MIB values as node values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitValueList(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the enterprise MIB value as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpEnterprisePart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the variable MIB values as node values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpVarPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the display hint as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpDisplayPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the MIB values as node values.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpNotificationsPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds an SNMP module as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpModulePart(Production node)
        throws ParseException {

        SnmpModule module;
        String     name = null;
        ArrayList  groups = new ArrayList();
        ArrayList  modules = new ArrayList();
        String     comment = null;
        Node       child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.MODULE:
                comment = MibAnalyzerUtil.getComments(child);
                break;
            case Asn1Constants.SNMP_MODULE_IMPORT:
                name = getStringValue(child, 0);
                popContext();
                break;
            case Asn1Constants.SNMP_MANDATORY_PART:
                groups = (ArrayList) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_COMPLIANCE_PART:
                modules.add(getValue(child, 0));
                break;
            }
        }
        module = new SnmpModule(name, groups, modules);
        module.setComment(comment);
        node.addValue(module);
        return node;
    }

    /**
     * Adds the module name as a node value. This method also sets
     * current MIB context to the referenced module. The imports are
     * implicit, meaning that symbol names do not have to be listed
     * in order to be imported.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpModuleImport(Production node)
        throws ParseException {

        MibImport  imp;
        String     module;

        // Load referenced module
        module = getStringValue(getChildAt(node, 0), 0);
        loader.scheduleLoad(module);

        // Create module reference and context
        imp = new MibImport(loader, getLocation(node), module, null);
        currentMib.addImport(imp);
        pushContextExtension(imp);

        // Return results
        node.addValue(module);
        return node;
    }

    /**
     * Adds the list of group values as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpMandatoryPart(Production node) {
        node.addValue(getChildValues(node));
        return node;
    }

    /**
     * Adds an SNMP compliance object as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpCompliancePart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds an SNMP compliance object as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitComplianceGroup(Production node)
        throws ParseException {

        SnmpCompliance  comp;
        MibValue        value;
        String          desc;

        value = (MibValue) getValue(getChildAt(node, 1), 0);
        desc = getStringValue(getChildAt(node, 2), 0);
        comp = new SnmpCompliance(true, value, null, null, null, desc);
        comp.setComment(MibAnalyzerUtil.getComments(node));
        node.addValue(comp);
        return node;
    }

    /**
     * Adds an SNMP compliance object as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitComplianceObject(Production node)
        throws ParseException {

        SnmpCompliance  comp;
        MibValue        value = null;
        MibType         syntax = null;
        MibType         write = null;
        SnmpAccess      access = null;
        String          desc = null;
        Node            child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.VALUE:
                value = (MibValue) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_SYNTAX_PART:
                syntax = (MibType) getValue(child, 0);
                syntax.setComment(MibAnalyzerUtil.getComments(child));
                break;
            case Asn1Constants.SNMP_WRITE_SYNTAX_PART:
                write = (MibType) getValue(child, 0);
                write.setComment(MibAnalyzerUtil.getComments(child));
                break;
            case Asn1Constants.SNMP_ACCESS_PART:
                access = (SnmpAccess) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            }
        }
        comp = new SnmpCompliance(false, value, syntax, write, access, desc);
        comp.setComment(MibAnalyzerUtil.getComments(node));
        node.addValue(comp);
        return node;
    }

    /**
     * Adds the MIB type as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpWriteSyntaxPart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds the product release string as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpProductReleasePart(Production node) {
        node.addValues(getChildValues(node));
        return node;
    }

    /**
     * Adds an SNMP module support as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpModuleSupportPart(Production node)
        throws ParseException {

        String     module = null;
        ArrayList  groups = null;
        ArrayList  vars = new ArrayList();
        Node       child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_MODULE_IMPORT:
                module = getStringValue(child, 0);
                popContext();
                break;
            case Asn1Constants.VALUE_LIST:
                groups = child.getAllValues();
                break;
            case Asn1Constants.SNMP_VARIATION_PART:
                vars.add(getValue(child, 0));
                break;
            }
        }
        node.addValue(new SnmpModuleSupport(module, groups, vars));
        return node;
    }

    /**
     * Modifies the MIB context stack to make sure all references are
     * interpreted in the context of the symbol being modified.
     *
     * @param node           the parent node
     * @param child          the child node, or null
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected void childSnmpVariationPart(Production node, Node child)
        throws ParseException {

        MibType     type;
        MibContext  context;

        if (child.getId() == Asn1Constants.VALUE) {
            context = new MibTypeContext(getValue(child, 0));
            pushContextExtension(context);
        } else if (child.getId() == Asn1Constants.SNMP_SYNTAX_PART) {
            type = (MibType) getValue(child, 0);
            if (type instanceof MibContext) {
                pushContextExtension((MibContext) type);
            }
        }
        node.addChild(child);
    }

    /**
     * Adds an SNMP variation as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     *
     * @throws ParseException if the node analysis discovered errors
     */
    protected Node exitSnmpVariationPart(Production node)
        throws ParseException {

        MibValue    value = null;
        MibType     syntax = null;
        MibType     write = null;
        SnmpAccess  access = null;
        ArrayList   reqs = new ArrayList();
        MibValue    defVal = null;
        String      desc = null;
        Node        child;

        for (int i = 0; i < node.getChildCount(); i++) {
            child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.VALUE:
                value = (MibValue) getValue(child, 0);
                popContext();
                break;
            case Asn1Constants.SNMP_SYNTAX_PART:
                syntax = (MibType) getValue(child, 0);
                if (syntax instanceof MibContext) {
                    popContext();
                }
                syntax.setComment(MibAnalyzerUtil.getComments(child));
                break;
            case Asn1Constants.SNMP_WRITE_SYNTAX_PART:
                write = (MibType) getValue(child, 0);
                write.setComment(MibAnalyzerUtil.getComments(child));
                break;
            case Asn1Constants.SNMP_ACCESS_PART:
                access = (SnmpAccess) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_CREATION_PART:
                reqs = (ArrayList) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DEF_VAL_PART:
                defVal = (MibValue) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            }
        }
        node.addValue(new SnmpVariation(value,
                                        syntax,
                                        write,
                                        access,
                                        reqs,
                                        defVal,
                                        desc));
        return node;
    }

    /**
     * Adds a list of the MIB values as a node value.
     *
     * @param node           the node being exited
     *
     * @return the node to add to the parse tree
     */
    protected Node exitSnmpCreationPart(Production node) {
        node.addValue(getChildValues(node));
        return node;
    }


    /**
     * Returns the location of a specified node.
     *
     * @param node           the parse tree node
     *
     * @return the file location of the node
     */
    private FileLocation getLocation(Node node) {
        return new FileLocation(file,
                                node.getStartLine(),
                                node.getStartColumn());
    }

    /**
     * Returns the top context on the context stack.
     *
     * @return the top context on the context stack
     */
    private MibContext getContext() {
        return (MibContext) contextStack.get(contextStack.size() - 1);
    }

    /**
     * Adds a new context to the top of the context stack.
     *
     * @param context        the context to add
     */
    private void pushContext(MibContext context) {
        contextStack.add(context);
    }

    /**
     * Adds an extension to the current context to the top of the
     * context stack. A new compound context will be created by
     * appending the top context to the specified one.
     *
     * @param context        the context extension to add
     */
    private void pushContextExtension(MibContext context) {
        pushContext(new CompoundContext(context, getContext()));
    }

    /**
     * Removes the top context on the context stack.
     */
    private void popContext() {
        contextStack.remove(contextStack.size() - 1);
    }
}
