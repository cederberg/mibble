/*
 * SnmpType.java
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
 * Copyright (c) 2004 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import net.percederberg.mibble.MibType;

/**
 * The base SNMP macro type. This is an abstract type, meaning there
 * only exist instances of subclasses. It exists to provide methods
 * that are valid across all SNMP macro types.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.5
 * @since    2.5
 */
public abstract class SnmpType extends MibType {

    /**
     * The type description.
     */
    private String description;

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
        StringBuffer  buffer = new StringBuffer();
        String        str = description;
        int           indent = -1;
        int           pos;

        if (str == null) {
            return null;
        }
        while (str.length() > 0) {
            pos = str.indexOf("\n");
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
        StringBuffer  buffer;
        String        str = getDescription();
        int           pos;

        if (str == null) {
            return null;
        }
        pos = str.indexOf("\n");
        if (pos < 0) {
            return str;
        } else {
            buffer = new StringBuffer();
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

    /**
     * Removes the specified number of leading spaces from a string.
     * All tab characters in the string will be converted to spaces
     * before processing. If the string contains fewer than the
     * specified number of leading spaces, all will be removed. If
     * the indentation count is less than zero (0), all leading
     * spaces in the input string will be removed.
     *
     * @param str            the input string
     * @param indent         the indentation space count
     *
     * @return the unindented string
     */
    private String removeIndent(String str, int indent) {
        int  pos = 0;

        str = replaceTabs(str);
        if (indent < 0) {
            return str.trim();
        }
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
    private String replaceTabs(String str) {
        StringBuffer  buffer;

        if (str.indexOf('\t') < 0) {
            return str;
        } else {
            buffer = new StringBuffer();
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
    private boolean isSpace(char ch) {
        return ch == ' '
            || ch == '\t';
    }
}
