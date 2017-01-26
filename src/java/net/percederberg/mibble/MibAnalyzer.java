/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

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
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
class MibAnalyzer extends Asn1Analyzer {

    /**
     * The list of MIB modules found.
     */
    private ArrayList<Mib> mibs = new ArrayList<>();

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
    private ArrayList<MibContext> contextStack = new ArrayList<>();

    /**
     * The implicit tags flag.
     */
    private boolean implicitTags = true;

    /**
     * An internal hash set containing processed comment tokens. When
     * a comment string is processed method, the corresponding tokens
     * will be added to this set and not processed again.
     */
    private HashSet<Token> commentTokens = new HashSet<>();

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
        mibs = new ArrayList<>();
        currentMib = null;
        baseContext = null;
        contextStack.clear();
        implicitTags = true;
        commentTokens.clear();
    }

    /**
     * Returns the list of MIB modules found during analysis.
     *
     * @return a list of MIB modules
     */
    public ArrayList<Mib> getMibs() {
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
        String str = node.getImage();
        str = str.substring(1, str.length() - 2);
        if (str.length() == 0) {
            node.addValue(new Integer(0));
        } else if (str.length() < 32) {
            node.addValue(new Integer(Integer.parseInt(str, 2)));
        } else if (str.length() < 64) {
            node.addValue(new Long(Long.parseLong(str, 2)));
        } else {
            node.addValue(new BigInteger(str, 2));
        }
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
        String str = node.getImage();
        str = str.substring(1, str.length() - 2);
        if (str.length() == 0) {
            node.addValue(new Integer(0));
        } else if (str.length() < 8) {
            node.addValue(new Integer(Integer.parseInt(str, 16)));
        } else if (str.length() < 16) {
            node.addValue(new Long(Long.parseLong(str, 16)));
        } else {
            node.addValue(new BigInteger(str, 16));
        }
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
        String str = node.getImage();
        str = str.substring(1, str.length() - 1);
        if (str.indexOf("\"\"") >= 0) {
            str = str.replace("\"\"", "\"");
        }
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
        String str = node.getImage();
        if (str.length() < 10) {
            node.addValue(new Integer(str));
        } else if (str.length() < 19) {
            node.addValue(new Long(str));
        } else {
            node.addValue(new BigInteger(str));
        }
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
        String comment = MibAnalyzerUtil.getCommentsFooter(node, commentTokens);
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
        currentMib = new Mib(loader, log);
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

        MibFileRef fileRef = MibAnalyzerUtil.getFileRef(file, node);
        currentMib.setName(getStringValue(getChildAt(node, 0), 0));
        currentMib.setFileRef(fileRef);
        currentMib.setText(MibAnalyzerUtil.getText(node));
        String str = MibAnalyzerUtil.getComments(node, commentTokens);
        currentMib.setHeaderComment(str);
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

        Node child = getChildAt(node, 0);
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
        ArrayList<MibImport> imports = getChildValues(node);
        boolean importsSMI = false;
        boolean isSMIv2 = false;
        for (MibImport imp : imports) {
            String name = imp.getName();
            importsSMI |= name.equals("RFC1065-SMI") ||
                          name.equals("RFC1155-SMI") ||
                          name.equals("SNMPv2-SMI");
            isSMIv2 |= name.equals("SNMPv2-SMI") ||
                       name.equals("SNMPv2-TC") ||
                       name.equals("SNMPv2-CONF");
        }
        if (!importsSMI) {
            // Add a "hidden" SMI as the last import (without named symbols)
            // This will trigger warnings for each symbol found there.
            MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
            String module = isSMIv2 ? "SNMPv2-SMI" : "RFC1155-SMI";
            List<String> empty = Collections.<String> emptyList();
            MibImport imp = new MibImport(loader, ref, module, empty);
            loader.scheduleLoad(imp.getName());
            currentMib.addImport(imp);
            imports.add(imp);
        }
        if (isSMIv2) {
            currentMib.setSmiVersion(2);
        }
        MibContext current = loader.getDefaultContext();
        for (int i = imports.size() - 1; i >= 0; i--) {
            MibImport imp = imports.get(i);
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

        // Create MIB reference
        Node child = getChildAt(node, 0);
        List<String> symbols = child.getAllValues();
        if (symbols == null) {
            symbols = Collections.emptyList();
        }
        child = getChildAt(node, 2);
        String module = getStringValue(child, 0);
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node.getChildAt(2));
        MibImport imp = new MibImport(loader, ref, module, symbols);
        if (module.equals("RFC1065-SMI")) {
            log.addWarning(ref, "RFC1065-SMI is obsoleted by RFC1155-SMI");
        }

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

        // Check macro name
        String name = getStringValue(getChildAt(node, 0), 0);
        if (currentMib.getSymbol(name) != null) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "a symbol '" + name + "' already present in the MIB",
                node.getStartLine(),
                node.getStartColumn());
        }

        // Create macro symbol
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        MibMacroSymbol symbol = new MibMacroSymbol(ref, currentMib, name);
        symbol.setComment(MibAnalyzerUtil.getComments(node, commentTokens));

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

        // Check type name
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        String name = getStringValue(getChildAt(node, 0), 0);
        if (currentMib.getSymbol(name) != null) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "a symbol '" + name + "' already present in the MIB",
                node.getStartLine(),
                node.getStartColumn());
        }
        if (!Character.isUpperCase(name.charAt(0))) {
            log.addWarning(ref,
                           "type identifier '" + name + "' doesn't " +
                           "start with an uppercase character");
        }

        // Create type symbol
        MibType type = (MibType) getValue(getChildAt(node, 2), 0);
        MibTypeSymbol symbol = new MibTypeSymbol(ref, currentMib, name, type);
        symbol.setComment(MibAnalyzerUtil.getComments(node, commentTokens));

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

        MibContext local = getContext();
        String name = null;
        Object value = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
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
                break;
            default:
                // Ignored node
            }
        }
        MibFileRef fileRef = MibAnalyzerUtil.getFileRef(file, node);
        if (value instanceof Constraint) {
            value = new TypeReference(fileRef, local, name, (Constraint) value);
        } else if (value instanceof ArrayList<?>) {
            value = new TypeReference(fileRef, local, name, (ArrayList<?>) value);
        } else {
            value = new TypeReference(fileRef, local, name);
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
        ArrayList<?> values = getChildValues(node);
        if (values.size() == 0) {
            node.addValue(new IntegerType());
        } else {
            Object obj = values.get(0);
            if (obj instanceof ArrayList) {
                node.addValue(new IntegerType((ArrayList<?>) obj));
            } else {
                node.addValue(new IntegerType((Constraint) obj));
            }
        }
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
        ArrayList<Constraint> values = getChildValues(node);
        if (values.size() == 0) {
            node.addValue(new StringType());
        } else {
            node.addValue(new StringType(values.get(0)));
        }
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
        ArrayList<?> values = getChildValues(node);
        if (values.size() == 0) {
            node.addValue(new BitSetType());
        } else {
            Object obj = values.get(0);
            if (obj instanceof ArrayList) {
                node.addValue(new BitSetType((ArrayList<?>) obj));
            } else {
                node.addValue(new BitSetType((Constraint) obj));
            }
        }
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
        node.addValue(new SequenceType(getChildValues(node)));
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

        Node child = getChildAt(node, node.getChildCount() - 1);
        MibType type = (MibType) getValue(child, 0);
        Constraint  c = null;
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "SET type currently unsupported");
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "SET OF type currently unsupported");
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "ENUMERATED type currently unsupported");
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "selection type currently unsupported");
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

        boolean implicit = implicitTags;
        Node child = getChildAt(node, 0);
        MibTypeTag tag = (MibTypeTag) getValue(child, 0);
        child = getChildAt(node, 1);
        if (child.getId() == Asn1Constants.EXPLICIT_OR_IMPLICIT_TAG) {
            implicit = ((Boolean) getValue(child, 0)).booleanValue();
        }
        child = getChildAt(node, node.getChildCount() - 1);
        MibType type = (MibType) getValue(child, 0);
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
        int category = MibTypeTag.CONTEXT_SPECIFIC_CATEGORY;
        int value = 0;
        ArrayList<Number> values = getChildValues(node);
        if (values.size() == 1) {
            value = values.get(0).intValue();
        } else {
            category = values.get(0).intValue();
            value = values.get(1).intValue();
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
        Node child = getChildAt(node, 0);
        int category = MibTypeTag.CONTEXT_SPECIFIC_CATEGORY;
        if (child.getId() == Asn1Constants.UNIVERSAL) {
            category = MibTypeTag.UNIVERSAL_CATEGORY;
        } else if (child.getId() == Asn1Constants.APPLICATION) {
            category = MibTypeTag.APPLICATION_CATEGORY;
        } else if (child.getId() == Asn1Constants.PRIVATE) {
            category = MibTypeTag.PRIVATE_CATEGORY;
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

        Node child = getChildAt(node, 0);
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "ANY type currently unsupported");
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

        String name = null;
        Node child = getChildAt(node, 0);
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
        MibType type = new ElementType(name, (MibType) getValue(child, 0));
        type.setComment(MibAnalyzerUtil.getComments(node, commentTokens));
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "optional and default elements are currently unsupported");
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
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
            if (child.getId() == Asn1Constants.NAMED_NUMBER) {
                MibValueSymbol symbol = (MibValueSymbol) child.getValue(0);
                String str = MibAnalyzerUtil.getComments(child, commentTokens);
                symbol.setComment(str);
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

        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        String name = getStringValue(getChildAt(node, 0), 0);
        MibValue value = (MibValue) getValue(getChildAt(node, 2), 0);
        MibValueSymbol symbol = new MibValueSymbol(ref, null, name, null, value);
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
        Constraint result = null;
        ArrayList<Constraint> values = getChildValues(node);
        for (int i = values.size() - 1; i >= 0; i--) {
            Constraint c = values.get(i);
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

        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        ArrayList<?> list = getChildValues(node);
        if (list.size() == 0) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "no value specified in constraint",
                node.getStartLine(),
                node.getStartColumn());
        } else if (list.size() == 1) {
            MibValue val = (MibValue) list.get(0);
            node.addValue(new ValueConstraint(ref, val));
        } else {
            MibValue lower = null;
            MibValue upper = null;
            Boolean strictLower = null;
            Boolean strictUpper = null;
            for (Object obj : list) {
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
            node.addValue(new ValueRangeConstraint(ref,
                                                   lower,
                                                   strictLower.booleanValue(),
                                                   upper,
                                                   strictUpper.booleanValue()));
        }
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

        // Check for strict lower end point
        Node child = getChildAt(node, 0);
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

        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        Constraint c = (Constraint) getValue(getChildAt(node, 1), 0);
        node.addValue(new SizeConstraint(ref, c));
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "FROM constraints are currently unsupported");
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "INCLUDES constraints are currently unsupported");
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
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        log.addError(ref, "WITH COMPONENT(S) constraints are currently unsupported");
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

        // Check value name
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        String name = getStringValue(getChildAt(node, 0), 0);
        if (currentMib.getSymbol(name) != null) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "a symbol '" + name + "' already present in the MIB",
                node.getStartLine(),
                node.getStartColumn());
        }
        if (!Character.isLowerCase(name.charAt(0))) {
            log.addWarning(ref,
                           "value identifier '" + name + "' doesn't " +
                           "start with a lowercase character");
        }

        // Create value symbol
        MibType type = (MibType) getValue(getChildAt(node, 1), 0);
        MibValue value = (MibValue) getValue(getChildAt(node, 3), 0);
        MibValueSymbol symbol = new MibValueSymbol(ref, currentMib, name, type, value);
        symbol.setComment(MibAnalyzerUtil.getComments(node, commentTokens));

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

        // Check for module reference
        MibContext local = getContext();
        Node child = getChildAt(node, 0);
        if (child.getId() == Asn1Constants.MODULE_REFERENCE) {
            String name = getStringValue(child, 0);
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
        MibFileRef fileRef = MibAnalyzerUtil.getFileRef(file, node);
        String name = getStringValue(child, 0);
        ValueReference ref = new ValueReference(fileRef, local, name);
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

        Node child = getChildAt(node, 0);
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

        Number number = null;
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

        Number number = null;
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

        Node child = getChildAt(node, 0);
        Number number = (Number) child.getValue(0);
        String text = (String) child.getValue(1);
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

        Node child = getChildAt(node, 0);
        Number number = (Number) child.getValue(0);
        String text = (String) child.getValue(1);
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

        String str = getStringValue(getChildAt(node, 0), 0);
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
        BitSet bits = new BitSet();
        ArrayList<ValueReference> values = new ArrayList<>();
        ArrayList<NamedNumber> components = getChildValues(node);
        for (NamedNumber number : components) {
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

        // Check for minimum number of components
        ArrayList<NamedNumber> components = getChildValues(node);
        if (components.size() < 1) {
            throw new ParseException(
                ParseException.ANALYSIS_ERROR,
                "object identifier must contain at least one component",
                node.getStartLine(),
                node.getStartColumn());
        }

        // Analyze components
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        MibValue parent = null;
        for (NamedNumber number : components) {
            if (number.hasNumber()) {
                int value = number.getNumber().intValue();
                if (parent == null && value == 0) {
                    parent = new ValueReference(ref, getContext(), DefaultContext.CCITT);
                } else if (parent == null && value == 1) {
                    parent = new ValueReference(ref, getContext(), DefaultContext.ISO);
                } else if (parent == null && value == 2) {
                    parent = new ValueReference(ref, getContext(), DefaultContext.JOINT_ISO_CCITT);
                } else if (parent instanceof ObjectIdentifierValue) {
                    try {
                        parent = new ObjectIdentifierValue(
                                        ref,
                                        (ObjectIdentifierValue) parent,
                                        number.getName(),
                                        value);
                    } catch (MibException e) {
                        log.addError(e);
                        parent = null;
                    }
                } else {
                    parent = new ObjectIdentifierValue(
                                        ref,
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

        Object obj = getValue(getChildAt(node, 0), 0);
        if (obj instanceof Number) {
            node.addValue(new NamedNumber((Number) obj));
        } else if (obj instanceof String) {
            MibFileRef fileRef = MibAnalyzerUtil.getFileRef(file, node);
            ValueReference ref = new ValueReference(fileRef,
                                                    getContext(),
                                                    (String) obj);
            node.addValue(new NamedNumber((String) obj, ref));
        } else {
            node.addValue(obj);
        }
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

        String name = getStringValue(getChildAt(node, 0), 0);
        Object obj = getValue(getChildAt(node, 2), 0);
        if (obj instanceof Number) {
            node.addValue(new NamedNumber(name, (Number) obj));
        } else {
            node.addValue(new NamedNumber(name, (ValueReference) obj));
        }
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

        currentMib.setSmiVersion(2);
        String update = getStringValue(getChildAt(node, 1), 0);
        String org = getStringValue(getChildAt(node, 2), 0);
        String contact = getStringValue(getChildAt(node, 3), 0);
        String desc = getStringValue(getChildAt(node, 4), 0);
        ArrayList<SnmpRevision> revisions = new ArrayList<>();
        for (int i = 5; i < node.getChildCount(); i++) {
            revisions.add((SnmpRevision) getValue(getChildAt(node, i), 0));
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

        currentMib.setSmiVersion(2);
        SnmpStatus status = (SnmpStatus) getValue(getChildAt(node, 1), 0);
        String desc = getStringValue(getChildAt(node, 2), 0);
        String ref = null;
        if (node.getChildCount() > 3) {
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

        if (child.getId() == Asn1Constants.SNMP_SYNTAX_PART) {
            MibType type = (MibType) getValue(child, 0);
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
    @SuppressWarnings("unchecked")
    protected Node exitSnmpObjectTypeMacroType(Production node)
        throws ParseException {

        MibType         syntax = null;
        String          units = null;
        SnmpAccess      access = null;
        SnmpStatus      status = null;
        String          desc = null;
        String          ref = null;
        Object          index = null;
        MibValue        defVal = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_SYNTAX_PART:
                syntax = (MibType) getValue(child, 0);
                if (syntax instanceof MibContext) {
                    popContext();
                }
                syntax.setComment(MibAnalyzerUtil.getComments(child, commentTokens));
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
            default:
                // Ignore other nodes
            }
        }
        if (index instanceof ArrayList) {
            node.addValue(new SnmpObjectType(syntax,
                                             units,
                                             access,
                                             status,
                                             desc,
                                             ref,
                                             (ArrayList<SnmpIndex>) index,
                                             defVal));
        } else {
            node.addValue(new SnmpObjectType(syntax,
                                             units,
                                             access,
                                             status,
                                             desc,
                                             ref,
                                             (MibValue) index,
                                             defVal));
        }
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

        currentMib.setSmiVersion(2);
        ArrayList<MibValue> objects = new ArrayList<>();
        SnmpStatus status = null;
        String desc = null;
        String ref = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_OBJECTS_PART:
                objects = (ArrayList<MibValue>) getValue(child, 0);
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
            default:
                // Ignore other nodes
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

        MibValue enterprise = null;
        ArrayList<MibValue> vars = new ArrayList<>();
        String desc = null;
        String ref = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
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
            default:
                // Ignore other nodes
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

        currentMib.setSmiVersion(2);
        String display = null;
        SnmpStatus status = null;
        String desc = null;
        String ref = null;
        MibType syntax = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
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
                syntax.setComment(MibAnalyzerUtil.getComments(child, commentTokens));
                break;
            default:
                // Ignore other nodes
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

        currentMib.setSmiVersion(2);
        ArrayList<MibValue> objects = (ArrayList<MibValue>) getValue(getChildAt(node, 1), 0);
        SnmpStatus status = (SnmpStatus) getValue(getChildAt(node, 2), 0);
        String desc = getStringValue(getChildAt(node, 3), 0);
        String ref = null;
        if (node.getChildCount() > 4) {
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

        currentMib.setSmiVersion(2);
        ArrayList<MibValue> notifications = getChildAt(node, 1).getAllValues();
        SnmpStatus status = (SnmpStatus) getValue(getChildAt(node, 2), 0);
        String desc = getStringValue(getChildAt(node, 3), 0);
        String ref = null;
        if (node.getChildCount() > 4) {
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

        SnmpStatus status = null;
        String desc = null;
        String ref = null;
        ArrayList<SnmpModule> modules = new ArrayList<>();
        currentMib.setSmiVersion(2);
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
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
                modules.add((SnmpModule) getValue(child, 0));
                break;
            default:
                // Ignore other nodes
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

        String prod = null;
        SnmpStatus status = null;
        String desc = null;
        String ref = null;
        ArrayList<SnmpModuleSupport> modules = new ArrayList<>();
        currentMib.setSmiVersion(2);
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
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
                modules.add((SnmpModuleSupport) getValue(child, 0));
                break;
            default:
                // Ignore other nodes
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

        MibValue value = (MibValue) getValue(getChildAt(node, 1), 0);
        String desc = getStringValue(getChildAt(node, 3), 0);
        SnmpRevision rev = new SnmpRevision(value, desc);
        rev.setComment(MibAnalyzerUtil.getComments(node, commentTokens));
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

        Node child = getChildAt(node, 1);
        String name = getStringValue(child, 0);
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

        Node child = getChildAt(node, 0);
        if (child.getId() != Asn1Constants.ACCESS) {
            currentMib.setSmiVersion(2);
        }
        child = getChildAt(node, 1);
        String name = getStringValue(child, 0);
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
        Object obj = getChildValues(node).get(0);
        switch (node.getChildAt(0).getId()) {
        case Asn1Constants.VALUE:
            node.addValue(new SnmpIndex(false, (MibValue) obj, null));
            break;
        case Asn1Constants.IMPLIED:
            node.addValue(new SnmpIndex(true, (MibValue) obj, null));
            break;
        default:
            node.addValue(new SnmpIndex(false, null, (MibType) obj));
        }
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

        String name = null;
        ArrayList<MibValue> groups = new ArrayList<>();
        ArrayList<SnmpCompliance> compliances = new ArrayList<>();
        String comment = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.MODULE:
                comment = MibAnalyzerUtil.getComments(child, commentTokens);
                break;
            case Asn1Constants.SNMP_MODULE_IMPORT:
                name = getStringValue(child, 0);
                popContext();
                break;
            case Asn1Constants.SNMP_MANDATORY_PART:
                groups = (ArrayList<MibValue>) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_COMPLIANCE_PART:
                compliances.add((SnmpCompliance) getValue(child, 0));
                break;
            default:
                // Ignore other nodes
            }
        }
        SnmpModule module = new SnmpModule(name, groups, compliances);
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

        // Load referenced module
        String module = getStringValue(getChildAt(node, 0), 0);
        loader.scheduleLoad(module);

        // Create module reference and context
        MibFileRef ref = MibAnalyzerUtil.getFileRef(file, node);
        MibImport imp = new MibImport(loader, ref, module, null);
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

        MibValue value = (MibValue) getValue(getChildAt(node, 1), 0);
        String desc = getStringValue(getChildAt(node, 2), 0);
        SnmpCompliance comp = new SnmpCompliance(true, value, null, null, null, desc);
        comp.setComment(MibAnalyzerUtil.getComments(node, commentTokens));
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

        MibValue value = null;
        MibType syntax = null;
        MibType write = null;
        SnmpAccess access = null;
        String desc = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.VALUE:
                value = (MibValue) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_SYNTAX_PART:
                syntax = (MibType) getValue(child, 0);
                syntax.setComment(MibAnalyzerUtil.getComments(child, commentTokens));
                break;
            case Asn1Constants.SNMP_WRITE_SYNTAX_PART:
                write = (MibType) getValue(child, 0);
                write.setComment(MibAnalyzerUtil.getComments(child, commentTokens));
                break;
            case Asn1Constants.SNMP_ACCESS_PART:
                access = (SnmpAccess) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            default:
                // Ignore other nodes
            }
        }
        SnmpCompliance comp = new SnmpCompliance(false, value, syntax, write, access, desc);
        comp.setComment(MibAnalyzerUtil.getComments(node, commentTokens));
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

        String module = null;
        ArrayList<MibValue>  groups = null;
        ArrayList<SnmpVariation> vars = new ArrayList<>();
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
            switch (child.getId()) {
            case Asn1Constants.SNMP_MODULE_IMPORT:
                module = getStringValue(child, 0);
                popContext();
                break;
            case Asn1Constants.VALUE_LIST:
                groups = child.getAllValues();
                break;
            case Asn1Constants.SNMP_VARIATION_PART:
                vars.add((SnmpVariation) getValue(child, 0));
                break;
            default:
                // Ignore other nodes
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

        if (child.getId() == Asn1Constants.VALUE) {
            MibContext context = new MibTypeContext(getValue(child, 0));
            pushContextExtension(context);
        } else if (child.getId() == Asn1Constants.SNMP_SYNTAX_PART) {
            MibType type = (MibType) getValue(child, 0);
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

        MibValue value = null;
        MibType syntax = null;
        MibType write = null;
        SnmpAccess access = null;
        ArrayList<MibValue> reqs = new ArrayList<>();
        MibValue defVal = null;
        String desc = null;
        for (int i = 0; i < node.getChildCount(); i++) {
            Node child = node.getChildAt(i);
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
                syntax.setComment(MibAnalyzerUtil.getComments(child, commentTokens));
                break;
            case Asn1Constants.SNMP_WRITE_SYNTAX_PART:
                write = (MibType) getValue(child, 0);
                write.setComment(MibAnalyzerUtil.getComments(child, commentTokens));
                break;
            case Asn1Constants.SNMP_ACCESS_PART:
                access = (SnmpAccess) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_CREATION_PART:
                reqs = (ArrayList<MibValue>) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DEF_VAL_PART:
                defVal = (MibValue) getValue(child, 0);
                break;
            case Asn1Constants.SNMP_DESCR_PART:
                desc = getStringValue(child, 0);
                break;
            default:
                // Ignore other nodes
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
     * Returns the top context on the context stack.
     *
     * @return the top context on the context stack
     */
    private MibContext getContext() {
        return contextStack.get(contextStack.size() - 1);
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
