/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2009-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A MIB module locator. This class attempts to map MIB module names
 * to files in a directory or on a resource path. It keeps two
 * internal caches; one based on file names, and one based on the
 * first few lines of file content. Each of these caches are created
 * upon first use and the content cache is normally a secondary
 * alternative due to the performance penalty when created.
 *
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.10
 */
public class MibLocator {

    /**
     * The MIB name regular expression pattern.
     */
    private static final Pattern NAME = Pattern.compile("[a-zA-Z][a-zA-Z0-9-_]*");

    /**
     * The optional class loader to use for locating MIB files. If
     * set, the MIB modules are searched as resources via this loader
     * and the directory path prefix.
     */
    private ClassLoader classLoader;

    /**
     * The directory to search. If used together with a class loader,
     * this contains the resource path to search.
     */
    private File dir;

    /**
     * The file name cache. This cache is indexed by upper-case MIB
     * name and points to the MIB source.
     */
    private HashMap<String,MibSource> nameCache = null;

    /**
     * The content cache. This cache is indexed by the MIB name read
     * from the file and points to the MIB source.
     */
    private HashMap<String,MibSource> contentCache = null;

    /**
     * Creates a new MIB module locator for a file directory.
     *
     * @param dir            the directory to index
     */
    public MibLocator(File dir) {
        this.dir = dir;
    }

    /**
     * Creates a new MIB module locator for a resource path on the
     * class path. Note that the resource path must be unique to a
     * specific JAR (or directory).
     *
     * @param classLoader    the class loader to use
     * @param path           the resource path to index
     */
    public MibLocator(ClassLoader classLoader, String path) {
        this.classLoader = classLoader;
        this.dir = new File(path);
    }

    /**
     * Checks if the class loader is used for locating resources.
     *
     * @return true if the directory is a resource path, or
     *         false if it represents a local directory
     */
    public boolean isResourceDir() {
        return this.classLoader != null;
    }

    /**
     * Returns the directory or resource path indexed. If used with
     * a class loader, the file object is be a resource path (not an
     * existing file).
     *
     * @return the directory or resource path indexed
     */
    public File getDir() {
        return dir;
    }

    /**
     * Returns a URL to the directory or resource path indexed. If
     * used with a class loader, the URL may point to a JAR file
     * (with a path suffix). Otherwise, the URL uses the "file:"
     * protocol and points to the absolute file path.
     *
     * @return the URL to the directory or resource path indexed
     */
    public URL getUrl() {
        if (isResourceDir()) {
            return this.classLoader.getResource(this.dir.toString());
        } else {
            try {
                return this.dir.toURI().toURL();
            } catch (MalformedURLException ignore) {
                return null;
            }
        }
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
    public Map<String,MibSource> getNameMap() {
        if (nameCache == null) {
            nameCache = new HashMap<>();
            URL url = this.getUrl();
            if (url == null) {
                // No files found
            } else if (url.getProtocol().equals("jar")) {
                nameCache.putAll(readJar(url, this.dir.toString(), false));
            } else if (url.getProtocol().equals("file")) {
                nameCache.putAll(readDir(new File(url.getPath()), false));
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
    public Map<String,MibSource> getContentMap() {
        if (contentCache == null) {
            contentCache = new HashMap<>();
            URL url = this.getUrl();
            if (url == null) {
                // No files found
            } else if (url.getProtocol().equals("jar")) {
                contentCache.putAll(readJar(url, this.dir.toString(), true));
            } else if (url.getProtocol().equals("file")) {
                contentCache.putAll(readDir(new File(url.getPath()), true));
            }
        }
        return contentCache;
    }

    /**
     * Searches for a MIB in the file name cache. The file name match
     * is case insensitive and ignores file extensions and suffixes.
     * Note that there are no guarantees that the returned file is
     * indeed a MIB file.
     *
     * @param mibName        the MIB name
     *
     * @return the first matching MIB source, or
     *         null if no match was found
     */
    public MibSource findByName(String mibName) {
        return getNameMap().get(mibName.toUpperCase());
    }

    /**
     * Searches for a MIB in the content cache. This cache is costly
     * to initialize (on first call), but may be more accurate as it
     * is based on the first few lines of each file.
     *
     * @param mibName        the MIB name
     *
     * @return the first matching MIB source, or
     *         null if no match was found
     */
    public MibSource findByContent(String mibName) {
        return getContentMap().get(mibName);
    }

    /**
     * Finds all MIB files found in directory. The MIB names are
     * either guessed from the file names or read from the content.
     * MIB name guesses are always upper-case.
     *
     * @param dir            the file directory
     * @param readContent    the read MIB content flag
     *
     * @return a map of MIB module names to MIB sources
     */
    private static Map<String,MibSource> readDir(File dir, boolean readContent) {
        HashMap<String,MibSource> res = new HashMap<>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                String mibName = null;
                if (!readContent) {
                    mibName = guessMibName(file.getName());
                } else {
                    mibName = readMibName(file);
                }
                if (mibName != null) {
                    res.put(mibName, new MibSource(file));
                }
            }
        }
        return res;
    }

    /**
     * Finds all MIB files found in a JAR URL. The MIB names are
     * either guessed from the file names or read from the content.
     * MIB name guesses are always upper-case.
     *
     * @param url            the JAR URL (resource URL)
     * @param prefix         the path prefix
     * @param readContent    the read MIB content flag
     *
     * @return a map of MIB module names to MIB sources
     */
    private static Map<String,MibSource> readJar(URL url,
                                                 String prefix,
                                                 boolean readContent) {

        HashMap<String,MibSource> res = new HashMap<>();
        try (
            JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
        ){
            String urlBase = url.toString().replaceAll("!.*", "");
            Enumeration<JarEntry> e = jar.entries();
            while (e.hasMoreElements()) {
                String path = e.nextElement().getName();
                if (path.startsWith(prefix)) {
                    URL resUrl = new URL(urlBase + "!/" + path);
                    String mibName = null;
                    if (!readContent) {
                        mibName = guessMibName(path);
                    } else {
                        mibName = readMibName(resUrl);
                    }
                    if (mibName != null) {
                        res.put(mibName, new MibSource(path, resUrl));
                    }
                }
            }
        } catch (Exception ignore) {
            // Do nothing
        }
        return res;
    }

    /**
     * Returns a possible matching MIB from a path. Any directory
     * portion of the path will be removed before matching the file
     * name to a MIB name regular expression. Any file extension or
     * non-matching file suffix is ignored.
     *
     * @param path           the full file path
     *
     * @return the matching MIB name, or
     *         null for none
     */
    private static String guessMibName(String path) {
        File file = new File(path);
        Matcher m = NAME.matcher(file.getName());
        if (m.lookingAt() && !path.endsWith("/") && !file.isDirectory()) {
            return m.group().toUpperCase();
        } else {
            return null;
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
    private static String readMibName(File file) {
        if (!file.canRead() || !file.isFile()) {
            return null;
        }
        try (
            Reader in = new FileReader(file);
        ) {
            return readMibName(in);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Reads the initial lines of a supposed text file attempting to
     * extract a MIB name.
     *
     * @param url            the URL to read
     *
     * @return the MIB name found, or
     *         null if no name was found
     */
    private static String readMibName(URL url) {
        try (
            Reader in = new InputStreamReader(url.openStream());
        ) {
            return readMibName(in);
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Reads the initial lines of a supposed text file attempting to
     * extract a MIB name.
     *
     * @param reader         the input stream to read
     *
     * @return the MIB name found, or
     *         null if no name was found
     */
    private static String readMibName(Reader reader) {
        try (
            BufferedReader in = new BufferedReader(reader);
        ) {
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
        } catch (Exception ignore) {
            // Do nothing
        }
        return null;
    }
}
