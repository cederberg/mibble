/*
 * MibFileRef.java
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
 * Copyright (c) 2004-2016 Per Cederberg. All rights reserved.
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
