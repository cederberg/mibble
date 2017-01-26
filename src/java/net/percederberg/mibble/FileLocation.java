/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
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
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class FileLocation {

    /**
     * The file name.
     */
    protected File file;

    /**
     * The line number.
     */
    protected int line;

    /**
     * The column number.
     */
    protected int column;

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
     * will NOT contain the terminating '\n' character. This method
     * takes special care to only count the linefeed (LF, 0x0A)
     * character as a valid newline.
     *
     * @return the line read, or
     *         null if not found
     */
    public String readLine() {
        if (file == null || line < 0) {
            return null;
        }
        try (
            BufferedReader input = new BufferedReader(new FileReader(file));
        ) {
            // Only count line-feed characters in files with invalid line
            // termination sequences. The default readLine() method doesn't
            // quite do the right thing in those cases... (bug #16252)
            int count = 1;
            int chr;
            while (count < line && (chr = input.read()) >= 0) {
                if (chr == '\n') {
                    count++;
                }
            }
            return input.readLine();
        } catch (IOException e) {
            return null;
        }
    }
}
