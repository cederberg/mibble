/*
 * MibDirectoryCache.java
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
 * Copyright (c) 2009 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A MIB search directory cache. This class attempts to map MIB names
 * to files for a single directory. It keeps two internal caches; one
 * based on the file name similarity with MIB names, and one based on
 * the content of the first few lines in each file. Each of these
 * caches are created upon request and the content cache is normally
 * used only as a secondary alternative due to the performance
 * penalty of its creation.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.9
 */
class MibDirectoryCache {

    /**
     * The MIB name regular expression pattern.
     */
    private static final Pattern NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9-_]*");

    /**
     * The directory to search.
     */
    private File dir;

    /**
     * The file name cache. This cache is indexed by upper-case MIB
     * name and links to the directory file.
     */
    private HashMap nameCache = null;

    /**
     * The content cache. This cache is indexed by the actual MIB
     * name read from the file and links to the directory file.
     */
    private HashMap contentCache = null;

    /**
     * Creates a new MIB search directory cache.
     *
     * @param dir            the directory to index
     */
    public MibDirectoryCache(File dir) {
        this.dir = dir;
    }

    /**
     * Returns the directory indexed by this cache.
     *
     * @return the directory indexed by this cache
     */
    public File getDir() {
        return dir;
    }

    /**
     * Searches for a named MIB in the directory file name cache.
     * Note that there are no guarantees that the returned file is
     * indeed a MIB file.
     *
     * @param mibName        the MIB name
     *
     * @return the first matching MIB file,
     *         null if no match was found
     */
    public File findByName(String mibName) {
        if (nameCache == null) {
            initNameCache();
        }
        return (File) nameCache.get(mibName.toUpperCase());
    }

    /**
     * Initializes the name cache.
     */
    private void initNameCache() {
        File[]   files = dir.listFiles();
        String   name;
        Matcher  m;

        nameCache = new HashMap();
        for (int i = 0; i < files.length; i++) {
            name = files[i].getName();
            m = NAME.matcher(name);
            if (m.lookingAt() && files[i].isFile()) {
                nameCache.put(m.group().toUpperCase(), files[i]);
            }
        }
    }

    /**
     * Searches for a named MIB in the directory content cache. This
     * cache is costly to initialize, but since it is based on the
     * actual content of the first few lines in each file it is more
     * accurate.
     *
     * @param mibName        the MIB name
     *
     * @return the first matching MIB file,
     *         null if no match was found
     */
    public File findByContent(String mibName) {
        if (contentCache == null) {
            initContentCache();
        }
        return (File) contentCache.get(mibName);
    }

    /**
     * Initializes the content cache.
     */
    private void initContentCache() {
        File[]   files = dir.listFiles();
        String   name;

        contentCache = new HashMap();
        for (int i = 0; i < files.length; i++) {
            name = readMibName(files[i]);
            if (name != null) {
                contentCache.put(name, files[i]);
            }
        }
    }

    /**
     * Reads the initial lines of a supposed text file attempting to
     * extract a MIB name.
     *
     * @param file           the file to read
     *
     * @return the MIB name found, or
     *         null if no name was found
     */
    private String readMibName(File file) {
        BufferedReader  in = null;
        String          str;
        Matcher         m;

        if (!file.canRead() || !file.isFile()) {
            return null;
        }
        try {
            in = new BufferedReader(new FileReader(file));
            while (true) {
                str = in.readLine();
                if (str == null) {
                    break;
                }
                str = str.trim();
                if (!str.equals("") && !str.startsWith("--")) {
                    m = NAME.matcher(str);
                    return m.lookingAt() ? m.group() : null;
                }
            }
        } catch (FileNotFoundException ignore) {
            // Do nothing
        } catch (IOException ignore) {
            // Do nothing
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignore) {
                    // Do nothing
                }
            }
        }
        return null;
    }
}
