/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2009-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * A MIB input source. This class encapsulates the different ways of
 * locating a MIB file, either through a file or a URL.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.10
 */
public class MibSource {

    /**
     * The MIB file. This variable is only set if the MIB is read
     * from file, or if the MIB name is known.
     */
    private File file = null;

    /**
     * The MIB URL location. This variable is only set if the MIB
     * is read from a URL.
     */
    private URL url = null;

    /**
     * The MIB reader. This variable is only set if the MIB
     * is read from an input stream.
     */
    private Reader input = null;

    /**
     * Creates a new MIB input source. The MIB will be read from
     * the specified file.
     *
     * @param file           the file to read from
     */
    public MibSource(File file) {
        this.file = file;
    }

    /**
     * Creates a new MIB input source. The MIB will be read from
     * the specified URL.
     *
     * @param url            the URL to read from
     */
    public MibSource(URL url) {
        this.url = url;
    }

    /**
     * Creates a new MIB input source. The MIB will be read from
     * the specified URL. This method also creates a reference file
     * from the specified MIB path to better report error locations.
     *
     * @param path           the resource path (i.e. error location)
     * @param url            the URL to read from
     */
    public MibSource(String path, URL url) {
        this.file = new File(path);
        this.url = url;
    }

    /**
     * Creates a new MIB input source. The MIB will be read from
     * the specified input reader. The input reader will be closed
     * after reading the MIB.
     *
     * @param input          the input stream to read from
     */
    protected MibSource(Reader input) {
        this.input = input;
    }

    /**
     * Checks if this object is equal to another. This method
     * will only return true for another MIB source object with
     * the same input source.
     *
     * @param obj            the object to compare with
     *
     * @return true if the object is equal to this, or
     *         false otherwise
     */
    public boolean equals(Object obj) {
        if (obj instanceof MibSource) {
            MibSource src = (MibSource) obj;
            if (url != null) {
                return url.equals(src.url);
            } else if (file != null) {
                return file.equals(src.file);
            }
        }
        return false;
    }

    /**
     * Returns the hash code value for the object. This method is
     * re-implemented to fulfill the contract of returning the same
     * hash code for objects that are considered equal.
     *
     * @return the hash code value for the object
     *
     * @since 2.6
     */
    public int hashCode() {
        if (url != null) {
            return url.hashCode();
        } else if (file != null) {
            return file.hashCode();
        } else {
            return super.hashCode();
        }
    }

    /**
     * Returns the MIB file if set. If the MIB source is a URL, the
     * file may still be set (and contain only the known MIB name).
     *
     * @return the MIB file, or null
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the MIB URL if set.
     *
     * @return the MIB URL, or null
     */
    public URL getURL() {
        return url;
    }

    /**
     * Returns a stream reader for the MIB file. It is the
     * responsibility of the caller to ensure closing the stream
     * (after use).
     *
     * @return a stream reader for the MIB file
     *
     * @throws IOException if the MIB file couldn't be opened
     */
    public Reader getReader() throws IOException {
        if (input != null) {
            return input;
        } else if (url != null) {
            return new InputStreamReader(url.openStream());
        } else {
            return new FileReader(file);
        }
    }
}
