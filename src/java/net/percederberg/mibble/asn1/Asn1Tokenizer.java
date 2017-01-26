/*
 * Asn1Tokenizer.java
 *
 * THIS FILE HAS BEEN GENERATED AUTOMATICALLY. DO NOT EDIT!
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.asn1;

import java.io.Reader;

import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.TokenPattern;
import net.percederberg.grammatica.parser.Tokenizer;

/**
 * A character stream tokenizer.
 *
 * @author   Per Cederberg
 * @version  2.9
 */
public class Asn1Tokenizer extends Tokenizer {

    /**
     * Creates a new tokenizer for the specified input stream.
     *
     * @param input          the input stream to read
     *
     * @throws ParserCreationException if the tokenizer couldn't be
     *             initialized correctly
     */
    public Asn1Tokenizer(Reader input) throws ParserCreationException {
        super(input, false);
        createPatterns();
    }

    /**
     * Initializes the tokenizer by creating all the token patterns.
     *
     * @throws ParserCreationException if the tokenizer couldn't be
     *             initialized correctly
     */
    private void createPatterns() throws ParserCreationException {
        TokenPattern  pattern;

        pattern = new TokenPattern(Asn1Constants.DOT,
                                   "DOT",
                                   TokenPattern.STRING_TYPE,
                                   ".");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DOUBLE_DOT,
                                   "DOUBLE_DOT",
                                   TokenPattern.STRING_TYPE,
                                   "..");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.TRIPLE_DOT,
                                   "TRIPLE_DOT",
                                   TokenPattern.STRING_TYPE,
                                   "...");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.COMMA,
                                   "COMMA",
                                   TokenPattern.STRING_TYPE,
                                   ",");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.SEMI_COLON,
                                   "SEMI_COLON",
                                   TokenPattern.STRING_TYPE,
                                   ";");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.LEFT_PAREN,
                                   "LEFT_PAREN",
                                   TokenPattern.STRING_TYPE,
                                   "(");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.RIGHT_PAREN,
                                   "RIGHT_PAREN",
                                   TokenPattern.STRING_TYPE,
                                   ")");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.LEFT_BRACE,
                                   "LEFT_BRACE",
                                   TokenPattern.STRING_TYPE,
                                   "{");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.RIGHT_BRACE,
                                   "RIGHT_BRACE",
                                   TokenPattern.STRING_TYPE,
                                   "}");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.LEFT_BRACKET,
                                   "LEFT_BRACKET",
                                   TokenPattern.STRING_TYPE,
                                   "[");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.RIGHT_BRACKET,
                                   "RIGHT_BRACKET",
                                   TokenPattern.STRING_TYPE,
                                   "]");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MINUS,
                                   "MINUS",
                                   TokenPattern.STRING_TYPE,
                                   "-");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.LESS_THAN,
                                   "LESS_THAN",
                                   TokenPattern.STRING_TYPE,
                                   "<");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.VERTICAL_BAR,
                                   "VERTICAL_BAR",
                                   TokenPattern.STRING_TYPE,
                                   "|");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DEFINITION,
                                   "DEFINITION",
                                   TokenPattern.STRING_TYPE,
                                   "::=");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DEFINITIONS,
                                   "DEFINITIONS",
                                   TokenPattern.STRING_TYPE,
                                   "DEFINITIONS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.EXPLICIT,
                                   "EXPLICIT",
                                   TokenPattern.STRING_TYPE,
                                   "EXPLICIT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.IMPLICIT,
                                   "IMPLICIT",
                                   TokenPattern.STRING_TYPE,
                                   "IMPLICIT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.TAGS,
                                   "TAGS",
                                   TokenPattern.STRING_TYPE,
                                   "TAGS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.BEGIN,
                                   "BEGIN",
                                   TokenPattern.STRING_TYPE,
                                   "BEGIN");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.END,
                                   "END",
                                   TokenPattern.STRING_TYPE,
                                   "END");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.EXPORTS,
                                   "EXPORTS",
                                   TokenPattern.STRING_TYPE,
                                   "EXPORTS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.IMPORTS,
                                   "IMPORTS",
                                   TokenPattern.STRING_TYPE,
                                   "IMPORTS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.FROM,
                                   "FROM",
                                   TokenPattern.STRING_TYPE,
                                   "FROM");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MACRO,
                                   "MACRO",
                                   TokenPattern.STRING_TYPE,
                                   "MACRO");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.INTEGER,
                                   "INTEGER",
                                   TokenPattern.STRING_TYPE,
                                   "INTEGER");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.REAL,
                                   "REAL",
                                   TokenPattern.STRING_TYPE,
                                   "REAL");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.BOOLEAN,
                                   "BOOLEAN",
                                   TokenPattern.STRING_TYPE,
                                   "BOOLEAN");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.NULL,
                                   "NULL",
                                   TokenPattern.STRING_TYPE,
                                   "NULL");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.BIT,
                                   "BIT",
                                   TokenPattern.STRING_TYPE,
                                   "BIT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OCTET,
                                   "OCTET",
                                   TokenPattern.STRING_TYPE,
                                   "OCTET");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.STRING,
                                   "STRING",
                                   TokenPattern.STRING_TYPE,
                                   "STRING");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.ENUMERATED,
                                   "ENUMERATED",
                                   TokenPattern.STRING_TYPE,
                                   "ENUMERATED");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.SEQUENCE,
                                   "SEQUENCE",
                                   TokenPattern.STRING_TYPE,
                                   "SEQUENCE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.SET,
                                   "SET",
                                   TokenPattern.STRING_TYPE,
                                   "SET");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OF,
                                   "OF",
                                   TokenPattern.STRING_TYPE,
                                   "OF");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.CHOICE,
                                   "CHOICE",
                                   TokenPattern.STRING_TYPE,
                                   "CHOICE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.UNIVERSAL,
                                   "UNIVERSAL",
                                   TokenPattern.STRING_TYPE,
                                   "UNIVERSAL");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.APPLICATION,
                                   "APPLICATION",
                                   TokenPattern.STRING_TYPE,
                                   "APPLICATION");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.PRIVATE,
                                   "PRIVATE",
                                   TokenPattern.STRING_TYPE,
                                   "PRIVATE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.ANY,
                                   "ANY",
                                   TokenPattern.STRING_TYPE,
                                   "ANY");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DEFINED,
                                   "DEFINED",
                                   TokenPattern.STRING_TYPE,
                                   "DEFINED");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.BY,
                                   "BY",
                                   TokenPattern.STRING_TYPE,
                                   "BY");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OBJECT,
                                   "OBJECT",
                                   TokenPattern.STRING_TYPE,
                                   "OBJECT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.IDENTIFIER,
                                   "IDENTIFIER",
                                   TokenPattern.STRING_TYPE,
                                   "IDENTIFIER");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.INCLUDES,
                                   "INCLUDES",
                                   TokenPattern.STRING_TYPE,
                                   "INCLUDES");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MIN,
                                   "MIN",
                                   TokenPattern.STRING_TYPE,
                                   "MIN");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MAX,
                                   "MAX",
                                   TokenPattern.STRING_TYPE,
                                   "MAX");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.SIZE,
                                   "SIZE",
                                   TokenPattern.STRING_TYPE,
                                   "SIZE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.WITH,
                                   "WITH",
                                   TokenPattern.STRING_TYPE,
                                   "WITH");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.COMPONENT,
                                   "COMPONENT",
                                   TokenPattern.STRING_TYPE,
                                   "COMPONENT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.COMPONENTS,
                                   "COMPONENTS",
                                   TokenPattern.STRING_TYPE,
                                   "COMPONENTS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.PRESENT,
                                   "PRESENT",
                                   TokenPattern.STRING_TYPE,
                                   "PRESENT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.ABSENT,
                                   "ABSENT",
                                   TokenPattern.STRING_TYPE,
                                   "ABSENT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OPTIONAL,
                                   "OPTIONAL",
                                   TokenPattern.STRING_TYPE,
                                   "OPTIONAL");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DEFAULT,
                                   "DEFAULT",
                                   TokenPattern.STRING_TYPE,
                                   "DEFAULT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.TRUE,
                                   "TRUE",
                                   TokenPattern.STRING_TYPE,
                                   "TRUE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.FALSE,
                                   "FALSE",
                                   TokenPattern.STRING_TYPE,
                                   "FALSE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.PLUS_INFINITY,
                                   "PLUS_INFINITY",
                                   TokenPattern.STRING_TYPE,
                                   "PLUS-INFINITY");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MINUS_INFINITY,
                                   "MINUS_INFINITY",
                                   TokenPattern.STRING_TYPE,
                                   "MINUS-INFINITY");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MODULE_IDENTITY,
                                   "MODULE_IDENTITY",
                                   TokenPattern.STRING_TYPE,
                                   "MODULE-IDENTITY");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OBJECT_IDENTITY,
                                   "OBJECT_IDENTITY",
                                   TokenPattern.STRING_TYPE,
                                   "OBJECT-IDENTITY");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OBJECT_TYPE,
                                   "OBJECT_TYPE",
                                   TokenPattern.STRING_TYPE,
                                   "OBJECT-TYPE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.NOTIFICATION_TYPE,
                                   "NOTIFICATION_TYPE",
                                   TokenPattern.STRING_TYPE,
                                   "NOTIFICATION-TYPE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.TRAP_TYPE,
                                   "TRAP_TYPE",
                                   TokenPattern.STRING_TYPE,
                                   "TRAP-TYPE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.TEXTUAL_CONVENTION,
                                   "TEXTUAL_CONVENTION",
                                   TokenPattern.STRING_TYPE,
                                   "TEXTUAL-CONVENTION");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OBJECT_GROUP,
                                   "OBJECT_GROUP",
                                   TokenPattern.STRING_TYPE,
                                   "OBJECT-GROUP");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.NOTIFICATION_GROUP,
                                   "NOTIFICATION_GROUP",
                                   TokenPattern.STRING_TYPE,
                                   "NOTIFICATION-GROUP");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MODULE_COMPLIANCE,
                                   "MODULE_COMPLIANCE",
                                   TokenPattern.STRING_TYPE,
                                   "MODULE-COMPLIANCE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.AGENT_CAPABILITIES,
                                   "AGENT_CAPABILITIES",
                                   TokenPattern.STRING_TYPE,
                                   "AGENT-CAPABILITIES");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.LAST_UPDATED,
                                   "LAST_UPDATED",
                                   TokenPattern.STRING_TYPE,
                                   "LAST-UPDATED");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.ORGANIZATION,
                                   "ORGANIZATION",
                                   TokenPattern.STRING_TYPE,
                                   "ORGANIZATION");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.CONTACT_INFO,
                                   "CONTACT_INFO",
                                   TokenPattern.STRING_TYPE,
                                   "CONTACT-INFO");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DESCRIPTION,
                                   "DESCRIPTION",
                                   TokenPattern.STRING_TYPE,
                                   "DESCRIPTION");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.REVISION,
                                   "REVISION",
                                   TokenPattern.STRING_TYPE,
                                   "REVISION");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.STATUS,
                                   "STATUS",
                                   TokenPattern.STRING_TYPE,
                                   "STATUS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.REFERENCE,
                                   "REFERENCE",
                                   TokenPattern.STRING_TYPE,
                                   "REFERENCE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.SYNTAX,
                                   "SYNTAX",
                                   TokenPattern.STRING_TYPE,
                                   "SYNTAX");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.BITS,
                                   "BITS",
                                   TokenPattern.STRING_TYPE,
                                   "BITS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.UNITS,
                                   "UNITS",
                                   TokenPattern.STRING_TYPE,
                                   "UNITS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.ACCESS,
                                   "ACCESS",
                                   TokenPattern.STRING_TYPE,
                                   "ACCESS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MAX_ACCESS,
                                   "MAX_ACCESS",
                                   TokenPattern.STRING_TYPE,
                                   "MAX-ACCESS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MIN_ACCESS,
                                   "MIN_ACCESS",
                                   TokenPattern.STRING_TYPE,
                                   "MIN-ACCESS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.INDEX,
                                   "INDEX",
                                   TokenPattern.STRING_TYPE,
                                   "INDEX");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.AUGMENTS,
                                   "AUGMENTS",
                                   TokenPattern.STRING_TYPE,
                                   "AUGMENTS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.IMPLIED,
                                   "IMPLIED",
                                   TokenPattern.STRING_TYPE,
                                   "IMPLIED");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DEFVAL,
                                   "DEFVAL",
                                   TokenPattern.STRING_TYPE,
                                   "DEFVAL");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.OBJECTS,
                                   "OBJECTS",
                                   TokenPattern.STRING_TYPE,
                                   "OBJECTS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.ENTERPRISE,
                                   "ENTERPRISE",
                                   TokenPattern.STRING_TYPE,
                                   "ENTERPRISE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.VARIABLES,
                                   "VARIABLES",
                                   TokenPattern.STRING_TYPE,
                                   "VARIABLES");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.DISPLAY_HINT,
                                   "DISPLAY_HINT",
                                   TokenPattern.STRING_TYPE,
                                   "DISPLAY-HINT");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.NOTIFICATIONS,
                                   "NOTIFICATIONS",
                                   TokenPattern.STRING_TYPE,
                                   "NOTIFICATIONS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MODULE,
                                   "MODULE",
                                   TokenPattern.STRING_TYPE,
                                   "MODULE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.MANDATORY_GROUPS,
                                   "MANDATORY_GROUPS",
                                   TokenPattern.STRING_TYPE,
                                   "MANDATORY-GROUPS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.GROUP,
                                   "GROUP",
                                   TokenPattern.STRING_TYPE,
                                   "GROUP");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.WRITE_SYNTAX,
                                   "WRITE_SYNTAX",
                                   TokenPattern.STRING_TYPE,
                                   "WRITE-SYNTAX");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.PRODUCT_RELEASE,
                                   "PRODUCT_RELEASE",
                                   TokenPattern.STRING_TYPE,
                                   "PRODUCT-RELEASE");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.SUPPORTS,
                                   "SUPPORTS",
                                   TokenPattern.STRING_TYPE,
                                   "SUPPORTS");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.VARIATION,
                                   "VARIATION",
                                   TokenPattern.STRING_TYPE,
                                   "VARIATION");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.CREATION_REQUIRES,
                                   "CREATION_REQUIRES",
                                   TokenPattern.STRING_TYPE,
                                   "CREATION-REQUIRES");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.BINARY_STRING,
                                   "BINARY_STRING",
                                   TokenPattern.REGEXP_TYPE,
                                   "'[0-1]*'(B|b)");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.HEXADECIMAL_STRING,
                                   "HEXADECIMAL_STRING",
                                   TokenPattern.REGEXP_TYPE,
                                   "'[0-9A-Fa-f]*'(H|h)");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.QUOTED_STRING,
                                   "QUOTED_STRING",
                                   TokenPattern.REGEXP_TYPE,
                                   "\"([^\"]|\"\")*\"");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.IDENTIFIER_STRING,
                                   "IDENTIFIER_STRING",
                                   TokenPattern.REGEXP_TYPE,
                                   "[a-zA-Z][a-zA-Z0-9-_]*");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.NUMBER_STRING,
                                   "NUMBER_STRING",
                                   TokenPattern.REGEXP_TYPE,
                                   "[0-9]+");
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.WHITESPACE,
                                   "WHITESPACE",
                                   TokenPattern.REGEXP_TYPE,
                                   "[ \\t\\n\\r\\f\\x0b\\x17\\x18\\x19\\x1a]+");
        pattern.setIgnore();
        addPattern(pattern);

        pattern = new TokenPattern(Asn1Constants.COMMENT,
                                   "COMMENT",
                                   TokenPattern.REGEXP_TYPE,
                                   "--([^\\n\\r-]|-[^\\n\\r-])*(--|-?[\\n\\r])");
        pattern.setIgnore();
        addPattern(pattern);
    }
}
