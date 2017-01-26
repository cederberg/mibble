/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble.snmp;

import java.util.ArrayList;

import net.percederberg.mibble.MibException;
import net.percederberg.mibble.MibLoaderLog;
import net.percederberg.mibble.MibValue;

/**
 * An SNMP module compliance value. This declaration is used inside
 * the module compliance macro type.
 *
 * @see SnmpModuleCompliance
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class SnmpModule {

    /**
     * The module name.
     */
    private String module;

    /**
     * The list of mandatory group values.
     */
    private ArrayList<MibValue> groups;

    /**
     * The list of compliances.
     */
    private ArrayList<SnmpCompliance> compliances;

    /**
     * The module comment.
     */
    private String comment = null;

    /**
     * Creates a new module compliance declaration.
     *
     * @param module         the module name, or null
     * @param groups         the list of mandatory group values
     * @param compliances    the list of compliances
     */
    public SnmpModule(String module,
                      ArrayList<MibValue> groups,
                      ArrayList<SnmpCompliance> compliances) {

        this.module = module;
        this.groups = groups;
        this.compliances = compliances;
    }

    /**
     * Initializes the object. This will remove all levels of
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
    void initialize(MibLoaderLog log) throws MibException {
        for (int i = 0; i < groups.size(); i++) {
            groups.set(i, groups.get(i).initialize(log, null));
        }
        for (SnmpCompliance comp : compliances) {
            comp.initialize(log);
        }
    }

    /**
     * Returns the module name.
     *
     * @return the module name, or
     *         null if not set
     */
    public String getModule() {
        return module;
    }

    /**
     * Returns the list of mandatory group values. The returned list
     * will consist of MibValue instances.
     *
     * @return the list of mandatory group values
     *
     * @see net.percederberg.mibble.MibValue
     */
    public ArrayList<MibValue> getGroups() {
        return groups;
    }

    /**
     * Returns the list of compliances. The returned list will
     * consist of SnmpCompliance instances.
     *
     * @return the list of compliances
     *
     * @see SnmpCompliance
     */
    public ArrayList<SnmpCompliance> getCompliances() {
        return compliances;
    }

    /**
     * Returns the module comment.
     *
     * @return the module comment, or
     *         null if no comment was set
     *
     * @since 2.9
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the module comment.
     *
     * @param comment        the module comment
     *
     * @since 2.9
     */
    public void setComment(String comment) {
        if (module != null || !"THIS MODULE".equalsIgnoreCase(comment)) {
            this.comment = comment;
        }
    }

    /**
     * Returns a string representation of this object.
     *
     * @return a string representation of this object
     */
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        if (module != null) {
            buffer.append(module);
        }
        if (groups.size() > 0) {
            buffer.append("\n    Mandatory Groups: ");
            buffer.append(groups);
        }
        for (SnmpCompliance comp : compliances) {
            buffer.append("\n    Module: ");
            buffer.append(comp);
        }
        return buffer.toString();
    }
}
