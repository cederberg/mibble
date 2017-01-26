/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2005-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
import net.percederberg.mibble.snmp.SnmpTextualConvention;
import net.percederberg.mibble.snmp.SnmpTrapType;
import net.percederberg.mibble.snmp.SnmpVariation;
import net.percederberg.mibble.type.BitSetType;
import net.percederberg.mibble.type.Constraint;
import net.percederberg.mibble.type.ElementType;
import net.percederberg.mibble.type.IntegerType;
import net.percederberg.mibble.type.SequenceOfType;
import net.percederberg.mibble.type.SequenceType;
import net.percederberg.mibble.type.StringType;
import net.percederberg.mibble.value.BitSetValue;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import net.percederberg.mibble.value.StringValue;

/**
 * A MIB output stream writer. This class contains a pretty printer
 * for a loaded MIB. All macros and data are printed in SMIv2 format,
 * and no translation from SMIv1 to SMIv2 takes place. The optional
 * SMIv1 backward compability flag may be set, which should allow
 * SMIv1 MIB:s to be printed correctly (but still without any
 * translation).
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.6
 */
public class MibWriter {

    /**
     * The underlying print writer to use.
     */
    private PrintWriter os;

    /**
     * The text margin. If this value is non-zero, all text will be
     * reformatted in an attempt to fit within the margin.
     */
    private int margin;

    /**
     * Creates a new MIB writer. When using this constructor, please
     * make sure that the output stream can handle text output.
     *
     * @param os             the underlying output stream to use
     */
    public MibWriter(OutputStream os) {
        this(new OutputStreamWriter(os));
    }

    /**
     * Creates a new MIB writer without any print margin.
     *
     * @param os             the underlying writer to use
     */
    public MibWriter(Writer os) {
        this(os, 0);
    }

    /**
     * Creates a new MIB writer. By specifying a non-zero print
     * margin, all comments and descriptions in the MIB may be
     * subject to reformatting if the output lines become too long.
     * For the best printing results, the source MIB file should be
     * edited manually instead of attempting to correct the print
     * margin automatically here.
     *
     * @param os             the underlying writer to use
     * @param margin         the print margin, or zero (0) for none
     */
    public MibWriter(Writer os, int margin) {
        if (os instanceof PrintWriter) {
            this.os = (PrintWriter) os;
        } else {
            this.os = new PrintWriter(os);
        }
        this.margin = margin;
    }

    /**
     * Closes the underlying output stream. No further print methods
     * in this class should be called after this.
     */
    public void close() {
        os.close();
    }

    /**
     * Prints the specified MIB.
     *
     * @param mib            the MIB to print
     */
    public void print(Mib mib) {
        printComment(mib.getHeaderComment(), "", true);
        if (mib.getHeaderComment() != null) {
            os.println();
        }
        os.print(mib.getName());
        os.println(" DEFINITIONS ::= BEGIN");
        os.println();
        List<MibImport> imps = mib.getAllImports();
        if (imps.size() > 0) {
            os.println("IMPORTS");
            Iterator<MibImport> iter = imps.iterator();
            while (iter.hasNext()) {
                printImport(iter.next());
                if (iter.hasNext()) {
                    os.println();
                }
            }
            os.println(";");
            os.println();
        }
        Iterator<MibSymbol> iter = mib.getAllSymbols().iterator();
        while (iter.hasNext()) {
            printSymbol(iter.next(), mib.getSmiVersion());
        }
        os.println("END");
        printComment(mib.getFooterComment(), "", true);
        os.flush();
    }

    /**
     * Prints a MIB comment string. This method will prefix each
     * non-blank line in the coment with the ASN.1 comment syntax.
     *
     * @param comment        the string to print
     * @param indent         the indentation to use
     * @param header         the header comment flag
     */
    private void printComment(String comment, String indent, boolean header) {
        if (comment != null) {
            if (header) {
                printIndent(indent + "-- ", comment);
                os.println();
            } else if (comment.indexOf("\n") >= 0) {
                int pos = comment.indexOf("\n");
                os.print(" -- ");
                os.print(comment.substring(0, pos));
                os.println();
                printIndent(indent + " -- ", comment.substring(pos + 1));
            } else {
                os.print(" -- ");
                os.print(comment);
            }
        }
    }

    /**
     * Prints a MIB import declaration.
     *
     * @param imp            the MIB import
     */
    private void printImport(MibImport imp) {
        Iterator<String> iter = imp.getAllSymbolNames().iterator();
        while (iter.hasNext()) {
            String str = iter.next().toString();
            int pos = 0;
            if (pos <= 0) {
                pos = str.length();
                os.print("    ");
            } else {
                pos = str.length();
                os.println(",");
                os.print("    ");
            }
            os.print(str);
        }
        os.println();
        os.print("        FROM ");
        os.print(imp.getName());
    }

    /**
     * Prints a MIB symbol declaration.
     *
     * @param sym            the MIB symbol
     * @param smiVersion     the SMI version to use
     */
    private void printSymbol(MibSymbol sym, int smiVersion) {
        printComment(sym.getComment(), "", true);
        if (sym instanceof MibTypeSymbol) {
            os.print(sym.getName());
            os.print(" ::= ");
            printType(((MibTypeSymbol) sym).getType(), "", smiVersion);
        } else if (sym instanceof MibValueSymbol) {
            os.print(sym.getName());
            os.print(" ");
            printType(((MibValueSymbol) sym).getType(), "", smiVersion);
            os.println();
            os.print("    ::= ");
            printValue(((MibValueSymbol) sym).getValue());
        } else {
            os.print("-- ");
            os.print(sym.getName());
            os.print(" MACRO ... not printed");
        }
        os.println();
        os.println();
    }

    /**
     * Prints a MIB type.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     * @param smiVersion     the SMI version to use
     */
    private void printType(MibType type, String indent, int smiVersion) {
        if (type.getReferenceSymbol() != null) {
            os.print(type.getReferenceSymbol().getName());
            MibType refType = type.getReferenceSymbol().getType();
            Constraint refCons = getConstraint(refType);
            Constraint typeCons = getConstraint(type);
            if (typeCons != null && typeCons != refCons) {
                printConstraint(type, indent);
            }
        } else if (type instanceof SequenceType) {
            SequenceType seqType = (SequenceType) type;
            os.println("SEQUENCE {");
            printTypeElements(seqType.getAllElements(),
                              indent + "    ",
                              smiVersion);
            os.println();
            os.print(indent);
            os.print("}");
        } else if (type instanceof SequenceOfType) {
            SequenceOfType seqOfType = (SequenceOfType) type;
            os.print("SEQUENCE ");
            if (seqOfType.getConstraint() != null) {
                 os.print("(");
                 os.print(seqOfType.getConstraint());
                 os.print(") ");
            }
            os.print("OF ");
            printType(seqOfType.getElementType(), indent, smiVersion);
        } else if (type instanceof IntegerType) {
            os.print("INTEGER");
            printConstraint(type, indent);
        } else if (type instanceof BitSetType) {
            os.print("BITS");
            printConstraint(type, indent);
        } else if (type instanceof StringType) {
            os.print("OCTET STRING");
            printConstraint(type, indent);
        } else if (type.isPrimitive()) {
            os.print(type.getName());
        } else if (type instanceof SnmpModuleIdentity) {
            printType((SnmpModuleIdentity) type, indent);
        } else if (type instanceof SnmpObjectIdentity) {
            printType((SnmpObjectIdentity) type, indent);
        } else if (type instanceof SnmpObjectType) {
            printType((SnmpObjectType) type, indent, smiVersion);
        } else if (type instanceof SnmpNotificationType) {
            printType((SnmpNotificationType) type, indent);
        } else if (type instanceof SnmpTrapType) {
            printType((SnmpTrapType) type, indent);
        } else if (type instanceof SnmpTextualConvention) {
            printType((SnmpTextualConvention) type, indent, smiVersion);
        } else if (type instanceof SnmpObjectGroup) {
            printType((SnmpObjectGroup) type, indent);
        } else if (type instanceof SnmpNotificationGroup) {
            printType((SnmpNotificationGroup) type, indent);
        } else if (type instanceof SnmpModuleCompliance) {
            printType((SnmpModuleCompliance) type, indent, smiVersion);
        } else if (type instanceof SnmpAgentCapabilities) {
            printType((SnmpAgentCapabilities) type, indent, smiVersion);
        } else {
            os.print("-- ERROR: type definition unknown");
        }
        printComment(type.getComment(), indent, false);
    }

    /**
     * Prints an SNMP module identity.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpModuleIdentity type, String indent) {
        os.println("MODULE-IDENTITY");
        os.print("    LAST-UPDATED    ");
        os.println(getQuote(type.getLastUpdated()));
        os.print("    ORGANIZATION    ");
        os.println(getQuote(type.getOrganization()));
        os.println("    CONTACT-INFO");
        printIndent("            ", getQuote(type.getContactInfo()));
        os.println();
        printDescription(type.getDescription());
        ArrayList<SnmpRevision> list = type.getRevisions();
        for (SnmpRevision rev : list) {
            os.println();
            if (rev.getComment() != null) {
                os.println();
                printComment(rev.getComment(), "    ", true);
            }
            os.print("    REVISION        ");
            printValue(rev.getValue());
            os.println();
            os.println("    DESCRIPTION");
            printIndent("            ", getQuote(rev.getDescription()));
        }
    }

    /**
     * Prints an SNMP object identity.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpObjectIdentity type, String indent) {
        os.println("OBJECT-IDENTITY");
        os.print("    STATUS          ");
        os.println(type.getStatus());
        printDescription(type.getDescription());
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
    }

    /**
     * Prints an SNMP object type.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     * @param smiVersion     the SMI version to use
     */
    private void printType(SnmpObjectType type,
                           String indent,
                           int smiVersion) {

        os.println("OBJECT-TYPE");
        os.print("    SYNTAX          ");
        printType(type.getSyntax(), "                    ", smiVersion);
        os.println();
        if (type.getUnits() != null) {
            os.print("    UNITS           ");
            os.print(getQuote(type.getUnits()));
            os.println();
        }
        if (smiVersion == 1) {
            os.print("    ACCESS          ");
        } else {
            os.print("    MAX-ACCESS      ");
        }
        os.println(type.getAccess());
        os.print("    STATUS          ");
        os.print(type.getStatus());
        if (type.getDescription() != null) {
            os.println();
            printDescription(type.getDescription());
        }
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
        if (type.getIndex() != null && type.getIndex().size() > 0) {
            os.println();
            os.print("    INDEX           ");
            printReferenceList(type.getIndex(), "                    ");
        }
        if (type.getAugments() != null) {
            os.println();
            os.print("    AUGMENTS        ");
            printReference(type.getAugments());
        }
        if (type.getDefaultValue() != null) {
            os.println();
            os.print("    DEFVAL          ");
            printReference(type.getDefaultValue(),
                           getSymbols(type.getSyntax()));
        }
    }

    /**
     * Prints an SNMP notification type.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpNotificationType type, String indent) {
        os.println("NOTIFICATION-TYPE");
        if (type.getObjects().size() > 0) {
            os.print("    OBJECTS         ");
            printReferenceList(type.getObjects(), "                    ");
            os.println();
        }
        os.print("    STATUS          ");
        os.println(type.getStatus());
        printDescription(type.getDescription());
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
    }

    /**
     * Prints an SNMP trap type.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpTrapType type, String indent) {
        os.println("TRAP-TYPE");
        os.print("    ENTERPRISE      ");
        printReferenceEntry(type.getEnterprise());
        if (type.getVariables().size() > 0) {
            os.println();
            os.print("    VARIABLES       ");
            printReferenceList(type.getVariables(), "                    ");
        }
        if (type.getDescription() != null) {
            os.println();
            printDescription(type.getDescription());
        }
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
    }

    /**
     * Prints an SNMP textual convention.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     * @param smiVersion     the SMI version to use
     */
    private void printType(SnmpTextualConvention type,
                           String indent,
                           int smiVersion) {

        os.println("TEXTUAL-CONVENTION");
        if (type.getDisplayHint() != null) {
            os.print("    DISPLAY-HINT    ");
            os.print(getQuote(type.getDisplayHint()));
            os.println();
        }
        os.print("    STATUS          ");
        os.println(type.getStatus());
        printDescription(type.getDescription());
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
        os.println();
        os.print("    SYNTAX          ");
        printType(type.getSyntax(), "                    ", smiVersion);
    }

    /**
     * Prints an SNMP object group.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpObjectGroup type, String indent) {
        os.println("OBJECT-GROUP");
        os.print("    OBJECTS         ");
        printReferenceList(type.getObjects(), "                    ");
        os.println();
        os.print("    STATUS          ");
        os.println(type.getStatus());
        printDescription(type.getDescription());
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
    }

    /**
     * Prints an SNMP object group.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpNotificationGroup type, String indent) {
        os.println("NOTIFICATION-GROUP");
        os.print("    NOTIFICATIONS   ");
        printReferenceList(type.getNotifications(), "                    ");
        os.println();
        os.print("    STATUS          ");
        os.println(type.getStatus());
        printDescription(type.getDescription());
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
    }

    /**
     * Prints an SNMP module compliance.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     * @param smiVersion     the SMI version to use
     */
    private void printType(SnmpModuleCompliance type,
                           String indent,
                           int smiVersion) {

        os.println("MODULE-COMPLIANCE");
        os.print("    STATUS          ");
        os.println(type.getStatus());
        printDescription(type.getDescription());
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
        for (SnmpModule module : type.getModules()) {
            os.println();
            printComment(module.getComment(), "    ", true);
            os.print("    MODULE          ");
            if (module.getModule() == null) {
                os.print("-- this module");
            } else {
                os.print(module.getModule());
            }
            if (module.getGroups().size() > 0) {
                os.println();
                os.print("    MANDATORY-GROUPS ");
                printReferenceList(module.getGroups(),
                                   "                    ");
            }
            for (SnmpCompliance comp : module.getCompliances()) {
                os.println();
                os.println();
                printModuleCompliance(comp, smiVersion);
            }
        }
    }

    /**
     * Prints an SNMP agent capabilities.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     * @param smiVersion     the SMI version to use
     */
    private void printType(SnmpAgentCapabilities type,
                           String indent,
                           int smiVersion) {

        os.println("AGENT-CAPABILITIES");
        os.print("    PRODUCT-RELEASE ");
        os.println(getQuote(type.getProductRelease()));
        os.print("    STATUS          ");
        os.println(type.getStatus());
        printDescription(type.getDescription());
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
        for (SnmpModuleSupport module : type.getModules()) {
            os.println();
            os.print("    SUPPORTS        ");
            os.println(module.getModule());
            os.print("    INCLUDES        ");
            printReferenceList(module.getGroups(),
                               "                    ");
            for (SnmpVariation var : module.getVariations()) {
                os.println();
                os.println();
                printVariation(var, smiVersion);
            }
        }
    }

    /**
     * Prints an SNMP module compliance statement.
     *
     * @param comp           the module compliance statement
     * @param smiVersion     the SMI version to use
     */
    private void printModuleCompliance(SnmpCompliance comp, int smiVersion) {
        printComment(comp.getComment(), "    ", true);
        if (comp.isGroup()) {
            os.print("    GROUP           ");
            printReferenceEntry(comp.getValue());
            os.println();
        } else {
            os.print("    OBJECT          ");
            printReferenceEntry(comp.getValue());
            os.println();
            if (comp.getSyntax() != null) {
                os.print("    SYNTAX          ");
                printType(comp.getSyntax(),
                          "                    ",
                          smiVersion);
                os.println();
            }
            if (comp.getWriteSyntax() != null) {
                os.print("    WRITE-SYNTAX    ");
                printType(comp.getWriteSyntax(),
                          "                    ",
                          smiVersion);
                os.println();
            }
            if (comp.getAccess() != null) {
                os.print("    MIN-ACCESS      ");
                os.println(comp.getAccess());
            }
        }
        printDescription(comp.getDescription());
    }

    /**
     * Prints an SNMP variation statement.
     *
     * @param var            the variation statement
     * @param smiVersion     the SMI version to use
     */
    private void printVariation(SnmpVariation var, int smiVersion) {
        os.print("    VARIATION       ");
        printReferenceEntry(var.getValue());
        os.println();
        if (var.getSyntax() != null) {
            os.print("    SYNTAX          ");
            printType(var.getSyntax(),
                      "                    ",
                      smiVersion);
            os.println();
        }
        if (var.getWriteSyntax() != null) {
            os.print("    WRITE-SYNTAX    ");
            printType(var.getWriteSyntax(),
                      "                    ",
                      smiVersion);
            os.println();
        }
        if (var.getAccess() != null) {
            os.print("    ACCESS          ");
            os.println(var.getAccess());
        }
        if (var.getRequiredCells().size() > 0) {
            os.print("    CREATION-REQUIRES ");
            printReferenceList(var.getRequiredCells(),
                               "                     ");
            os.println();
        }
        if (var.getDefaultValue() != null) {
            os.print("    DEFVAL          ");
            printReference(var.getDefaultValue(),
                           getSymbols(var.getBaseSymbol()));
            os.println();
        }
        printDescription(var.getDescription());
    }

    /**
     * Prints an SNMP description.
     *
     * @param descr          the description to print
     */
    private void printDescription(String descr) {
        if (descr.length() < 50 && descr.indexOf("\n") < 0) {
            os.print("    DESCRIPTION     ");
            os.print(getQuote(descr));
        } else {
            os.println("    DESCRIPTION");
            printIndent("            ", getQuote(descr));
        }
    }

    /**
     * Prints an array of MIB type elements.
     *
     * @param elems          the type elements to print
     * @param indent         the indentation to use on new lines
     * @param smiVersion     the SMI version to use
     */
    private void printTypeElements(ElementType[] elems,
                                   String indent,
                                   int smiVersion) {

        int column = 20;
        for (ElementType elem : elems) {
            if (elem.getName().length() + 2 > column) {
                column = elem.getName().length() + 2;
            }
        }
        String typeIndent = indent;
        for (int i = 0; i < column; i++) {
            typeIndent += " ";
        }
        for (int i = 0; i < elems.length; i++) {
            if (i > 0) {
                os.println(",");
            }
            printComment(elems[i].getComment(), indent, true);
            os.print(indent);
            os.print(elems[i].getName());
            for (int j = elems[i].getName().length(); j < column; j++) {
                os.print(" ");
            }
            printType(elems[i].getType(), typeIndent, smiVersion);
        }
    }

    /**
     * Prints a type constraint declaration. If the type doesn't have
     * any constraints, nothing will be printed.
     *
     * @param type           the MIB type
     * @param indent         the indentation to use
     */
    private void printConstraint(MibType type, String indent) {
        if (type instanceof IntegerType) {
            IntegerType intType = (IntegerType) type;
            if (intType.hasSymbols()) {
                os.println(" {");
                printEnumeration(intType.getAllSymbols(),
                                 indent + "    ");
                os.println();
                os.print(indent);
                os.print("}");
            } else if (intType.hasConstraint()) {
                os.print(" (");
                os.print(intType.getConstraint());
                os.print(")");
            }
        } else if (type instanceof BitSetType) {
            BitSetType bitType = (BitSetType) type;
            if (bitType.hasSymbols()) {
                os.println(" {");
                printEnumeration(bitType.getAllSymbols(),
                                 indent + "    ");
                os.println();
                os.print(indent);
                os.print("}");
            } else if (bitType.hasConstraint()) {
                os.print(" (");
                os.print(bitType.getConstraint());
                os.print(")");
            }
        } else if (type instanceof StringType) {
            StringType strType = (StringType) type;
            if (strType.hasConstraint()) {
                os.print(" (");
                os.print(strType.getConstraint());
                os.print(")");
            }
        }
    }

    /**
     * Prints a MIB type enumeration.
     *
     * @param symbols        the value symbols to print
     * @param indent         the indentation to use on new lines
     */
    private void printEnumeration(MibValueSymbol[] symbols, String indent) {
        for (int i = 0; i < symbols.length; i++) {
            if (i > 0) {
                os.println(",");
            }
            printComment(symbols[i].getComment(), indent, true);
            os.print(indent);
            os.print(symbols[i].getName());
            os.print("(");
            os.print(symbols[i].getValue());
            os.print(")");
        }
    }

    /**
     * Prints a MIB value.
     *
     * @param value          the value to print
     */
    private void printValue(MibValue value) {
        if (value instanceof ObjectIdentifierValue) {
            os.print("{ ");
            os.print(((ObjectIdentifierValue) value).toAsn1String());
            os.print(" }");
        } else if (value instanceof StringValue) {
            os.print(getQuote(value.toString()));
        } else {
            os.print(value.toString());
        }
    }

    /**
     * Prints a reference to a type or value object.
     *
     * @param obj            the type or value object
     */
    private void printReference(Object obj) {
        os.print("{ ");
        printReferenceEntry(obj);
        os.print(" }");
    }

    /**
     * Prints a reference to a type or value object. If the object
     * value is present in the list of enumerated values, the value
     * name will be printed instead.
     *
     * @param obj            the type or value object
     * @param values         the enumerated value definitions
     */
    private void printReference(Object obj, MibValueSymbol[] values) {
        if (obj instanceof BitSetValue) {
            obj = ((BitSetValue) obj).toAsn1String(values);
        } else if (values != null) {
            for (MibValueSymbol val : values) {
                if (val.getValue().equals(obj)) {
                    printReference(val.getName());
                    return;
                }
            }
        }
        printReference(obj);
    }

    /**
     * Prints a list of references to type or value objects. This
     * method is useful for printing SNMP index or object parts.
     *
     * @param list           the list of type or value objects
     * @param indent         the indentation to use
     */
    private void printReferenceList(ArrayList<?> list, String indent) {
        if (list.size() == 1) {
            printReference(list.get(0));
        } else {
            os.print("{");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    os.print(",");
                }
                os.println();
                os.print(indent);
                os.print("    ");
                printReferenceEntry(list.get(i));
            }
            os.println();
            os.print(indent);
            os.print("}");
        }
    }

    /**
     * Prints a reference to a type or value object. This method will
     * not print the encapsulating braces.
     *
     * @param obj            the type or value object
     */
    private void printReferenceEntry(Object obj) {
        if (obj instanceof SnmpIndex) {
            if (((SnmpIndex) obj).isImplied()) {
                os.print("IMPLIED ");
            }
            printReferenceEntry(((SnmpIndex) obj).getTypeOrValue());
        } else if (obj instanceof ObjectIdentifierValue) {
            ObjectIdentifierValue oid = (ObjectIdentifierValue) obj;
            if (oid.getSymbol() != null) {
                os.print(oid.getSymbol().getName());
            } else {
                os.print(oid.toAsn1String());
            }
        } else if (obj instanceof StringValue) {
            os.print(getQuote(obj.toString()));
        } else {
            os.print(obj.toString());
        }
    }

    /**
     * Prints an indented string. This method will indent each non-
     * blank line in the string with the specified indentation. If a
     * print margin is set, it will also attempt to reflow the string
     * to print so that it fits within the margin.
     *
     * @param indent         the indentation string
     * @param str            the string to print
     */
    private void printIndent(String indent, String str) {
        if (margin > 0) {
            str = reflow(str, margin - indent.length());
        }
        int pos = -1;
        while (str != null && (pos = str.indexOf('\n')) >= 0) {
            if (pos == 0) {
                os.println();
            } else {
                os.print(indent);
                os.println(str.substring(0, pos));
            }
            str = str.substring(pos + 1);
        }
        if (str != null && str.length() > 0) {
            os.print(indent);
            os.print(str);
        }
    }

    /**
     * Reformats all the linebreaks in a string. This method will
     * attempt to break too long lines into several lines, reflowing
     * any following text in the same paragraph with new line breaks.
     * It should not modify single lines that are shorter than the
     * maximum line length, unless a previous line was too long.
     *
     * @param str            the input string
     * @param maxLen         the maximum string length
     *
     * @return the reformatted string
     */
    private String reflow(String str, int maxLen) {
        StringBuilder src = new StringBuilder(str);
        StringBuilder res = new StringBuilder();
        while (src.length() > 0) {
            int pos = src.indexOf("\n");
            boolean fillNext = false;
            if (fillNext && pos > 0) {
                fillNext = false;
                src.setCharAt(pos, ' ');
                pos = src.indexOf("\n");
            }
            if (pos == 0) {
                fillNext = false;
                res.append("\n");
            } else {
                if (pos < 0) {
                    pos = src.length();
                }
                while (pos > maxLen) {
                    int temp = src.lastIndexOf(" ", pos - 1);
                    if (temp < 0) {
                        break;
                    }
                    pos = temp;
                    fillNext = true;
                }
                res.append(src.substring(0, pos));
                res.append("\n");
            }
            if (pos + 1 >= src.length()) {
                src.setLength(0);
            } else {
                src.delete(0, pos + 1);
            }
        }
        return res.toString();
    }

    /**
     * Returns a MIB type constraint. If the type didn't have any
     * constraint, null will be returned.
     *
     * @param type           the MIB type
     *
     * @return the MIB type constraint, or
     *         null if no constraint was set
     */
    private Constraint getConstraint(MibType type) {
        if (type instanceof IntegerType) {
            return ((IntegerType) type).getConstraint();
        } else if (type instanceof BitSetType) {
            return ((BitSetType) type).getConstraint();
        } else if (type instanceof StringType) {
            return ((StringType) type).getConstraint();
        } else if (type instanceof SnmpTextualConvention) {
            return getConstraint(((SnmpTextualConvention) type).getSyntax());
        } else {
            return null;
        }
    }

    /**
     * Returns all enumeration values for a MIB object. If the object
     * didn't have any enumerated values, null will be returned.
     *
     * @param obj            the MIB type or type reference
     *
     * @return the MIB enumeration value symbols, or
     *         null if no symbols were set
     */
    private MibValueSymbol[] getSymbols(Object obj) {
        if (obj instanceof IntegerType) {
            return ((IntegerType) obj).getAllSymbols();
        } else if (obj instanceof BitSetType) {
            return ((BitSetType) obj).getAllSymbols();
        } else if (obj instanceof SnmpTextualConvention) {
            return getSymbols(((SnmpTextualConvention) obj).getSyntax());
        } else if (obj instanceof SnmpObjectType) {
            return getSymbols(((SnmpObjectType) obj).getSyntax());
        } else if (obj instanceof MibValueSymbol) {
            return getSymbols(((MibValueSymbol) obj).getType());
        } else {
            return null;
        }
    }

    /**
     * Returns a correctly ASN.1 quoted version of a string.
     *
     * @param str            the string to quote
     *
     * @return a correct ASN.1 string syntax
     */
    private String getQuote(String str) {
        StringBuilder buffer = new StringBuilder();
        buffer.append('"');
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '"') {
                buffer.append("\"\"");
            } else {
                buffer.append(str.charAt(i));
            }
        }
        buffer.append('"');
        return buffer.toString();
    }
}
