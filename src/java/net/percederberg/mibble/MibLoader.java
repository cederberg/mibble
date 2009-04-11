/*
 * MibLoader.java
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
 * Copyright (c) 2004-2009 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;

import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;

import net.percederberg.mibble.asn1.Asn1Parser;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A MIB loader. This class contains a search path for locating MIB
 * files, and also holds a refererence to previously loaded MIB files
 * to avoid loading the same file multiple times. The MIB search path
 * consists of directories with MIB files that can be imported into
 * other MIBs. The search path directories can either be normal file
 * system directories or resource directories. By default the search
 * path contains resource directories containing standard IANA and
 * IETF MIB files (packaged in the Mibble JAR file).<p>
 *
 * The MIB loader searches for MIB files in a specific order. First,
 * the file system directories in the search path are scanned for
 * files with the same name as the MIB module being imported. This
 * search ignores any file name extensions and compares the base file
 * name in case-insensitive mode. If this search fails, the resource
 * directories are searched for a file having the exact name of the
 * MIB module being imported (case sensitive). The last step, if both
 * the previous ones have failed, is to open the files in the search
 * path one by one to check the MIB module names specified inside.
 * Note that this may be slow for large directories with many files,
 * and it is therefore recommended to always name the MIB files
 * according to their module name.<p>
 *
 * The MIB loader is not thread-safe, i.e. it cannot be used
 * concurrently in multiple threads.
 *
 * @author   Per Cederberg, <per at percederberg dot net>
 * @version  2.9
 * @since    2.0
 */
public class MibLoader {

    /**
     * The MIB file directory caches. This is also a list of the MIB
     * file search path, as each directory on the path has its own
     * cache. If a MIB isn't found among these directories, the
     * resource directories will be attempted.
     */
    private ArrayList dirCaches = new ArrayList();

    /**
     * The MIB file resource directories. This is a list of Java class
     * loader resource directories to search for MIB files. These
     * directories can be used to store MIB files as resources inside
     * a JAR file.
     */
    private ArrayList resources = new ArrayList();

    /**
     * The MIB files loaded. This list contains all MIB file loaded
     * with this loader, in order to avoid loading some MIB files
     * multiple times (and thus duplicating import symbols).
     */
    private ArrayList mibs = new ArrayList();

    /**
     * The queue of MIB files to load. This queue contains either
     * MIB module names or MibSource objects.
     */
    private ArrayList queue = new ArrayList();

    /**
     * The default MIB context.
     */
    private DefaultContext context = new DefaultContext();

    /**
     * Creates a new MIB loader.
     */
    public MibLoader() {
        addResourceDir("mibs/iana");
        addResourceDir("mibs/ietf");
    }

    /**
     * Checks if a directory is in the MIB search path. If a file is
     * specified instead of a directory, this method checks if the
     * parent directory is in the MIB search path.
     *
     * @param dir            the directory or file to check
     *
     * @return true if the directory is in the MIB search path, or
     *         false otherwise
     *
     * @since 2.9
     */
    public boolean hasDir(File dir) {
        MibDirectoryCache  cache;

        if (dir == null) {
            dir = new File(".");
        } else if (!dir.isDirectory()) {
            dir = dir.getParentFile();
        }
        for (int i = 0; i < dirCaches.size(); i++) {
            cache = (MibDirectoryCache) dirCaches.get(i);
            if (cache.getDir().equals(dir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all the directories in the MIB search path. If a tree
     * of directories has been added, all the individual directories
     * will be returned by this method.
     *
     * @return the directories in the MIB search path
     *
     * @since 2.9
     */
    public File[] getDirs() {
        File[]             res = new File[dirCaches.size()];
        MibDirectoryCache  cache;

        for (int i = 0; i < dirCaches.size(); i++) {
            cache = (MibDirectoryCache) dirCaches.get(i);
            res[i] = cache.getDir();
        }
        return res;
    }

    /**
     * Adds a directory to the MIB search path. If the directory
     * specified is null, the current working directory will be added.
     *
     * @param dir            the directory to add
     */
    public void addDir(File dir) {
        if (dir == null) {
            dir = new File(".");
        }
        if (!hasDir(dir)) {
            dirCaches.add(new MibDirectoryCache(dir));
        }
    }

    /**
     * Adds directories to the MIB search path.
     *
     * @param dirs           the directories to add
     */
    public void addDirs(File[] dirs) {
        for (int i = 0; i < dirs.length; i++) {
            addDir(dirs[i]);
        }
    }

    /**
     * Adds a directory and all subdirectories to the MIB search path.
     * If the directory specified is null, the current working
     * directory (and subdirectories) will be added.
     *
     * @param dir            the directory to add
     */
    public void addAllDirs(File dir) {
        File[]  files;

        if (dir == null) {
            dir = new File(".");
        }
        addDir(dir);
        files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                addAllDirs(files[i]);
            }
        }
    }

    /**
     * Removes a directory from the MIB search path.
     *
     * @param dir            the directory to remove
     */
    public void removeDir(File dir) {
        MibDirectoryCache  cache;

        for (int i = 0; i < dirCaches.size(); i++) {
            cache = (MibDirectoryCache) dirCaches.get(i);
            if (cache.getDir().equals(dir)) {
                dirCaches.remove(i--);
            }
        }
    }

    /**
     * Removes all directories from the MIB search path.
     */
    public void removeAllDirs() {
        dirCaches.clear();
    }

    /**
     * Checks if a directory is in the MIB resource path. The
     * resource search path is used for searching for MIB files with
     * the ClassLoader (i.e. MIB files on the Java classpath) and is
     * a secondary alternative to the directory search path. Note
     * that the MIB files stored as resources must have the EXACT MIB
     * name, i.e. no file extensions can be used and name casing is
     * important.
     *
     * @param dir            the directory to check
     *
     * @return true if the directory is in the MIB resource path, or
     *         false otherwise
     *
     * @since 2.9
     */
    public boolean hasResourceDir(String dir) {
        return resources.contains(dir);
    }

    /**
     * Returns all the directories in the MIB resource path. The
     * resource search path is used for searching for MIB files with
     * the ClassLoader (i.e. MIB files on the Java classpath) and is
     * a secondary alternative to the directory search path. Note
     * that the MIB files stored as resources must have the EXACT MIB
     * name, i.e. no file extensions can be used and name casing is
     * important.
     *
     * @return the directories in the MIB resource path
     *
     * @since 2.9
     */
    public String[] getResourceDirs() {
        return (String[]) resources.toArray(new String[resources.size()]);
    }

    /**
     * Adds a directory to the MIB resource search path. The resource
     * search path is used for searching for MIB files with the
     * ClassLoader (i.e. MIB files on the Java classpath) and is a
     * secondary alternative to the directory search path. Note that
     * the MIB files stored as resources must have the EXACT MIB
     * name, i.e. no file extensions can be used and name casing is
     * important.
     *
     * @param dir            the resource directory to add
     *
     * @since 2.3
     */
    public void addResourceDir(String dir) {
        if (!hasResourceDir(dir)) {
            resources.add(dir);
        }
    }

    /**
     * Removes a directory from the MIB resource search path. The
     * resource search path can be used to load MIB files as resources
     * via the ClassLoader.
     *
     * @param dir            the resource directory to remove
     *
     * @since 2.3
     */
    public void removeResourceDir(String dir) {
        resources.remove(dir);
    }

    /**
     * Removes all directories from the MIB resource search path. This
     * will also remove the default directories where the IANA and
     * IETF MIB are present, and may thus make this MIB loader mostly
     * unusable. Use this method with caution.
     *
     * @since 2.3
     */
    public void removeAllResourceDirs() {
        resources.clear();
    }

    /**
     * Resets this loader. This means that all references to previuos
     * MIB files will be removed, forcing a reload of any imported
     * MIB.<p>
     *
     * Note that this is not the same operation as unloadAll, since
     * the MIB files previously loaded will be unaffected by this
     * this method (i.e. they remain possible to use). If the purpose
     * is to free all memory used by the loaded MIB files, use the
     * unloadAll() method instead.
     *
     * @see #unloadAll()
     */
    public void reset() {
        mibs.clear();
        queue.clear();
        context = new DefaultContext();
    }

    /**
     * Returns the default MIB context. This context contains the
     * symbols that are predefined for all MIB:s (such as 'iso').
     *
     * @return the default MIB context
     */
    public MibContext getDefaultContext() {
        return context;
    }

    /**
     * Returns the root object identifier value (OID). This OID is
     * the "iso" symbol.
     *
     * @return the root object identifier value
     *
     * @since 2.7
     */
    public ObjectIdentifierValue getRootOid() {
        MibSymbol symbol;
        MibValue  value;

        symbol = context.findSymbol(DefaultContext.ISO, false);
        value = ((MibValueSymbol) symbol).getValue();
        return (ObjectIdentifierValue) value;
    }

    /**
     * Returns a previously loaded MIB file. If the MIB file hasn't
     * been loaded, null will be returned. The MIB is identified by
     * it's MIB name (i.e. the module name).
     *
     * @param name           the MIB (module) name
     *
     * @return the MIB module if found, or
     *         null otherwise
     */
    public Mib getMib(String name) {
        Mib  mib;

        for (int i = 0; i < mibs.size(); i++) {
            mib = (Mib) mibs.get(i);
            if (mib.equals(name)) {
                return mib;
            }
        }
        return null;
    }

    /**
     * Returns a previously loaded MIB file. If the MIB file hasn't
     * been loaded, null will be returned. The MIB is identified by
     * it's file name. Note that if the file contained several MIB
     * modules, this method will only return the first one.
     *
     * @param file           the MIB file
     *
     * @return the first MIB module if found, or
     *         null otherwise
     *
     * @since 2.3
     */
    public Mib getMib(File file) {
        Mib  mib;

        for (int i = 0; i < mibs.size(); i++) {
            mib = (Mib) mibs.get(i);
            if (mib.equals(file)) {
                return mib;
            }
        }
        return null;
    }

    /**
     * Returns all previously loaded MIB files. If no MIB files have
     * been loaded an empty array will be returned.
     *
     * @return an array with all loaded MIB files
     *
     * @since 2.2
     */
    public Mib[] getAllMibs() {
        Mib[]  res;

        res = new Mib[mibs.size()];
        mibs.toArray(res);
        return res;
    }

    /**
     * Loads a MIB file with the specified base name. The file is
     * searched for in the MIB search path. The MIB is identified by
     * it's MIB name (i.e. the module name). This method will also
     * load all imported MIB:s if not previously loaded by this
     * loader. If a MIB with the same name has already been loaded, it
     * will be returned directly instead of reloading it.
     *
     * @param name           the MIB name (filename without extension)
     *
     * @return the MIB module loaded
     *
     * @throws IOException if the MIB file couldn't be found in the
     *             MIB search path
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     */
    public Mib load(String name) throws IOException, MibLoaderException {
        MibSource  src;
        Mib        mib;

        mib = getMib(name);
        if (mib == null) {
            src = locate(name);
            if (src == null) {
                throw new FileNotFoundException("couldn't locate MIB: '" +
                                                name + "'");
            }
            mib = load(src);
        } else {
            mib.setLoaded(true);
        }
        return mib;
    }

    /**
     * Loads a MIB file. This method will also load all imported MIB:s
     * if not previously loaded by this loader. If a MIB with the same
     * file name has already been loaded, it will be returned directly
     * instead of reloading it. Note that if a file contains several
     * MIB modules, this method will only return the first one
     * (although all are loaded).
     *
     * @param file           the MIB file
     *
     * @return the first MIB module loaded
     *
     * @throws IOException if the MIB file couldn't be read
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     */
    public Mib load(File file) throws IOException, MibLoaderException {
        Mib  mib;

        mib = getMib(file);
        if (mib == null) {
            mib = load(new MibSource(file));
        } else {
            mib.setLoaded(true);
        }
        return mib;
    }

    /**
     * Loads a MIB file from the specified URL. This method will also
     * load all imported MIB:s if not previously loaded by this
     * loader. Note that if the URL data contains several MIB modules,
     * this method will only return the first one (although all are
     * loaded).
     *
     * @param url            the URL containing the MIB
     *
     * @return the first MIB module loaded
     *
     * @throws IOException if the MIB URL couldn't be read
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     *
     * @since 2.3
     */
    public Mib load(URL url) throws IOException, MibLoaderException {
        return load(new MibSource(url));
    }

    /**
     * Loads a MIB file from the specified input reader. This method
     * will also load all imported MIB:s if not previously loaded by
     * this loader. Note that if the input data contains several MIB
     * modules, this method will only return the first one (although
     * all are loaded).
     *
     * @param input          the input stream containing the MIB
     *
     * @return the first MIB module loaded
     *
     * @throws IOException if the input stream couldn't be read
     * @throws MibLoaderException if the MIB file couldn't be loaded
     *             correctly
     *
     * @since 2.3
     */
    public Mib load(Reader input) throws IOException, MibLoaderException {
        return load(new MibSource(input));
    }

    /**
     * Loads a MIB. This method will also load all imported MIB:s if
     * not previously loaded by this loader. Note that if the source
     * contains several MIB modules, this method will only return the
     * first one (although all are loaded).
     *
     * @param src            the MIB source
     *
     * @return the first MIB module loaded
     *
     * @throws IOException if the MIB couldn't be found
     * @throws MibLoaderException if the MIB couldn't be loaded
     *             correctly
     */
    private Mib load(MibSource src) throws IOException, MibLoaderException {

        int           position;
        MibLoaderLog  log;

        position = mibs.size();
        queue.clear();
        queue.add(src);
        log = loadQueue();
        if (log.errorCount() > 0) {
            throw new MibLoaderException(log);
        }
        return (Mib) mibs.get(position);
    }

    /**
     * Unloads a MIB. This method will remove the loader reference to
     * a previously loaded MIB if no other MIBs are depending on it.
     * This method attempts to free the memory used by the MIB, as it
     * clears both the loader and internal MIB references to the data
     * structures (thereby allowing the garbage collector to recover
     * the memory used if no other references exist). Other MIB:s
     * should be unaffected by this operation.
     *
     * @param name           the MIB name
     *
     * @throws MibLoaderException if the MIB couldn't be unloaded
     *             due to dependencies from other loaded MIBs
     *
     * @see #reset
     *
     * @since 2.3
     */
    public void unload(String name) throws MibLoaderException {
        Mib  mib;

        for (int i = 0; i < mibs.size(); i++) {
            mib = (Mib) mibs.get(i);
            if (mib.equals(name)) {
                unload(mib);
                return;
            }
        }
    }

    /**
     * Unloads a MIB. This method will remove the loader reference to
     * a previously loaded MIB if no other MIBs are depending on it.
     * This method attempts to free the memory used by the MIB, as it
     * clears both the loader and internal MIB references to the data
     * structures (thereby allowing the garbage collector to recover
     * the memory used if no other references exist). Other MIB:s
     * should be unaffected by this operation.
     *
     * @param file           the MIB file
     *
     * @throws MibLoaderException if the MIB couldn't be unloaded
     *             due to dependencies from other loaded MIBs
     *
     * @see #reset
     *
     * @since 2.3
     */
    public void unload(File file) throws MibLoaderException {
        Mib  mib;

        for (int i = 0; i < mibs.size(); i++) {
            mib = (Mib) mibs.get(i);
            if (mib.equals(file)) {
                unload(mib);
                return;
            }
        }
    }

    /**
     * Unloads a MIB. This method will remove the loader reference to
     * a previously loaded MIB if no other MIBs are depending on it.
     * This method attempts to free the memory used by the MIB, as it
     * clears both the loader and internal MIB references to the data
     * structures (thereby allowing the garbage collector to recover
     * the memory used if no other references exist). Other MIB:s
     * should be unaffected by this operation.
     *
     * @param mib            the MIB
     *
     * @throws MibLoaderException if the MIB couldn't be unloaded
     *             due to dependencies from other loaded MIBs
     *
     * @see #reset
     *
     * @since 2.3
     */
    public void unload(Mib mib) throws MibLoaderException {
        Mib[]   referers;
        String  message;
        int     pos;

        pos = mibs.indexOf(mib);
        if (pos >= 0) {
            referers = mib.getImportingMibs();
            if (referers.length > 0) {
                message = "cannot be unloaded due to reference in " +
                          referers[0];
                throw new MibLoaderException(mib.getFile(), message);
            }
            mib = (Mib) mibs.remove(pos);
            mib.clear();
        }
    }

    /**
     * Unloads all MIBs loaded by this loaded (since the last reset).
     * This method attempts to free all the memory used by the MIBs,
     * as it clears both the loader and internal MIB references to
     * the data structures (thereby allowing the garbage collector to
     * recover the memory used if no other references exist). Note
     * that no previous MIBs returned by this loader should be
     * accessed after this method has been called.<p>
     *
     * In order to just reset the MIB loader to force re-loading of
     * MIB files, use the reset() method instead which will leave the
     * MIBs unaffected.
     *
     * @see #reset()
     * @since 2.9
     */
    public void unloadAll() {
        for (int i = 0; i < mibs.size(); i++) {
            ((Mib) mibs.get(i)).clear();
        }
        reset();
    }

    /**
     * Schedules the loading of a MIB file. The file is added to the
     * queue of MIB files to be loaded, unless it is already loaded
     * or in the queue. The MIB file search is postponed until the
     * MIB is to be loaded, avoiding loading if the MIB name was
     * defined in another MIB file in the queue.
     *
     * @param name           the MIB name (filename without extension)
     */
    void scheduleLoad(String name) {
        if (getMib(name) == null && !queue.contains(name)) {
            queue.add(name);
        }
    }

    /**
     * Loads all MIB files in the loader queue. New entries may be
     * added to the queue while loading a MIB, as a result of
     * importing other MIB files. This method will either load all
     * MIB files in the queue or none (if errors were encountered).
     *
     * @return the loader log for the whole queue
     *
     * @throws IOException if the MIB file in the queue couldn't be
     *             found
     */
    private MibLoaderLog loadQueue() throws IOException {
        MibLoaderLog  log = new MibLoaderLog();
        ArrayList     processed = new ArrayList();
        boolean       loaded;
        MibSource     src;
        ArrayList     list;
        Object        obj;

        // Parse MIB files in queue
        while (queue.size() > 0) {
            try {
                loaded = false;
                obj = queue.get(0);
                if (obj instanceof MibSource) {
                    loaded = true;
                    src = (MibSource) obj;
                } else if (getMib((String) obj) == null) {
                    src = locate((String) obj);
                } else {
                    src = null;
                }
                if (src != null && getMib(src.getFile()) == null) {
                    list = src.parseMib(this, log);
                    for (int i = 0; i < list.size(); i++) {
                        ((Mib) list.get(i)).setLoaded(loaded);
                    }
                    mibs.addAll(list);
                    processed.addAll(list);
                }
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log
            }
            queue.remove(0);
        }

        // Initialize all parsed MIB files in reverse order
        for (int i = processed.size() - 1; i >= 0; i--) {
            try {
                ((Mib) processed.get(i)).initialize();
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log
            }
        }

        // Validate all parsed MIB files in reverse order
        for (int i = processed.size() - 1; i >= 0; i--) {
            try {
                ((Mib) processed.get(i)).validate();
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log
            }
        }

        // Handle errors
        if (log.errorCount() > 0) {
            mibs.removeAll(processed);
        }

        return log;
    }

    /**
     * Searches for a MIB in the search path. The name specified
     * should be the MIB name. If a matching file name isn't found in
     * the directory search path, the contents in the base resource
     * path are also tested. Finally, if no MIB file has been found,
     * the files in the search path will be opened regardless of file
     * name to perform a small heuristic test for the MIB in question.
     *
     * @param name           the MIB name
     *
     * @return the MIB found, or
     *         null if no MIB was found
     */
    private MibSource locate(String name) {
        ClassLoader        loader = getClass().getClassLoader();
        MibDirectoryCache  cache;
        File               file;
        URL                url;
        int                i;

        for (i = 0; i < dirCaches.size(); i++) {
            cache = (MibDirectoryCache) dirCaches.get(i);
            file = cache.findByName(name);
            if (file != null) {
                return new MibSource(file);
            }
        }
        for (i = 0; i < resources.size(); i++) {
            url = loader.getResource(resources.get(i) + "/" + name);
            if (url != null) {
                return new MibSource(name, url);
            }
        }
        for (i = 0; i < dirCaches.size(); i++) {
            cache = (MibDirectoryCache) dirCaches.get(i);
            file = cache.findByContent(name);
            if (file != null) {
                return new MibSource(file);
            }
        }
        return null;
    }

    /**
     * A MIB input source. This class encapsulates the two different
     * ways of loacating a MIB file, either through a file or a URL.
     */
    private static class MibSource {

        /**
         * The singleton ASN.1 parser used by all MIB sources.
         */
        private static Asn1Parser parser = null;

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
         * the specified URL. This method also create a default file
         * from the specified MIB name in order to improve possible
         * error messages.
         *
         * @param name           the MIB name
         * @param url            the URL to read from
         */
        public MibSource(String name, URL url) {
            this(url);
            this.file = new File(name);
        }

        /**
         * Creates a new MIB input source. The MIB will be read from
         * the specified input reader. The input reader will be closed
         * after reading the MIB.
         *
         * @param input          the input stream to read from
         */
        public MibSource(Reader input) {
            this.input = input;
        }

        /**
         * Checks if this object is equal to another. This method
         * will only return true for another mib source object with
         * the same input source.
         *
         * @param obj            the object to compare with
         *
         * @return true if the object is equal to this, or
         *         false otherwise
         */
        public boolean equals(Object obj) {
            MibSource  src;

            if (obj instanceof MibSource) {
                src = (MibSource) obj;
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
         * reimplemented to fulfil the contract of returning the same
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
         * Returns the MIB file. If the MIB is loaded from URL this
         * file does not actually exist, but is used for providing a
         * unique reference to the MIB.
         *
         * @return the MIB file
         */
        public File getFile() {
            return file;
        }

        /**
         * Parses the MIB input source and returns the MIB modules
         * found. This method will read the MIB either from file, URL
         * or input stream.
         *
         * @param loader         the MIB loader to use for imports
         * @param log            the MIB log to use for errors
         *
         * @return the list of MIB modules created
         *
         * @throws IOException if the MIB couldn't be found
         * @throws MibLoaderException if the MIB couldn't be parsed
         *             or analyzed correctly
         */
        public ArrayList parseMib(MibLoader loader, MibLoaderLog log)
            throws IOException, MibLoaderException {

            MibAnalyzer  analyzer;
            String       msg;

            // Open input stream
            if (input != null) {
                // Do nothing as input stream already setup
            } else if (url != null) {
                input = new InputStreamReader(url.openStream());
            } else {
                input = new FileReader(file);
            }

            // Parse input stream
            analyzer = new MibAnalyzer(file, loader, log);
            try {
                if (parser == null) {
                    parser = new Asn1Parser(input, analyzer);
                    parser.getTokenizer().setUseTokenList(true);
                } else {
                    parser.reset(input, analyzer);
                }
                parser.parse();
                return analyzer.getMibs();
            } catch (ParserCreationException e) {
                msg = "parser creation error in ASN.1 parser: " +
                      e.getMessage();
                log.addInternalError(file, msg);
                throw new MibLoaderException(log);
            } catch (ParserLogException e) {
                log.addAll(file, e);
                throw new MibLoaderException(log);
            } finally {
                try {
                    input.close();
                } catch (Throwable ignore) {
                    // Errors on close are ignored
                }
                analyzer.reset();
            }
        }
    }
}
