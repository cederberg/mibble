/*
 * MibWriter.java
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
 * Copyright (c) 2005 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import net.percederberg.mibble.snmp.SnmpModuleIdentity;
import net.percederberg.mibble.snmp.SnmpObjectIdentity;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpRevision;
import net.percederberg.mibble.snmp.SnmpTextualConvention;
import net.percederberg.mibble.type.BitSetType;
import net.percederberg.mibble.type.ElementType;
import net.percederberg.mibble.type.IntegerType;
import net.percederberg.mibble.type.SequenceOfType;
import net.percederberg.mibble.type.SequenceType;
import net.percederberg.mibble.type.StringType;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import net.percederberg.mibble.value.StringValue;

/**
 * A MIB output stream writer. This class contains a pretty printer
 * for a loaded MIB.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.6
 * @since    2.6
 */
public class MibWriter {

    /**
     * The underlying print writer to use.
     */
    private PrintWriter os;

    /**
     * The print margin.
     */
    private int margin;

    /**
     * Creates a new MIB writer. When using this constructor, please
     * take care to make sure that the output stream expects text
     * output.
     *
     * @param os             the underlying output stream to use
     */
    public MibWriter(OutputStream os) {
        this(new OutputStreamWriter(os));
    }

    /**
     * Creates a new MIB writer.
     *
     * @param os             the underlying writer to use
     */
    public MibWriter(Writer os) {
        this(os, 72);
    }

    /**
     * Creates a new MIB writer.
     *
     * @param os             the underlying writer to use
     * @param margin         the print margin
     */
    public MibWriter(Writer os, int margin) {
        if (os instanceof PrintWriter) {
            this.os = (PrintWriter) os;
        } else {
            this.os = new PrintWriter(os);
        }
        if (margin > 0) {
            this.margin = margin;
        } else {
            this.margin = 72;
        }
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
        Collection  coll;
        Iterator    iter;

        printComment(mib.getHeaderComment());
        if (mib.getHeaderComment() != null) {
            os.println();
        }
        os.print(mib.getName());
        os.println(" DEFINITIONS ::= BEGIN");
        os.println();
        coll = mib.getAllImports();
        if (coll.size() > 0) {
            os.println("IMPORTS");
            iter = coll.iterator();
            while (iter.hasNext()) {
                printImport((MibImport) iter.next());
                if (iter.hasNext()) {
                    os.println();
                }
            }
            os.println(";");
            os.println();
        }
        iter = mib.getAllSymbols().iterator();
        while (iter.hasNext()) {
            printSymbol((MibSymbol) iter.next());
        }
        os.println("END");
        printComment(mib.getFooterComment());
        os.flush();
    }

    /**
     * Prints a MIB comment string. This method will prefix each
     * non-blank line in the coment with the ASN.1 comment syntax.
     *
     * @param str            the string to print
     */
    private void printComment(String str) {
        if (str != null) {
            printIndent("-- ", str);
            os.println();
        }
    }

    /**
     * Prints a MIB import declaration.
     *
     * @param imp            the MIB import
     */
    private void printImport(MibImport imp) {
        Iterator  iter;
        String    str;
        int       pos = 0;

        iter = imp.getAllSymbolNames().iterator();
        while (iter.hasNext()) {
            str = iter.next().toString();
            if (pos <= 0) {
                pos = str.length();
                os.print("    ");
/* TODO: remove comment to use fill-to-margin instead
            } else if (pos + 7 + str.length() < margin) {
                pos += 2 + str.length();
                os.print(", ");
*/
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
     */
    private void printSymbol(MibSymbol sym) {
        printComment(sym.getComment());
        if (sym instanceof MibTypeSymbol) {
            os.print(sym.getName());
            os.print(" ::= ");
            printType(((MibTypeSymbol) sym).getType(), "");
        } else if (sym instanceof MibValueSymbol) {
            os.print(sym.getName());
            os.print(" ");
            printType(((MibValueSymbol) sym).getType(), "");
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
     */
    private void printType(MibType type, String indent) {
        SequenceType    seqType;
        SequenceOfType  seqOfType;
        IntegerType     intType;
        BitSetType      bitType;
        StringType      strType;

        if (type.getReferenceSymbol() != null) {
            // TODO: handle constraints?
            os.print(type.getReferenceSymbol().getName());
        } else if (type instanceof SequenceType) {            
            seqType = (SequenceType) type;
            os.println("SEQUENCE {");
            os.print(indent + "    ");
            printTypeElements(seqType.getAllElements(), indent + "    ");
            os.println();
            os.print(indent);
            os.print("}");
        } else if (type instanceof SequenceOfType) {            
            seqOfType = (SequenceOfType) type;
            os.print("SEQUENCE ");
            if (seqOfType.getConstraint() != null) {
                 os.print("(");
                 os.print(seqOfType.getConstraint());
                 os.print(") ");
            }
            os.print("OF ");
            printType(seqOfType.getElementType(), indent);
        } else if (type instanceof IntegerType) {
            intType = (IntegerType) type;
            os.print("INTEGER");
            if (intType.hasSymbols()) {
                os.println(" {");
                os.print(indent + "    ");
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
            bitType = (BitSetType) type;
            os.print("BITS");
            if (bitType.hasSymbols()) {
                os.println(" {");
                os.print(indent + "    ");
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
            strType = (StringType) type;
            os.print("OCTET STRING");
            if (strType.hasConstraint()) {
                os.print(" (");
                os.print(strType.getConstraint());
                os.print(")");
            }
        } else if (type.isPrimitive()) {
            os.print(type.getName());
        } else if (type instanceof SnmpModuleIdentity) {
            printType((SnmpModuleIdentity) type, indent);
        } else if (type instanceof SnmpObjectIdentity) {
            printType((SnmpObjectIdentity) type, indent);
        } else if (type instanceof SnmpObjectType) {
            printType((SnmpObjectType) type, indent);
// TODO: NOTIFICATION-TYPE
// TODO: TRAP-TYPE ??
        } else if (type instanceof SnmpTextualConvention) {
            printType((SnmpTextualConvention) type, indent);
// TODO: OBJECT-GROUP
// TODO: NOTIFICATION-GROUP
// TODO: MODULE-COMPLIANCE
// TODO: AGENT-CAPABILITIES
        } else {
            // TODO: print error on unhandled types
            os.print("-- TODO: currently unhandled type");
        }
    }

    /**
     * Prints an SNMP module identity.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpModuleIdentity type, String indent) {
        ArrayList     list;
        SnmpRevision  rev;

        os.println("MODULE-IDENTITY");
        os.print("    LAST-UPDATED    ");
        os.println(getQuote(type.getLastUpdated()));
        os.print("    ORGANIZATION    ");
        os.println(getQuote(type.getOrganization()));
        os.println("    CONTACT-INFO");
        printIndent("            ", getQuote(type.getContactInfo()));
        os.println();
        os.println("    DESCRIPTION");
        printIndent("            ", getQuote(type.getDescription()));
        list = type.getRevisions();
        for (int i = 0; i < list.size(); i++) {
            rev = (SnmpRevision) list.get(i);
            os.println();
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
        os.println("    DESCRIPTION");
        printIndent("            ", getQuote(type.getDescription()));
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
     */
    private void printType(SnmpObjectType type, String indent) {
        // TODO: translate SMIv1 access and status to SMIv2?
        os.println("OBJECT-TYPE");
        os.print("    SYNTAX          ");
        printType(type.getSyntax(), "                    ");
        os.println();
        if (type.getUnits() != null) {
            os.print("    UNITS           ");
            os.print(getQuote(type.getUnits()));
            os.println();
        }
        os.print("    MAX-ACCESS      ");
        os.println(type.getAccess());
        os.print("    STATUS          ");
        os.print(type.getStatus());
        if (type.getDescription() != null) {
            os.println();
            os.println("    DESCRIPTION");
            printIndent("            ", getQuote(type.getDescription()));
        }
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
        if (type.getIndex() != null && type.getIndex().size() > 0) {
            os.println();
            os.print("    INDEX           { ");
            for (int i = 0; i < type.getIndex().size(); i++) {
                if (i > 0) {
                    os.print(", ");
                }
                printTypeOrValue(type.getIndex().get(i));
            }
            os.print(" }");
        }
        if (type.getDefaultValue() != null) {
            os.println();
            os.print("    DEFVAL          ");
            printValue(type.getDefaultValue());
        }
    }

    /**
     * Prints an SNMP textual convention.
     *
     * @param type           the type to print
     * @param indent         the indentation to use on new lines
     */
    private void printType(SnmpTextualConvention type, String indent) {
        os.println("TEXTUAL-CONVENTION");
        if (type.getDisplayHint() != null) {
            os.print("    DISPLAY-HINT    ");
            os.print(getQuote(type.getDisplayHint()));
            os.println();
        }
        os.print("    STATUS          ");
        os.println(type.getStatus());
        os.println("    DESCRIPTION");
        printIndent("            ", getQuote(type.getDescription()));
        if (type.getReference() != null) {
            os.println();
            os.print("    REFERENCE       ");
            os.print(getQuote(type.getReference()));
        }
        os.println();
        os.print("    SYNTAX          ");
        printType(type.getSyntax(), "                    ");
    }

    /**
     * Prints an array of MIB type elements.
     *
     * @param elems          the type elements to print
     * @param indent         the indentation to use on new lines
     */
    private void printTypeElements(ElementType[] elems, String indent) {
        int     column = 35;
        String  typeIndent = indent;
        int     i;

        for (i = 0; i < elems.length; i++) {
            if (elems[i].getName().length() + 2 > column) {
                column = elems[i].getName().length() + 2;
            }
        }
        for (i = 0; i < column; i++) {
            typeIndent += " ";
        }
        for (i = 0; i < elems.length; i++) {
            if (i > 0) {
                os.println(",");
                os.print(indent);
            }
            os.print(elems[i].getName());
            for (int j = elems[i].getName().length(); j < column; j++) {
                os.print(" ");
            }
            printType(elems[i].getType(), typeIndent);
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
                os.print(indent);
            }
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
     * Prints a type or value object. This method is useful for
     * printing index values.
     *
     * @param obj            the type or value object
     */
    private void printTypeOrValue(Object obj) {
        ObjectIdentifierValue  oid;

        if (obj instanceof ObjectIdentifierValue) {
            oid = (ObjectIdentifierValue) obj;
            if (oid.getSymbol() != null) {
                os.print(oid.getSymbol().getName());
            } else {
                os.print(oid.toAsn1String());
            }
        } else {
            os.print(obj.toString());
        }
    }

    /**
     * Prints an indented string. This method will indent each non-
     * blank line in the string with the specified indentation.
     *
     * @param indent         the indentation string
     * @param str            the string to print
     */
    private void printIndent(String indent, String str) {
        int  pos;

        while (str != null && (pos = str.indexOf('\n')) >=0) {
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
     * Returns a correctly ASN.1 quoted version of a string.
     *
     * @param str            the string to quote
     *
     * @return a correct ASN.1 string syntax
     */
    private String getQuote(String str) {
        StringBuffer  buffer = new StringBuffer();

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
