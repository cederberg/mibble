/*
 * FileLocation.java
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

package net.percederberg.mibble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A file location. This class contains a reference to an exact
 * location inside a text file.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.0
 * @since    2.0
 */
public class FileLocation {

    /**
     * The file name.
     */
    private File file;

    /**
     * The line number.
     */
    private int line;

    /**
     * The column number.
     */
    private int column;

    /**
     * Creates a new file location without an exact line or column
     * reference.
     *
     * @param file           the file name
     */
    public FileLocation(File file) {
        this(file, -1, -1);
    }

    /**
     * Creates a new file location.
     *
     * @param file           the file name
     * @param line           the line number
     * @param column         the column number
     */
    public FileLocation(File file, int line, int column) {
        this.file = file;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the file name.
     *
     * @return the file name
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the line number.
     *
     * @return the line number
     */
    public int getLineNumber() {
        return line;
    }

    /**
     * Returns the column number.
     *
     * @return the column number
     */
    public int getColumnNumber() {
        return column;
    }

    /**
     * Reads the specified line from the file. If the file couldn't
     * be opened or read correctly, null will be returned. The line
     * will NOT contain the terminating '\n' character.
     *
     * @return the line read, or
     *         null if not found
     */
    public String readLine() {
        BufferedReader  input;
        String          str = null;

        if (file == null || line < 0) {
            return null;
        }
        try {
            input = new BufferedReader(new FileReader(file));
            for (int i = 0; i < line; i++) {
                str = input.readLine();
            }
            input.close();
        } catch (IOException e) {
            return null;
        }
        return str;
    }
}
