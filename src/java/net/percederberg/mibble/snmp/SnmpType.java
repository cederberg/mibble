/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import net.percederberg.mibble.MibType;

/**
 * The base SNMP macro type. This is an abstract type, meaning there
 * only exist instances of subclasses. It exists to provide methods
 * that are valid across all SNMP macro types.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.5
 */
public abstract class SnmpType extends MibType {

    /**
     * The type description.
     */
    private String description;

    /**
     * Returns a string with any unneeded indentation removed. This
     * method will decide the indentation level from the number of
     * spaces on the second line. It also replaces all tab characters
     * with 8 spaces.
     *
     * @param str            the string to process
     *
     * @return the processed string
     */
    protected static String removeIndent(String str) {
        StringBuilder buffer = new StringBuilder();
        if (str == null) {
            return null;
        }
        int indent = -1;
        while (str.length() > 0) {
            int pos = str.indexOf("\n");
            if (pos < 0) {
                buffer.append(removeIndent(str, indent));
                str = "";
            } else if (pos == 0) {
                buffer.append("\n");
                str = str.substring(1);
            } else if (str.substring(0, pos).trim().length() == 0) {
                buffer.append("\n");
                str = str.substring(pos + 1);
            } else if (buffer.length() == 0) {
                buffer.append(removeIndent(str.substring(0, pos), -1));
                buffer.append("\n");
                str = str.substring(pos + 1);
            } else {
                if (indent < 0) {
                    indent = 0;
                    for (int i = 0; isSpace(str.charAt(i)); i++) {
                        if (str.charAt(i) == '\t') {
                            indent += 8;
                        } else {
                            indent++;
                        }
                    }
                }
                buffer.append(removeIndent(str.substring(0, pos), indent));
                buffer.append("\n");
                str = str.substring(pos + 1);
            }
        }
        return buffer.toString();
    }

    /**
     * Removes the specified number of leading spaces from a string.
     * All tab characters in the string will be converted to spaces
     * before processing. If the string contains fewer than the
     * specified number of leading spaces, all will be removed. If
     * the indentation count is less than zero (0), all leading
     * spaces in the input string will be removed.
     *
     * @param str            the input string
     * @param indent         the indentation space count
     *
     * @return the unindented string
     */
    private static String removeIndent(String str, int indent) {
        str = replaceTabs(str);
        if (indent < 0) {
            return str.trim();
        }
        int pos = 0;
        for (pos = 0; pos < str.length() && pos < indent; pos++) {
            if (str.charAt(pos) != ' ') {
                break;
            }
        }
        return str.substring(pos);
    }

    /**
     * Replaces any tab characters with 8 space characters.
     *
     * @param str            the input string
     *
     * @return the new string without tab characters
     */
    private static String replaceTabs(String str) {
        if (str.indexOf('\t') < 0) {
            return str;
        } else {
            StringBuilder buffer = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '\t') {
                    buffer.append("        ");
                } else {
                    buffer.append(str.charAt(i));
                }
            }
            return buffer.toString();
        }
    }

    /**
     * Checks if a character is a space character.
     *
     * @param ch             the character to check
     *
     * @return true if the character is a space character, or
     *         false otherwise
     */
    private static boolean isSpace(char ch) {
        return ch == ' '
            || ch == '\t';
    }

    /**
     * Creates a new SNMP macro type instance. This constructor can
     * only be called by subclasses.
     *
     * @param name           the type name
     * @param description    the type description
     */
    protected SnmpType(String name, String description) {
        super(name, false);
        this.description = description;
    }

    /**
     * Returns the type description. Any unneeded indentation will be
     * removed from the description, and it also replaces all tab
     * characters with 8 spaces.
     *
     * @return the type description, or
     *         null if no description has been set
     *
     * @see #getUnformattedDescription()
     */
    public String getDescription() {
        return removeIndent(description);
    }

    /**
     * Returns the unformatted type description. This method returns
     * the original MIB file description, without removing unneeded
     * indentation or similar.
     *
     * @return the unformatted type description, or
     *         null if no description has been set
     *
     * @see #getDescription()
     *
     * @since 2.5
     */
    public String getUnformattedDescription() {
        return description;
    }

    /**
     * Returns the type description indented with the specified
     * string. The first line will NOT be indented, but only the
     * following lines (if any).
     *
     * @param indent         the indentation string
     *
     * @return the indented type description, or
     *         null if no description has been set
     */
    protected String getDescription(String indent) {
        String str = getDescription();
        if (str == null) {
            return null;
        }
        int pos = str.indexOf("\n");
        if (pos < 0) {
            return str;
        } else {
            StringBuilder buffer = new StringBuilder();
            buffer.append(str.substring(0, pos + 1));
            str = str.substring(pos + 1);
            while (pos >= 0) {
                buffer.append(indent);
                pos = str.indexOf("\n");
                if (pos < 0) {
                    buffer.append(str);
                } else {
                    buffer.append(str.substring(0, pos + 1));
                    str = str.substring(pos + 1);
                }
            }
            return buffer.toString();
        }
    }
}
