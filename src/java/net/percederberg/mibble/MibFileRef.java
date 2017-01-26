/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;

/**
 * A MIB file reference. This class contains a reference to an exact
 * location inside a MIB source file.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.10
 */
public class MibFileRef extends FileLocation {

    /**
     * The line number for the initial (prefixing) comment. If no
     * comment is present, this will be set to the same line number
     * as the starting line number.
     */
    protected int lineCommentStart = -1;

    /**
     * The line number for the last line of the symbol declaration.
     * This may be set to the same line number as the starting line
     * number.
     */
    protected int lineEnd = -1;

    /**
     * Creates a void MIB file reference. This is used when line
     * numbers or file name isn't available, typically for symbols
     * automatically created outside the loaded MIB (such as the
     * OID root symbols).
     */
    public MibFileRef() {
        this(null, -1, -1);
    }

    /**
     * Creates a new MIB file reference.
     *
     * @param file           the file name
     * @param line           the start line number
     * @param column         the start column number
     */
    public MibFileRef(File file, int line, int column) {
        super(file, line, column);
        this.lineCommentStart = line;
        this.lineEnd = line;
    }
}
