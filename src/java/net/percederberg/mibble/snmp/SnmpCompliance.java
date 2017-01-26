/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibValue;

/**
 * An SNMP module compliance value. This declaration is used inside a
 * module declaration for both the GROUP and OBJECT compliance parts.
 *
 * @see SnmpModule
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SnmpCompliance {

    /**
     * The compliance group flag.
     */
    private boolean group;

    /**
     * The compliance value.
     */
    private MibValue value;

    /**
     * The value syntax.
     */
    private MibType syntax;

    /**
     * The value write syntax.
     */
    private MibType writeSyntax;

    /**
     * The access mode.
     */
    private SnmpAccess access;

    /**
     * The compliance description.
     */
    private String description;

    /**
     * The compliance comment.
     */
    private String comment = null;

    /**
     * Creates a new SNMP module compliance declaration.
     *
     * @param group          the group compliance flag
     * @param value          the compliance value
     * @param syntax         the value syntax, or null
     * @param writeSyntax    the value write syntax, or null
     * @param access         the access mode, or null
     * @param description    the compliance description
     */
    public SnmpCompliance(boolean group,
                          MibValue value,
                          MibType syntax,
                          MibType writeSyntax,
                          SnmpAccess access,
                          String description) {

        this.group = group;
        this.value = value;
        this.syntax = syntax;
        this.writeSyntax = writeSyntax;
        this.access = access;
        this.description = description;
    }

    /**
     * Initializes this object. This will remove all levels of
     * indirection present, such as references to other types, and
     * returns the basic type. No type information is lost by this
     * operation. This method may modify this object as a
     * side-effect, and will be called by the MIB loader.
     *
     * @param log            the MIB loader log
     *
     * @throws MibException if an error was encountered during the
     *             initialization
     */
    void initialize(MibLoaderLog log)
        throws MibException {

        value = value.initialize(log, null);
        if (syntax != null) {
            syntax = syntax.initialize(null, log);
        }
        if (writeSyntax != null) {
            writeSyntax = writeSyntax.initialize(null, log);
        }
    }

    /**
     * Checks if this is a group compliance.
     *
     * @return true if this is a group compliance, or
     *         false otherwise
     *
     * @since 2.6
     */
    public boolean isGroup() {
        return group;
    }

    /**
     * Checks if this is an object compliance.
     *
     * @return true if this is an object compliance, or
     *         false otherwise
     *
     * @since 2.6
     */
    public boolean isObject() {
        return !group;
    }

    /**
     * Returns the value.
     *
     * @return the value
     */
    public MibValue getValue() {
        return value;
    }

    /**
     * Returns the value syntax.
     *
     * @return the value syntax, or
     *         null if not set
     */
    public MibType getSyntax() {
        return syntax;
    }

    /**
     * Returns the value write syntax.
     *
     * @return the value write syntax, or
     *         null if not set
     */
    public MibType getWriteSyntax() {
        return writeSyntax;
    }

    /**
     * Returns the access mode.
     *
     * @return the access mode, or
     *         null if not set
     */
    public SnmpAccess getAccess() {
        return access;
    }

    /**
     * Returns the compliance description. Any unneeded indentation
     * will be removed from the description, and it also replaces all
     * tab characters with 8 spaces.
     *
     * @return the compliance description
     *
     * @see #getUnformattedDescription()
     */
    public String getDescription() {
        return SnmpType.removeIndent(description);
    }

    /**
     * Returns the unformatted compliance description. This method
     * returns the original MIB file text, without removing unneeded
     * indentation or similar.
     *
     * @return the unformatted compliance description, or
     *         null if no description has been set
     *
     * @see #getDescription()
     *
     * @since 2.6
     */
    public String getUnformattedDescription() {
        return description;
    }

    /**
     * Returns the compliance comment.
     *
     * @return the compliance comment, or
     *         null if no comment was set
     *
     * @since 2.9
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the compliance comment.
     *
     * @param comment        the compliance comment
     *
     * @since 2.9
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(value);
        if (syntax != null) {
            buffer.append("\n      Syntax: ");
            buffer.append(syntax);
        }
        if (writeSyntax != null) {
            buffer.append("\n      Write-Syntax: ");
            buffer.append(writeSyntax);
        }
        if (access != null) {
            buffer.append("\n      Access: ");
            buffer.append(access);
        }
        buffer.append("\n      Description: ");
        buffer.append(description);
        return buffer.toString();
    }
}
