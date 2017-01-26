/*
 * Mibble MIB Parser (www.mibble.org)
 *
 * See LICENSE.txt for licensing information.
 *
 * Copyright (c) 2004-2017 Per Cederberg. All rights reserved.
 */

package net.percederberg.mibble;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.percederberg.grammatica.parser.ParserCreationException;
import net.percederberg.grammatica.parser.ParserLogException;

import net.percederberg.mibble.asn1.Asn1Parser;
import net.percederberg.mibble.value.ObjectIdentifierValue;

/**
 * A MIB loader. This class contains a search path for locating MIB
 * files, and also holds a reference to previously loaded MIB files
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
 * @author   Per Cederberg
 * @version  2.10
 * @since    2.0
 */
public class MibLoader {

    /**
     * The MIB file directory caches. This is also a list of the MIB
     * file search path, as each directory on the path has its own
     * cache. If a MIB isn't found among these directories, the
     * resource directories will be attempted.
     */
    private ArrayList<MibLocator> dirCaches = new ArrayList<>();

    /**
     * The MIB file resource directories. This is a list of Java class
     * loader resource directories to search for MIB files. These
     * directories can be used to store MIB files as resources inside
     * a JAR file.
     */
    private ArrayList<String> resources = new ArrayList<>();

    /**
     * The MIB files loaded. This maps the MIB names to the loaded
     * MIB objects (loaded with this loaded). This is used to avoid
     * loading duplicate MIB files.
     */
    private LinkedHashMap<String,Mib> mibs = new LinkedHashMap<>();

    /**
     * The queue of MIB files to load. This queue contains either
     * MIB module names or MibSource objects.
     */
    private ArrayList<Object> queue = new ArrayList<>();

    /**
     * The default MIB context.
     */
    private DefaultContext context = new DefaultContext();

    /**
     * The ASN.1 parser used (and reused) for all MIB files.
     */
    private Asn1Parser parser = null;

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
        if (dir == null) {
            dir = new File(".");
        } else if (!dir.isDirectory()) {
            dir = dir.getParentFile();
        }
        for (MibLocator cache : dirCaches) {
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
        File[] res = new File[dirCaches.size()];
        for (int i = 0; i < dirCaches.size(); i++) {
            MibLocator cache = dirCaches.get(i);
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
        if (!hasDir(dir) && dir.isDirectory()) {
            dirCaches.add(new MibLocator(dir));
        }
    }

    /**
     * Adds directories to the MIB search path.
     *
     * @param dirs           the directories to add
     */
    public void addDirs(File[] dirs) {
        for (File file : dirs) {
            addDir(file);
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
        if (dir == null) {
            dir = new File(".");
        }
        addDir(dir);
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                addAllDirs(file);
            }
        }
    }

    /**
     * Removes a directory from the MIB search path.
     *
     * @param dir            the directory to remove
     */
    public void removeDir(File dir) {
        for (int i = 0; i < dirCaches.size(); i++) {
            MibLocator cache = dirCaches.get(i);
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
        return resources.toArray(new String[resources.size()]);
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
     * Searches the OID tree from the loaded MIB files for the best
     * matching value. The returned OID value will be the longest
     * matching OID value, but doesn't have to be an exact match. The
     * search requires the full numeric OID value (from the root).
     *
     * @param oid            the numeric OID string to search for
     *
     * @return the best matching OID value, or
     *         null if no partial match was found
     *
     * @see ObjectIdentifierValue#find(String)
     * @since 2.10
     */
    public ObjectIdentifierValue getOid(String oid) {
        return context.findOid(oid);
    }

    /**
     * Returns the "iso" root object identifier value (OID). This OID
     * is the root for SNMP objects. Note that "ccitt" and
     * "joint-iso-ccitt" are also roots of the OID tree, but not
     * returned by this method (use a search in the default context
     * to find them).
     *
     * @return the root object identifier value ("iso" OID)
     *
     * @see #getOid(String)
     * @see #getDefaultContext()
     * @since 2.7
     */
    public ObjectIdentifierValue getRootOid() {
        MibSymbol symbol = context.findSymbol(DefaultContext.ISO, false);
        MibValue value = ((MibValueSymbol) symbol).getValue();
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
        return mibs.get(name);
    }

    /**
     * Returns a previously loaded MIB file. If the MIB file hasn't
     * been loaded, null will be returned. The MIB is identified by
     * it's file name.<p>
     *
     * Note that if the file contained several MIB modules, this
     * method will only return the first one. Use getMibs(File) to
     * retrieve all.
     *
     * @param file           the MIB file
     *
     * @return the first MIB module if found, or
     *         null otherwise
     *
     * @since 2.3
     */
    public Mib getMib(File file) {
        for (Mib mib : mibs.values()) {
            if (mib.equals(file)) {
                return mib;
            }
        }
        return null;
    }

    /**
     * Returns a map of all MIB names and MIB files. If no MIB files
     * have been loaded, an empty map will be returned. The map is
     * ordered by load order.
     *
     * @return a map of MIB names to MIB objects
     *
     * @since 2.10
     */
    public Map<String,Mib> getMibs() {
        return mibs;
    }

    /**
     * Returns a map of all MIBs from a file. Normally, this is only
     * a single MIB, but some files may contain multiple MIBs. If the
     * file hasn't been loaded, an empty map will be returned. The
     * map is ordered by load order.
     *
     * @param file           the MIB file
     *
     * @return a map of MIB names to MIB objects
     *
     * @since 2.10
     */
    public Map<String,Mib> getMibs(File file) {
        LinkedHashMap<String,Mib> res = new LinkedHashMap<>();
        for (Mib mib : mibs.values()) {
            if (mib.equals(file)) {
                res.put(mib.getName(), mib);
            }
        }
        return res;
    }

    /**
     * Returns a map of all MIBs explicitly (or implicitly) loaded.
     * If no MIB files have been loaded, an empty map will be
     * returned. The map is ordered by load order.
     *
     * @param loaded         the explicitly loaded MIB flag
     *
     * @return a map of MIB names to MIB objects
     *
     * @since 2.10
     *
     * @see Mib#isLoaded()
     */
    public Map<String,Mib> getMibs(boolean loaded) {
        LinkedHashMap<String,Mib> res = new LinkedHashMap<>();
        for (Mib mib : mibs.values()) {
            if (mib.isLoaded() == loaded) {
                res.put(mib.getName(), mib);
            }
        }
        return res;
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
        return mibs.values().toArray(new Mib[mibs.size()]);
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
        Mib mib = getMib(name);
        if (mib == null) {
            MibSource src = locate(name);
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
     * instead of reloading it.<p>
     *
     * Note that if a file contains several MIB modules, this method
     * will only return the first one (although all are loaded). Use
     * getMibs(File) to retrieve all.
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
        Map<String, Mib> found = getMibs(file);
        for (Mib mib : found.values()) {
            mib.setLoaded(true);
        }
        if (found.size() <= 0) {
            return load(new MibSource(file));
        } else {
            return found.values().iterator().next();
        }
    }

    /**
     * Loads a MIB file from the specified URL. This method will also
     * load all imported MIB:s if not previously loaded by this
     * loader.<p>
     *
     * Note that if the URL data contains several MIB modules, this
     * method will only return the first one (although all are
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
     * this loader.<p>
     *
     * Note that if the input data contains several MIB modules, this
     * method will only return the first one (although all are
     * loaded).
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
     * not previously loaded by this loader.<p>
     *
     * Note that if the source contains several MIB modules, this
     * method will only return the first one (although all are
     * loaded).
     *
     * @param src            the MIB source
     *
     * @return the first MIB module loaded
     *
     * @throws IOException if the MIB couldn't be found
     * @throws MibLoaderException if one of the MIB:s couldn't be
     *             loaded correctly
     *
     * @since 2.10
     */
    public Mib load(MibSource src) throws IOException, MibLoaderException {
        queue.clear();
        queue.add(src);
        return loadQueue();
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
        unload(getMib(name));
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
        Iterator<Mib> iter = mibs.values().iterator();
        while (iter.hasNext()) {
            Mib mib = iter.next();
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
        if (mib != null) {
            Mib[] referers = mib.getImportingMibs();
            if (referers.length > 0) {
                String msg = "cannot be unloaded due to reference in " +
                             referers[0];
                throw new MibLoaderException(msg);
            }
            mibs.remove(mib.getName());
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
        Iterator<Mib> iter = mibs.values().iterator();
        while (iter.hasNext()) {
            iter.next().clear();
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
     * @return the first MIB module loaded
     *
     * @throws IOException if the MIB couldn't be found
     * @throws MibLoaderException if one of the MIB:s couldn't be
     *             loaded correctly
     */
    private Mib loadQueue() throws IOException, MibLoaderException {

        // Parse MIB files in queue
        MibLoaderLog log = new MibLoaderLog();
        ArrayList<Mib> processed = new ArrayList<>();
        Mib firstMib = null;
        while (queue.size() > 0) {
            try {
                boolean loaded = false;
                MibSource src = null;
                Object obj = queue.get(0);
                if (obj instanceof MibSource) {
                    loaded = true;
                    src = (MibSource) obj;
                } else if (getMib((String) obj) == null) {
                    src = locate((String) obj);
                }
                if (src != null && getMib(src.getFile()) == null) {
                    List<Mib> list = parseMib(src, log);
                    for (Mib mib : list) {
                        mib.setLoaded(loaded);
                        mibs.put(mib.getName(), mib);
                        if (firstMib == null) {
                            firstMib = mib;
                        }
                    }
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
                processed.get(i).initialize();
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log
            }
        }

        // Validate all parsed MIB files in reverse order
        for (int i = processed.size() - 1; i >= 0; i--) {
            try {
                processed.get(i).validate();
            } catch (MibLoaderException e) {
                // Do nothing, errors are already in the log
            }
        }

        // Handle errors
        if (log.errorCount() > 0) {
            for (Mib mib : processed) {
                mibs.remove(mib.getName());
                mib.clear();
            }
            throw new MibLoaderException(log);
        }

        return firstMib;
    }

    /**
     * Parses a MIB input source and returns the MIB modules found.
     * This method may read the MIB either from file, URL or input
     * stream.
     *
     * @param src            the MIB source to parse
     * @param log            the MIB log to use for errors
     *
     * @return the list of MIB modules created
     *
     * @throws IOException if the MIB couldn't be found
     * @throws MibLoaderException if the MIB couldn't be parsed
     *             or analyzed correctly
     */
    private ArrayList<Mib> parseMib(MibSource src, MibLoaderLog log)
        throws IOException, MibLoaderException {

        MibAnalyzer analyzer = new MibAnalyzer(src.getFile(), this, log);
        try (
            Reader input = src.getReader();
        ) {
            if (parser == null) {
                parser = new Asn1Parser(input, analyzer);
                parser.getTokenizer().setUseTokenList(true);
            } else {
                parser.reset(input, analyzer);
            }
            parser.parse();
            return analyzer.getMibs();
        } catch (ParserCreationException e) {
            String msg = "parser creation error in ASN.1 parser: " +
                         e.getMessage();
            log.addInternalError(msg);
            throw new MibLoaderException(log);
        } catch (ParserLogException e) {
            log.addAll(src.getFile(), e);
            throw new MibLoaderException(log);
        } finally {
            analyzer.reset();
        }
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
        for (MibLocator cache : dirCaches) {
            MibSource src = cache.findByName(name);
            if (src != null) {
                return src;
            }
        }
        ClassLoader loader = getClass().getClassLoader();
        for (String path : resources) {
            URL url = loader.getResource(path + "/" + name);
            if (url != null) {
                return new MibSource(name, url);
            }
        }
        for (MibLocator cache : dirCaches) {
            MibSource src = cache.findByContent(name);
            if (src != null) {
                return src;
            }
        }
        return null;
    }
}
