/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2009-2016 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A MIB directory mapper. This class attempts to map MIB names to
 * files in a directory. It keeps two internal caches; one based on
 * the file name similarity with MIB names, and one based on the
 * content of the first few lines in each file. Each of these caches
 * are created upon first request and the content cache is normally
 * only used as a secondary alternative due to the performance
 * penalty when it is created.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.10
 */
public class MibDirectory {

    /**
     * The MIB name regular expression pattern.
     */
    private static final Pattern NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9-_]*");

    /**
     * The directory to search.
     */
    private File dir;

    /**
     * The file name cache. This cache is indexed by upper-case file
     * name (without extension) and links to the actual file.
     */
    private HashMap<String,File> nameCache = null;

    /**
     * The content cache. This cache is indexed by the MIB name read
     * from the file and links to the actual file.
     */
    private HashMap<String,File> contentCache = null;

    /**
     * Creates a new MIB search directory cache.
     *
     * @param dir            the directory to index
     */
    public MibDirectory(File dir) {
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
     * Returns (and creates if needed) the MIB file name map. This
     * map converts file names to potential MIB module names and maps
     * them to the directory file. The MIB modules names will be all
     * UPPERCASE. Note that there are no guarantees that the returned
     * files are indeed MIB files.
     *
     * @return a map of MIB module names to files
     */
    public Map<String,File> getNameMap() {
        if (nameCache == null) {
            nameCache = new HashMap<String,File>();
            File[] files = dir.listFiles();
            for (int i = 0; files != null && i < files.length; i++) {
                String name = files[i].getName();
                Matcher m = NAME.matcher(name);
                if (m.lookingAt() && files[i].isFile()) {
                    nameCache.put(m.group().toUpperCase(), files[i]);
                }
            }
        }
        return nameCache;
    }

    /**
     * Returns (and creates if needed) the MIB file content map. This
     * map maps MIB module names read from the initial lines of the
     * files to the directory file. Note that there are no absolute
     * guarantees that the returned files are indeed MIB files, only
     * the first few lines will have been read (and only to fetch the
     * MIB module name).
     *
     * @return a map of MIB module names to files
     */
    public Map<String,File> getContentMap() {
        if (contentCache == null) {
            contentCache = new HashMap<String,File>();
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                String name = readMibName(files[i]);
                if (name != null) {
                    contentCache.put(name, files[i]);
                }
            }
        }
        return contentCache;
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
        return getNameMap().get(mibName.toUpperCase());
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
        return getContentMap().get(mibName);
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
        if (!file.canRead() || !file.isFile()) {
            return null;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            while (true) {
                String str = in.readLine();
                if (str == null) {
                    break;
                }
                str = str.trim();
                if (!str.equals("") && !str.startsWith("--")) {
                    Matcher m = NAME.matcher(str);
                    boolean hasDefToken = str.contains("DEFINITIONS");
                    return m.lookingAt() && hasDefToken ? m.group() : null;
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
